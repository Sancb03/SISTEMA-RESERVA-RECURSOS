package reserva.logic;

import reserva.red.AlertaListener;
import reserva.red.Notificacion;
import reserva.red.Peticion;
import reserva.red.Protocolo;
import reserva.red.Respuesta;

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
 * EL PROXY (patrón Proxy que pide el enunciado): una clase que "representa"
 * localmente al Service del Backend. Los Controllers la usan igual que usaban
 * Data en el Proyecto 1:
 *
 *      ServiceProxy.instance().listarCategorias(usuarioActual)
 *
 * pero aquí NO hay lógica de negocio ni base de datos: cada método arma una
 * Peticion con su propio nombre y sus parámetros, la manda por el socket al
 * Backend, espera la Respuesta y devuelve el resultado (o relanza la excepción
 * que el Backend reportó), como si la llamada hubiera sido local.
 *
 * Usa DOS conexiones al Backend:
 *   - peticiones: síncrona (uno pregunta, el Backend contesta). Se abre sola
 *                 la primera vez que se necesita (o con conectar()).
 *   - alertas:    solo recibe. La abre iniciarAlertas() cuando el usuario
 *                 inicia sesión, y un hilo escucha las Notificacion que el
 *                 Backend empuja cuando OTRO usuario crea una reserva.
 */
public class ServiceProxy implements Service {

    private static ServiceProxy instancia;

    public static synchronized ServiceProxy instance() {
        if (instancia == null) {
            instancia = new ServiceProxy();
        }
        return instancia;
    }

    private final String host;
    private final int puerto;

    // canal de peticiones
    private Socket socket;
    private ObjectOutputStream salida;
    private ObjectInputStream entrada;

    // canal de alertas
    private Socket socketAlertas;
    private Thread hiloAlertas;

    /** Lee servidor.properties (se puede sobreescribir con -Dreserva.servidor.host / -Dreserva.servidor.puerto). */
    private ServiceProxy() {
        Properties prop = new Properties();
        try (InputStream in = ServiceProxy.class.getResourceAsStream("/servidor.properties")) {
            if (in != null) {
                prop.load(in);
            }
        } catch (IOException ex) {
            throw new RuntimeException("No se pudo leer servidor.properties: " + ex.getMessage(), ex);
        }
        this.host = System.getProperty("reserva.servidor.host", prop.getProperty("servidor_host", "localhost"));
        this.puerto = Integer.parseInt(System.getProperty("reserva.servidor.puerto",
                prop.getProperty("servidor_puerto", String.valueOf(Protocolo.PUERTO_POR_DEFECTO))));
    }

    /** Para pruebas: un Proxy apuntando a un servidor específico (no es el singleton). */
    public ServiceProxy(String host, int puerto) {
        this.host = host;
        this.puerto = puerto;
    }

    public String getHost() {
        return host;
    }

    public int getPuerto() {
        return puerto;
    }

    // =====================================================================
    // Conexión
    // =====================================================================

    /** Abre la conexión de peticiones (si ya estaba abierta, no hace nada). */
    public synchronized void conectar() throws IOException {
        if (socket != null && !socket.isClosed()) {
            return;
        }
        Socket nuevo = new Socket(host, puerto);
        try {
            // primero el OutputStream y flush, luego el InputStream: igual que hace el Backend
            ObjectOutputStream out = new ObjectOutputStream(nuevo.getOutputStream());
            out.flush();
            ObjectInputStream in = new ObjectInputStream(nuevo.getInputStream());
            out.writeObject(Protocolo.CANAL_PETICIONES);
            out.flush();
            socket = nuevo;
            salida = out;
            entrada = in;
        } catch (IOException ex) {
            nuevo.close();
            throw ex;
        }
    }

