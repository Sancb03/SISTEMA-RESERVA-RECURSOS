package reserva.logic;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.Socket;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Properties;

/**
 * EL PROXY que pide el enunciado: representa localmente al Service del Backend.
 * Los Controllers lo usan como antes usaban Data:
 *
 *      Service.instance().listarCategorias(usuarioActual)
 *
 * Aquí NO hay lógica de negocio ni base de datos: cada método manda por el socket
 * el código de la operación (Protocol) y sus parámetros, espera la respuesta y
 * devuelve el resultado (o relanza la excepción que reportó el Backend).
 *
 * Usa DOS conexiones, como el ejemplo Personas:
 *   - sincrónica: peticiones (uno pregunta, el Backend contesta). Al conectar, el
 *     Backend devuelve un identificador de sesión (sid).
 *   - asincrónica: la abre SocketMonitor con ese sid, y por ella el Backend empuja las
 *     reservas que hacen los demás usuarios.
 */
public class Service implements IService {
    private static Service theInstance;

    public static Service instance() {
        if (theInstance == null) theInstance = new Service();
        return theInstance;
    }

    private final String host;
    private final int port;

    Socket s;
    ObjectOutputStream os;
    ObjectInputStream is;

    String sid; // Session Id

    /** Lee servidor.properties (se puede sobreescribir con -Dreserva.servidor.host / -Dreserva.servidor.puerto). */
    private Service() {
        Properties prop = new Properties();
        try (InputStream in = Service.class.getResourceAsStream("/servidor.properties")) {
            if (in != null) {
                prop.load(in);
            }
        } catch (IOException ex) {
            throw new RuntimeException("No se pudo leer servidor.properties: " + ex.getMessage(), ex);
        }
        this.host = System.getProperty("reserva.servidor.host", prop.getProperty("servidor_host", Protocol.SERVER));
        this.port = Integer.parseInt(System.getProperty("reserva.servidor.puerto",
                prop.getProperty("servidor_puerto", String.valueOf(Protocol.PORT))));
    }

