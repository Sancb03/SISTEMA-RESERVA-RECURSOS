package reserva.logic;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Atiende a UN Frontend: un hilo que lee el código de la operación, lee sus
 * parámetros, llama al Service y escribe la respuesta (primero un int con
 * ERROR_NO_ERROR o el tipo de error, luego el resultado o el mensaje).
 * Además guarda el socket asíncrono de ese Frontend para empujarle alertas.
 */
public class Worker {
    Server srv;
    Socket s;
    ObjectOutputStream os;
    ObjectInputStream is;
    IService service;

    String sid; // Session Id
    Socket as; // Asynchronous Socket
    ObjectOutputStream aos;
    ObjectInputStream ais;

    public Worker(Server srv, Socket s, ObjectOutputStream os, ObjectInputStream is, String sid, IService service) {
        this.srv = srv;
        this.s = s;
        this.os = os;
        this.is = is;
        this.service = service;
        this.sid = sid;
    }

    public void setAs(Socket as, ObjectOutputStream aos, ObjectInputStream ais) {
        this.as = as;
        this.aos = aos;
        this.ais = ais;
    }

    boolean continuar;

    public void start() {
        try {
            System.out.println("Worker atendiendo peticiones...");
            Thread t = new Thread(new Runnable() {
                public void run() {
                    listen();
                }
            });
            continuar = true;
            t.start();
        } catch (Exception ex) {
        }
    }

    public void stop() {
        continuar = false;
        try {
            s.close();
            if (as != null) as.close();
        } catch (IOException ex) {
        }
        System.out.println("Conexion cerrada...");
    }