    private synchronized void desconectar() {
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException ignored) {
        }
        socket = null;
        salida = null;
        entrada = null;
    }

    /** Cierra las dos conexiones (al cerrar la aplicación o cerrar sesión). */
    public synchronized void stop() {
        detenerAlertas();
        desconectar();
    }

    // =====================================================================
    // Alertas (notificaciones asíncronas del Backend)
    // =====================================================================

    /**
     * Abre el canal de alertas para el usuario que acaba de iniciar sesión y
     * arranca un hilo que, por cada reserva nueva de OTRO usuario, llama a
     * listener.nuevaReserva(...). Si ya había un canal abierto, lo reemplaza.
     */
    public synchronized void iniciarAlertas(Usuario usuario, AlertaListener listener) throws IOException {
        detenerAlertas();

        Socket nuevo = new Socket(host, puerto);
        ObjectInputStream in;
        try {
            ObjectOutputStream out = new ObjectOutputStream(nuevo.getOutputStream());
            out.flush();
            in = new ObjectInputStream(nuevo.getInputStream());
            out.writeObject(Protocolo.CANAL_ALERTAS);
            out.writeObject(usuario.getId());
            out.flush();
        } catch (IOException ex) {
            nuevo.close();
            throw ex;
        }

        socketAlertas = nuevo;
        hiloAlertas = new Thread(() -> escucharAlertas(nuevo, in, listener), "alertas-" + usuario.getIdentificacion());
        hiloAlertas.setDaemon(true); // que no impida cerrar la aplicación
        hiloAlertas.start();
    }

    private void escucharAlertas(Socket mio, ObjectInputStream in, AlertaListener listener) {
        try {
            while (!mio.isClosed()) {
                Object recibido = in.readObject();
                if (recibido instanceof Notificacion n
                        && Notificacion.NUEVA_RESERVA.equals(n.getTipo())
                        && n.getDatos() instanceof Reserva reserva) {
                    listener.nuevaReserva(reserva);
                }
            }
        } catch (IOException | ClassNotFoundException ex) {
            // el canal se cerró (detenerAlertas) o el Backend se cayó: el hilo termina
        }
    }

    public synchronized void detenerAlertas() {
        try {
            if (socketAlertas != null) {
                socketAlertas.close(); // despierta al hilo, que sale por la IOException
            }
        } catch (IOException ignored) {
        }
        socketAlertas = null;
        hiloAlertas = null;
    }

    // =====================================================================
    // Envío de peticiones
    // =====================================================================

    /**
     * Manda una Peticion y devuelve el resultado de la Respuesta. Es synchronized:
     * aunque varios hilos del Frontend llamen al Proxy, por el socket solo viaja
     * una pregunta a la vez y cada Respuesta se le entrega a quien la preguntó.
     */
    private synchronized Object enviar(String operacion, Object... parametros) {
        try {
            conectar();
            salida.writeObject(new Peticion(operacion, parametros));
            salida.flush();
            salida.reset(); // si no, el stream reenvía versiones viejas de objetos que cambiaron
            Respuesta respuesta = (Respuesta) entrada.readObject();

            if (!respuesta.isExito()) {
                throw excepcionDe(respuesta);
            }
            if (respuesta.getEntidadActualizada() != null && parametros.length > 0) {
                copiarEstado(respuesta.getEntidadActualizada(), parametros[0]);
            }
            return respuesta.getResultado();
        } catch (IOException | ClassNotFoundException ex) {
            desconectar(); // la próxima llamada intenta reconectar
            throw new RuntimeException("No se pudo comunicar con el servidor (" + host + ":" + puerto + "): "
                    + ex.getMessage(), ex);
        }
    }

    /** Reconstruye la excepción que el Backend reportó, para que el Controller la atrape igual que antes. */
    private RuntimeException excepcionDe(Respuesta r) {
        String mensaje = r.getMensajeError();
        if (SecurityException.class.getName().equals(r.getTipoError())) {
            return new SecurityException(mensaje);
        }
        if (IllegalStateException.class.getName().equals(r.getTipoError())) {
            return new IllegalStateException(mensaje);
        }
        if (IllegalArgumentException.class.getName().equals(r.getTipoError())) {
            return new IllegalArgumentException(mensaje);
        }
        return new RuntimeException(mensaje);
    }

    /**
     * En el Proyecto 1, guardarCategoria(c) le ponía el id al MISMO objeto c. Por la red el
     * Backend trabajó con una copia, así que aquí se le copian de vuelta los campos que el
     * Backend modificó (id generado, clave inicial...) al objeto original del Controller.
     */
    static void copiarEstado(Object desde, Object hacia) {
        if (desde == hacia || hacia == null || desde.getClass() != hacia.getClass()) {
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

    // =====================================================================
    // Service: cada método solo traduce la llamada a una Peticion
    // =====================================================================

    @Override
    public boolean guardarUsuario(Usuario usuario) {
        return (boolean) enviar("guardarUsuario", usuario);
    }

    @Override
    public boolean actualizarUsuario(Usuario usuario) {
        return (boolean) enviar("actualizarUsuario", usuario);
    }

    @Override
    public boolean eliminarUsuario(int id) {
        return (boolean) enviar("eliminarUsuario", id);
    }

    @Override
    public Usuario buscarUsuarioPorId(int id) {
        return (Usuario) enviar("buscarUsuarioPorId", id);
    }

    @Override
    public Usuario buscarUsuarioPorIdentificacion(String identificacion) {
        return (Usuario) enviar("buscarUsuarioPorIdentificacion", identificacion);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Usuario> listarUsuarios() {
        return (List<Usuario>) enviar("listarUsuarios");
    }

    @Override
    public Usuario login(String identificacion, String clave) {
        return (Usuario) enviar("login", identificacion, clave);
    }

    @Override
    public boolean cambiarClave(String identificacion, String claveActual, String claveNueva) {
        return (boolean) enviar("cambiarClave", identificacion, claveActual, claveNueva);
    }

    @Override
    public boolean guardarFuncionario(Funcionario funcionario, Usuario usuarioActual) {
        return (boolean) enviar("guardarFuncionario", funcionario, usuarioActual);
    }

    @Override
    public boolean actualizarFuncionario(Funcionario funcionario, Usuario usuarioActual) {
        return (boolean) enviar("actualizarFuncionario", funcionario, usuarioActual);
    }

    @Override
    public boolean eliminarFuncionario(int id, Usuario usuarioActual) {
        return (boolean) enviar("eliminarFuncionario", id, usuarioActual);
    }

    @Override
    public Funcionario buscarFuncionarioPorId(int id, Usuario usuarioActual) {
        return (Funcionario) enviar("buscarFuncionarioPorId", id, usuarioActual);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Funcionario> buscarFuncionariosPorNombre(String nombre, Usuario usuarioActual) {
        return (List<Funcionario>) enviar("buscarFuncionariosPorNombre", nombre, usuarioActual);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Funcionario> listarFuncionarios(Usuario usuarioActual) {
        return (List<Funcionario>) enviar("listarFuncionarios", usuarioActual);
    }

    @Override
    public boolean guardarCategoria(Categoria categoria, Usuario usuarioActual) {
        return (boolean) enviar("guardarCategoria", categoria, usuarioActual);
    }

    @Override
    public boolean actualizarCategoria(Categoria categoria, Usuario usuarioActual) {
        return (boolean) enviar("actualizarCategoria", categoria, usuarioActual);
    }

    @Override
    public boolean eliminarCategoria(String id, Usuario usuarioActual) {
        return (boolean) enviar("eliminarCategoria", id, usuarioActual);
    }

    @Override
    public Categoria buscarCategoriaPorId(String id, Usuario usuarioActual) {
        return (Categoria) enviar("buscarCategoriaPorId", id, usuarioActual);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Categoria> buscarCategoriasPorDescripcion(String texto, Usuario usuarioActual) {
        return (List<Categoria>) enviar("buscarCategoriasPorDescripcion", texto, usuarioActual);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Categoria> listarCategorias(Usuario usuarioActual) {
        return (List<Categoria>) enviar("listarCategorias", usuarioActual);
    }

    @Override
    public boolean guardarRecurso(Recurso recurso, Usuario usuarioActual) {
        return (boolean) enviar("guardarRecurso", recurso, usuarioActual);
    }

    @Override
    public boolean actualizarRecurso(Recurso recurso, Usuario usuarioActual) {
        return (boolean) enviar("actualizarRecurso", recurso, usuarioActual);
    }

    @Override
    public boolean eliminarRecurso(String id, Usuario usuarioActual) {
        return (boolean) enviar("eliminarRecurso", id, usuarioActual);
    }

    @Override
    public Recurso buscarRecursoPorId(String id, Usuario usuarioActual) {
        return (Recurso) enviar("buscarRecursoPorId", id, usuarioActual);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Recurso> buscarRecursosPorCategoria(Categoria categoria, Usuario usuarioActual) {
        return (List<Recurso>) enviar("buscarRecursosPorCategoria", categoria, usuarioActual);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Recurso> listarRecursos(Usuario usuarioActual) {
        return (List<Recurso>) enviar("listarRecursos", usuarioActual);
    }

    @Override
    public boolean guardarReserva(Reserva reserva) {
        return (boolean) enviar("guardarReserva", reserva);
    }

    @Override
    public boolean actualizarReserva(Reserva reserva) {
        return (boolean) enviar("actualizarReserva", reserva);
    }

    @Override
    public boolean eliminarReserva(String id) {
        return (boolean) enviar("eliminarReserva", id);
    }

    @Override
    public Reserva buscarReservaPorId(String id) {
        return (Reserva) enviar("buscarReservaPorId", id);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Reserva> listarReservas() {
        return (List<Reserva>) enviar("listarReservas");
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Reserva> listarReservasPorFuncionario(int funcionarioId) {
        return (List<Reserva>) enviar("listarReservasPorFuncionario", funcionarioId);
    }

    @Override
    public boolean existeSolape(Recurso recurso, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin,
                                String excluirReservaId) {
        return (boolean) enviar("existeSolape", recurso, fecha, horaInicio, horaFin, excluirReservaId);
    }
}