    /** Para pruebas: un Service apuntando a un servidor específico (no es el singleton). */
    public Service(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getSid() {
        return sid;
    }

    // =============== CONEXIÓN ===============

    /** Abre la conexión sincrónica y recibe el sid (si ya estaba abierta, no hace nada). */
    public synchronized void conectar() throws IOException {
        if (s != null && !s.isClosed()) {
            return;
        }
        Socket nuevo = new Socket(host, port);
        try {
            ObjectOutputStream out = new ObjectOutputStream(nuevo.getOutputStream());
            out.flush();
            ObjectInputStream in = new ObjectInputStream(nuevo.getInputStream());
            out.writeInt(Protocol.SYNC);
            out.flush();
            sid = (String) in.readObject(); // Stores returned Session Id
            s = nuevo;
            os = out;
            is = in;
        } catch (IOException | ClassNotFoundException | ClassCastException ex) {
            nuevo.close();
            throw new IOException("Respuesta inesperada del servidor: " + ex.getMessage(), ex);
        }
    }

    private synchronized void desconectar() {
        try {
            if (s != null) {
                s.close();
            }
        } catch (IOException ignored) {
        }
        s = null;
        os = null;
        is = null;
    }

    @Override
    public synchronized void stop() {
        try {
            if (s != null && !s.isClosed()) {
                os.writeInt(Protocol.DISCONNECT);
                os.flush();
                s.shutdownOutput();
            }
        } catch (IOException ignored) {
        }
        desconectar();
    }

    // =============== ENVÍO DE PETICIONES ===============

    /**
     * Manda la operación y sus parámetros y devuelve el resultado. Es synchronized: aunque varios
     * hilos del Frontend llamen al Service, por el socket solo viaja una pregunta a la vez.
     */
    private synchronized Object pedir(int operacion, Object... parametros) {
        try {
            enviar(operacion, parametros);
            return recibir();
        } catch (IOException | ClassNotFoundException ex) {
            desconectar(); // la próxima llamada intenta reconectar
            throw new RuntimeException("No se pudo comunicar con el servidor (" + host + ":" + port + "): "
                    + ex.getMessage(), ex);
        }
    }

    /** Igual que pedir, pero los guardar* devuelven la entidad con el id y datos que le puso el Backend. */
    private synchronized Object pedirYCopiar(int operacion, Object entidad, Object... otros) {
        Object[] todos = new Object[otros.length + 1];
        todos[0] = entidad;
        System.arraycopy(otros, 0, todos, 1, otros.length);
        try {
            enviar(operacion, todos);
            Object resultado = recibir();
            copiarEstado(is.readObject(), entidad);
            return resultado;
        } catch (IOException | ClassNotFoundException ex) {
            desconectar();
            throw new RuntimeException("No se pudo comunicar con el servidor (" + host + ":" + port + "): "
                    + ex.getMessage(), ex);
        }
    }

    private void enviar(int operacion, Object[] parametros) throws IOException {
        conectar();
        os.writeInt(operacion);
        for (Object p : parametros) {
            os.writeObject(p);
        }
        os.flush();
        os.reset(); // si no, el stream reenvía versiones viejas de objetos que cambiaron
    }

    /** Lee el código de respuesta; si no fue ERROR_NO_ERROR relanza la excepción con su mensaje. */
    private Object recibir() throws IOException, ClassNotFoundException {
        int codigo = is.readInt();
        if (codigo == Protocol.ERROR_NO_ERROR) {
            return is.readObject();
        }
        String mensaje = (String) is.readObject();
        switch (codigo) {
            case Protocol.ERROR_SECURITY:
                throw new SecurityException(mensaje);
            case Protocol.ERROR_STATE:
                throw new IllegalStateException(mensaje);
            case Protocol.ERROR_ARGUMENT:
                throw new IllegalArgumentException(mensaje);
            default:
                throw new RuntimeException(mensaje);
        }
    }

    /**
     * En el Proyecto 1, guardarCategoria(c) le ponía el id al MISMO objeto c. Por la red el
     * Backend trabajó con una copia, así que aquí se le copian de vuelta los campos que el
     * Backend modificó (id generado, clave inicial...) al objeto original del Controller.
     */
    static void copiarEstado(Object desde, Object hacia) {
        if (desde == hacia || desde == null || hacia == null || desde.getClass() != hacia.getClass()) {
            return;
        }
        for (Class<?> c = desde.getClass(); c != null && c != Object.class; c = c.getSuperclass()) {
            for (Field campo : c.getDeclaredFields()) {
                if (Modifier.isStatic(campo.getModifiers())) {
                    continue;
                }
                try {
                    campo.setAccessible(true);
                    campo.set(hacia, campo.get(desde));
                } catch (IllegalAccessException ex) {
                    throw new IllegalStateException(ex);
                }
            }
        }
    }

    // =============== USUARIOS (usado por Login) ===============
    @Override
    public boolean guardarUsuario(Usuario usuario) {
        return (boolean) pedirYCopiar(Protocol.GUARDAR_USUARIO, usuario);
    }

    @Override
    public boolean actualizarUsuario(Usuario usuario) {
        return (boolean) pedir(Protocol.ACTUALIZAR_USUARIO, usuario);
    }

    @Override
    public boolean eliminarUsuario(int id) {
        return (boolean) pedir(Protocol.ELIMINAR_USUARIO, id);
    }

    @Override
    public Usuario buscarUsuarioPorId(int id) {
        return (Usuario) pedir(Protocol.BUSCAR_USUARIO_POR_ID, id);
    }

    @Override
    public Usuario buscarUsuarioPorIdentificacion(String identificacion) {
        return (Usuario) pedir(Protocol.BUSCAR_USUARIO_POR_IDENTIFICACION, identificacion);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Usuario> listarUsuarios() {
        return (List<Usuario>) pedir(Protocol.LISTAR_USUARIOS);
    }

    @Override
    public Usuario autenticar(String identificacion, String clave) {
        return (Usuario) pedir(Protocol.AUTENTICAR, identificacion, clave);
    }

    @Override
    public boolean cambiarClave(String identificacion, String claveActual, String claveNueva) {
        return (boolean) pedir(Protocol.CAMBIAR_CLAVE, identificacion, claveActual, claveNueva);
    }

    // =============== FUNCIONARIOS ===============
    @Override
    public boolean guardarFuncionario(Funcionario funcionario, Usuario usuarioActual) {
        return (boolean) pedirYCopiar(Protocol.GUARDAR_FUNCIONARIO, funcionario, usuarioActual);
    }

    @Override
    public boolean actualizarFuncionario(Funcionario funcionario, Usuario usuarioActual) {
        return (boolean) pedir(Protocol.ACTUALIZAR_FUNCIONARIO, funcionario, usuarioActual);
    }

    @Override
    public boolean eliminarFuncionario(int id, Usuario usuarioActual) {
        return (boolean) pedir(Protocol.ELIMINAR_FUNCIONARIO, id, usuarioActual);
    }

    @Override
    public Funcionario buscarFuncionarioPorId(int id, Usuario usuarioActual) {
        return (Funcionario) pedir(Protocol.BUSCAR_FUNCIONARIO_POR_ID, id, usuarioActual);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Funcionario> buscarFuncionariosPorNombre(String nombre, Usuario usuarioActual) {
        return (List<Funcionario>) pedir(Protocol.BUSCAR_FUNCIONARIOS_POR_NOMBRE, nombre, usuarioActual);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Funcionario> listarFuncionarios(Usuario usuarioActual) {
        return (List<Funcionario>) pedir(Protocol.LISTAR_FUNCIONARIOS, usuarioActual);
    }

    // =============== CATEGORIAS ===============
    @Override
    public boolean guardarCategoria(Categoria categoria, Usuario usuarioActual) {
        return (boolean) pedirYCopiar(Protocol.GUARDAR_CATEGORIA, categoria, usuarioActual);
    }

    @Override
    public boolean actualizarCategoria(Categoria categoria, Usuario usuarioActual) {
        return (boolean) pedir(Protocol.ACTUALIZAR_CATEGORIA, categoria, usuarioActual);
    }

    @Override
    public boolean eliminarCategoria(String id, Usuario usuarioActual) {
        return (boolean) pedir(Protocol.ELIMINAR_CATEGORIA, id, usuarioActual);
    }

    @Override
    public Categoria buscarCategoriaPorId(String id, Usuario usuarioActual) {
        return (Categoria) pedir(Protocol.BUSCAR_CATEGORIA_POR_ID, id, usuarioActual);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Categoria> buscarCategoriasPorDescripcion(String texto, Usuario usuarioActual) {
        return (List<Categoria>) pedir(Protocol.BUSCAR_CATEGORIAS_POR_DESCRIPCION, texto, usuarioActual);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Categoria> listarCategorias(Usuario usuarioActual) {
        return (List<Categoria>) pedir(Protocol.LISTAR_CATEGORIAS, usuarioActual);
    }

    // =============== RECURSOS ===============
    @Override
    public boolean guardarRecurso(Recurso recurso, Usuario usuarioActual) {
        return (boolean) pedirYCopiar(Protocol.GUARDAR_RECURSO, recurso, usuarioActual);
    }

    @Override
    public boolean actualizarRecurso(Recurso recurso, Usuario usuarioActual) {
        return (boolean) pedir(Protocol.ACTUALIZAR_RECURSO, recurso, usuarioActual);
    }

    @Override
    public boolean eliminarRecurso(String id, Usuario usuarioActual) {
        return (boolean) pedir(Protocol.ELIMINAR_RECURSO, id, usuarioActual);
    }

    @Override
    public Recurso buscarRecursoPorId(String id, Usuario usuarioActual) {
        return (Recurso) pedir(Protocol.BUSCAR_RECURSO_POR_ID, id, usuarioActual);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Recurso> buscarRecursosPorCategoria(Categoria categoria, Usuario usuarioActual) {
        return (List<Recurso>) pedir(Protocol.BUSCAR_RECURSOS_POR_CATEGORIA, categoria, usuarioActual);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Recurso> listarRecursos(Usuario usuarioActual) {
        return (List<Recurso>) pedir(Protocol.LISTAR_RECURSOS, usuarioActual);
    }

    // =============== RESERVAS ===============
    @Override
    public boolean guardarReserva(Reserva reserva) {
        return (boolean) pedirYCopiar(Protocol.GUARDAR_RESERVA, reserva);
    }

    @Override
    public boolean actualizarReserva(Reserva reserva) {
        return (boolean) pedir(Protocol.ACTUALIZAR_RESERVA, reserva);
    }

    @Override
    public boolean eliminarReserva(String id) {
        return (boolean) pedir(Protocol.ELIMINAR_RESERVA, id);
    }

    @Override
    public Reserva buscarReservaPorId(String id) {
        return (Reserva) pedir(Protocol.BUSCAR_RESERVA_POR_ID, id);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Reserva> listarReservas() {
        return (List<Reserva>) pedir(Protocol.LISTAR_RESERVAS);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Reserva> listarReservasPorFuncionario(int funcionarioId) {
        return (List<Reserva>) pedir(Protocol.LISTAR_RESERVAS_POR_FUNCIONARIO, funcionarioId);
    }

    @Override
    public boolean existeSolape(Recurso recurso, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin, String excluirReservaId) {
        return (boolean) pedir(Protocol.EXISTE_SOLAPE, recurso, fecha, horaInicio, horaFin, excluirReservaId);
    }
}