    public void listen() {
        int method;
        while (continuar) {
            try {
                method = is.readInt();
                System.out.println("Operacion: " + method);
                switch (method) {
                    // ===== USUARIOS (usado por Login) =====
                    case Protocol.GUARDAR_USUARIO:
                        try {
                            Usuario usuario = (Usuario) is.readObject();
                            boolean r = service.guardarUsuario(usuario);
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                            os.writeObject(r);
                            os.writeObject(usuario); // con el id y demás datos que puso el Backend
                        } catch (RuntimeException ex) {
                            error(ex);
                        }
                        break;
                    case Protocol.ACTUALIZAR_USUARIO:
                        try {
                            Usuario usuario = (Usuario) is.readObject();
                            boolean r = service.actualizarUsuario(usuario);
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                            os.writeObject(r);
                        } catch (RuntimeException ex) {
                            error(ex);
                        }
                        break;
                    case Protocol.ELIMINAR_USUARIO:
                        try {
                            int id = (Integer) is.readObject();
                            boolean r = service.eliminarUsuario(id);
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                            os.writeObject(r);
                        } catch (RuntimeException ex) {
                            error(ex);
                        }
                        break;
                    case Protocol.BUSCAR_USUARIO_POR_ID:
                        try {
                            int id = (Integer) is.readObject();
                            Usuario r = service.buscarUsuarioPorId(id);
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                            os.writeObject(r);
                        } catch (RuntimeException ex) {
                            error(ex);
                        }
                        break;
                    case Protocol.BUSCAR_USUARIO_POR_IDENTIFICACION:
                        try {
                            String identificacion = (String) is.readObject();
                            Usuario r = service.buscarUsuarioPorIdentificacion(identificacion);
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                            os.writeObject(r);
                        } catch (RuntimeException ex) {
                            error(ex);
                        }
                        break;
                    case Protocol.LISTAR_USUARIOS:
                        try {
                            List<Usuario> r = service.listarUsuarios();
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                            os.writeObject(r);
                        } catch (RuntimeException ex) {
                            error(ex);
                        }
                        break;
                    case Protocol.AUTENTICAR:
                        try {
                            String identificacion = (String) is.readObject();
                            String clave = (String) is.readObject();
                            Usuario r = service.autenticar(identificacion, clave);
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                            os.writeObject(r);
                        } catch (RuntimeException ex) {
                            error(ex);
                        }
                        break;
                    case Protocol.CAMBIAR_CLAVE:
                        try {
                            String identificacion = (String) is.readObject();
                            String claveActual = (String) is.readObject();
                            String claveNueva = (String) is.readObject();
                            boolean r = service.cambiarClave(identificacion, claveActual, claveNueva);
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                            os.writeObject(r);
                        } catch (RuntimeException ex) {
                            error(ex);
                        }
                        break;
                    // ===== FUNCIONARIOS =====
                    case Protocol.GUARDAR_FUNCIONARIO:
                        try {
                            Funcionario funcionario = (Funcionario) is.readObject();
                            Usuario usuarioActual = (Usuario) is.readObject();
                            boolean r = service.guardarFuncionario(funcionario, usuarioActual);
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                            os.writeObject(r);
                            os.writeObject(funcionario); // con el id y demás datos que puso el Backend
                        } catch (RuntimeException ex) {
                            error(ex);
                        }
                        break;
                    case Protocol.ACTUALIZAR_FUNCIONARIO:
                        try {
                            Funcionario funcionario = (Funcionario) is.readObject();
                            Usuario usuarioActual = (Usuario) is.readObject();
                            boolean r = service.actualizarFuncionario(funcionario, usuarioActual);
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                            os.writeObject(r);
                        } catch (RuntimeException ex) {
                            error(ex);
                        }
                        break;
                    case Protocol.ELIMINAR_FUNCIONARIO:
                        try {
                            int id = (Integer) is.readObject();
                            Usuario usuarioActual = (Usuario) is.readObject();
                            boolean r = service.eliminarFuncionario(id, usuarioActual);
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                            os.writeObject(r);
                        } catch (RuntimeException ex) {
                            error(ex);
                        }
                        break;
                    case Protocol.BUSCAR_FUNCIONARIO_POR_ID:
                        try {
                            int id = (Integer) is.readObject();
                            Usuario usuarioActual = (Usuario) is.readObject();
                            Funcionario r = service.buscarFuncionarioPorId(id, usuarioActual);
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                            os.writeObject(r);
                        } catch (RuntimeException ex) {
                            error(ex);
                        }
                        break;
                    case Protocol.BUSCAR_FUNCIONARIOS_POR_NOMBRE:
                        try {
                            String nombre = (String) is.readObject();
                            Usuario usuarioActual = (Usuario) is.readObject();
                            List<Funcionario> r = service.buscarFuncionariosPorNombre(nombre, usuarioActual);
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                            os.writeObject(r);
                        } catch (RuntimeException ex) {
                            error(ex);
                        }
                        break;
                    case Protocol.LISTAR_FUNCIONARIOS:
                        try {
                            Usuario usuarioActual = (Usuario) is.readObject();
                            List<Funcionario> r = service.listarFuncionarios(usuarioActual);
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                            os.writeObject(r);
                        } catch (RuntimeException ex) {
                            error(ex);
                        }
                        break;
                    // ===== CATEGORIAS =====
                    case Protocol.GUARDAR_CATEGORIA:
                        try {
                            Categoria categoria = (Categoria) is.readObject();
                            Usuario usuarioActual = (Usuario) is.readObject();
                            boolean r = service.guardarCategoria(categoria, usuarioActual);
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                            os.writeObject(r);
                            os.writeObject(categoria); // con el id y demás datos que puso el Backend
                        } catch (RuntimeException ex) {
                            error(ex);
                        }
                        break;
                    case Protocol.ACTUALIZAR_CATEGORIA:
                        try {
                            Categoria categoria = (Categoria) is.readObject();
                            Usuario usuarioActual = (Usuario) is.readObject();
                            boolean r = service.actualizarCategoria(categoria, usuarioActual);
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                            os.writeObject(r);
                        } catch (RuntimeException ex) {
                            error(ex);
                        }
                        break;
                    case Protocol.ELIMINAR_CATEGORIA:
                        try {
                            String id = (String) is.readObject();
                            Usuario usuarioActual = (Usuario) is.readObject();
                            boolean r = service.eliminarCategoria(id, usuarioActual);
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                            os.writeObject(r);
                        } catch (RuntimeException ex) {
                            error(ex);
                        }
                        break;
                    case Protocol.BUSCAR_CATEGORIA_POR_ID:
                        try {
                            String id = (String) is.readObject();
                            Usuario usuarioActual = (Usuario) is.readObject();
                            Categoria r = service.buscarCategoriaPorId(id, usuarioActual);
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                            os.writeObject(r);
                        } catch (RuntimeException ex) {
                            error(ex);
                        }
                        break;
                    case Protocol.BUSCAR_CATEGORIAS_POR_DESCRIPCION:
                        try {
                            String texto = (String) is.readObject();
                            Usuario usuarioActual = (Usuario) is.readObject();
                            List<Categoria> r = service.buscarCategoriasPorDescripcion(texto, usuarioActual);
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                            os.writeObject(r);
                        } catch (RuntimeException ex) {
                            error(ex);
                        }
                        break;
                    case Protocol.LISTAR_CATEGORIAS:
                        try {
                            Usuario usuarioActual = (Usuario) is.readObject();
                            List<Categoria> r = service.listarCategorias(usuarioActual);
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                            os.writeObject(r);
                        } catch (RuntimeException ex) {
                            error(ex);
                        }
                        break;
                    // ===== RECURSOS =====
                    case Protocol.GUARDAR_RECURSO:
                        try {
                            Recurso recurso = (Recurso) is.readObject();
                            Usuario usuarioActual = (Usuario) is.readObject();
                            boolean r = service.guardarRecurso(recurso, usuarioActual);
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                            os.writeObject(r);
                            os.writeObject(recurso); // con el id y demás datos que puso el Backend
                        } catch (RuntimeException ex) {
                            error(ex);
                        }
                        break;
                    case Protocol.ACTUALIZAR_RECURSO:
                        try {
                            Recurso recurso = (Recurso) is.readObject();
                            Usuario usuarioActual = (Usuario) is.readObject();
                            boolean r = service.actualizarRecurso(recurso, usuarioActual);
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                            os.writeObject(r);
                        } catch (RuntimeException ex) {
                            error(ex);
                        }
                        break;
                    case Protocol.ELIMINAR_RECURSO:
                        try {
                            String id = (String) is.readObject();
                            Usuario usuarioActual = (Usuario) is.readObject();
                            boolean r = service.eliminarRecurso(id, usuarioActual);
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                            os.writeObject(r);
                        } catch (RuntimeException ex) {
                            error(ex);
                        }
                        break;
                    case Protocol.BUSCAR_RECURSO_POR_ID:
                        try {
                            String id = (String) is.readObject();
                            Usuario usuarioActual = (Usuario) is.readObject();
                            Recurso r = service.buscarRecursoPorId(id, usuarioActual);
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                            os.writeObject(r);
                        } catch (RuntimeException ex) {
                            error(ex);
                        }
                        break;
                    case Protocol.BUSCAR_RECURSOS_POR_CATEGORIA:
                        try {
                            Categoria categoria = (Categoria) is.readObject();
                            Usuario usuarioActual = (Usuario) is.readObject();
                            List<Recurso> r = service.buscarRecursosPorCategoria(categoria, usuarioActual);
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                            os.writeObject(r);
                        } catch (RuntimeException ex) {
                            error(ex);
                        }
                        break;
                    case Protocol.LISTAR_RECURSOS:
                        try {
                            Usuario usuarioActual = (Usuario) is.readObject();
                            List<Recurso> r = service.listarRecursos(usuarioActual);
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                            os.writeObject(r);
                        } catch (RuntimeException ex) {
                            error(ex);
                        }
                        break;
                    // ===== RESERVAS =====
                    case Protocol.GUARDAR_RESERVA:
                        try {
                            Reserva reserva = (Reserva) is.readObject();
                            boolean r = service.guardarReserva(reserva);
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                            os.writeObject(r);
                            os.writeObject(reserva); // con el id y demás datos que puso el Backend
                            if (r) avisarReservaNueva(reserva.getId());
                        } catch (RuntimeException ex) {
                            error(ex);
                        }
                        break;
                    case Protocol.ACTUALIZAR_RESERVA:
                        try {
                            Reserva reserva = (Reserva) is.readObject();
                            boolean r = service.actualizarReserva(reserva);
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                            os.writeObject(r);
                        } catch (RuntimeException ex) {
                            error(ex);
                        }
                        break;
                    case Protocol.ELIMINAR_RESERVA:
                        try {
                            String id = (String) is.readObject();
                            boolean r = service.eliminarReserva(id);
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                            os.writeObject(r);
                        } catch (RuntimeException ex) {
                            error(ex);
                        }
                        break;
                    case Protocol.BUSCAR_RESERVA_POR_ID:
                        try {
                            String id = (String) is.readObject();
                            Reserva r = service.buscarReservaPorId(id);
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                            os.writeObject(r);
                        } catch (RuntimeException ex) {
                            error(ex);
                        }
                        break;
                    case Protocol.LISTAR_RESERVAS:
                        try {
                            List<Reserva> r = service.listarReservas();
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                            os.writeObject(r);
                        } catch (RuntimeException ex) {
                            error(ex);
                        }
                        break;
                    case Protocol.LISTAR_RESERVAS_POR_FUNCIONARIO:
                        try {
                            int funcionarioId = (Integer) is.readObject();
                            List<Reserva> r = service.listarReservasPorFuncionario(funcionarioId);
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                            os.writeObject(r);
                        } catch (RuntimeException ex) {
                            error(ex);
                        }
                        break;
                    case Protocol.EXISTE_SOLAPE:
                        try {
                            Recurso recurso = (Recurso) is.readObject();
                            LocalDate fecha = (LocalDate) is.readObject();
                            LocalTime horaInicio = (LocalTime) is.readObject();
                            LocalTime horaFin = (LocalTime) is.readObject();
                            String excluirReservaId = (String) is.readObject();
                            boolean r = service.existeSolape(recurso, fecha, horaInicio, horaFin, excluirReservaId);
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                            os.writeObject(r);
                        } catch (RuntimeException ex) {
                            error(ex);
                        }
                        break;
                    case Protocol.DISCONNECT:
                        stop();
                        srv.remove(this);
                        return;
                    default: // código desconocido: el Frontend no sigue el Protocol, se cierra la sesión
                        stop();
                        srv.remove(this);
                        return;
                }
                os.flush();
                os.reset(); // sin esto el stream reenvía copias viejas de objetos que cambiaron
            } catch (IOException | ClassNotFoundException e) {
                if (continuar) {
                    if (!(e instanceof java.io.EOFException || e instanceof java.net.SocketException)) {
                        System.out.println("Sesion " + sid + " terminada por error: " + e);
                    }
                    stop();
                    srv.remove(this);
                }
                return;
            }
        }
    }

    /** Responde con el tipo de error (para que el Frontend relance la misma excepción) y el mensaje. */
    private void error(RuntimeException ex) throws IOException {
        int tipo = Protocol.ERROR_ERROR;
        if (ex instanceof SecurityException) {
            tipo = Protocol.ERROR_SECURITY;
        } else if (ex instanceof IllegalStateException) {
            tipo = Protocol.ERROR_STATE;
        } else if (ex instanceof IllegalArgumentException) {
            tipo = Protocol.ERROR_ARGUMENT;
        } else {
            ex.printStackTrace(); // error inesperado: queda en la consola del Backend
        }
        os.writeInt(tipo);
        os.writeObject(ex.getMessage());
    }

    /** Después de guardar una reserva, la lee de la base y se la manda a los demás Frontend. */
    private void avisarReservaNueva(String id) {
        try {
            srv.deliver_reserva(this, service.buscarReservaPorId(id));
        } catch (RuntimeException ex) {
            ex.printStackTrace();
        }
    }

    public void deliver_reserva(Reserva e) {
        if (as != null) {
            try {
                aos.writeInt(Protocol.DELIVER_RESERVA);
                aos.writeObject(e);
                aos.flush();
                aos.reset();
            } catch (Exception ex) {
            }
        }
    }
}
