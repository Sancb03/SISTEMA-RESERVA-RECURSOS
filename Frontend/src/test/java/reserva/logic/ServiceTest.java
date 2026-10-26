package reserva.logic;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reserva.presentation.SocketMonitor;

import javax.swing.SwingUtilities;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Prueba de UNIDAD del Proxy (el Service del Frontend): en vez del Backend real hay un servidor
 * falso, hecho aquí mismo con sockets, que habla el Protocol y contesta lo que la prueba le diga.
 * Así se comprueba, sin MySQL ni Backend, que el Service manda el código de operación y los
 * parámetros correctos, interpreta las respuestas y entrega las alertas.
 */
@DisplayName("Service (Proxy): traduce llamadas a códigos de Protocol y respuestas a resultados")
class ServiceTest {

    /** Lo que hace el servidor falso con cada operación: lee sus parámetros y escribe la respuesta. */
    interface Atencion {
        void atender(int operacion, ObjectInputStream in, ObjectOutputStream out) throws Exception;
    }

    private ServerSocket servidor;
    private Service service;
    private volatile Atencion atencion = (op, in, out) -> { out.writeInt(Protocol.ERROR_NO_ERROR); out.writeObject(null); };
    private final List<Integer> operaciones = new ArrayList<>();
    private final List<Object> parametros = new ArrayList<>();
    private final BlockingQueue<ObjectOutputStream> canalesAsync = new LinkedBlockingQueue<>();
    private final BlockingQueue<String> sidsAsync = new LinkedBlockingQueue<>();
    private int sesiones;

    @BeforeEach
    void arrancarServidorFalso() throws IOException {
        servidor = new ServerSocket(0);
        Thread aceptador = new Thread(() -> {
            try {
                while (!servidor.isClosed()) {
                    Socket cliente = servidor.accept();
                    Thread t = new Thread(() -> atender(cliente));
                    t.setDaemon(true);
                    t.start();
                }
            } catch (IOException ignorada) {
                // servidor cerrado al terminar la prueba
            }
        });
        aceptador.setDaemon(true);
        aceptador.start();
        service = new Service("localhost", servidor.getLocalPort());
    }

    @AfterEach
    void detener() throws IOException {
        service.stop();
        servidor.close();
    }

    private void atender(Socket cliente) {
        try (cliente) {
            ObjectOutputStream out = new ObjectOutputStream(cliente.getOutputStream());
            out.flush();
            ObjectInputStream in = new ObjectInputStream(cliente.getInputStream());
            int tipo = in.readInt();
            if (tipo == Protocol.SYNC) {
                synchronized (this) {
                    sesiones++;
                    out.writeObject("sid-" + sesiones);
                }
                out.flush();
                while (true) {
                    int op = in.readInt();
                    if (op == Protocol.DISCONNECT) {
                        return;
                    }
                    synchronized (operaciones) {
                        operaciones.add(op);
                    }
                    atencion.atender(op, in, out);
                    out.flush();
                    out.reset();
                }
            } else if (tipo == Protocol.ASYNC) {
                sidsAsync.add((String) in.readObject());
                canalesAsync.add(out);
                in.readInt(); // se queda esperando hasta que el cliente cierre
            }
        } catch (Exception ignorada) {
            // el cliente se desconectó (o la prueba simuló una caída): se cierra el socket y listo
        }
    }

    /** Respuesta correcta con un resultado, leyendo antes los parámetros que se esperan. */
    private Atencion responde(int cantidadDeParametros, Object resultado) {
        return (op, in, out) -> {
            for (int i = 0; i < cantidadDeParametros; i++) {
                synchronized (operaciones) {
                    parametros.add(in.readObject());
                }
            }
            out.writeInt(Protocol.ERROR_NO_ERROR);
            out.writeObject(resultado);
        };
    }

    private Atencion fallaCon(int cantidadDeParametros, int codigo, String mensaje) {
        return (op, in, out) -> {
            for (int i = 0; i < cantidadDeParametros; i++) {
                in.readObject();
            }
            out.writeInt(codigo);
            out.writeObject(mensaje);
        };
    }

    private static Usuario usuario(int id, String nombre) {
        Funcionario f = new Funcionario();
        f.setId(id);
        f.setNombre(nombre);
        return f;
    }

    @Test
    @DisplayName("Una llamada viaja como código de operación + parámetros y devuelve el resultado")
    void llamadaSeConvierteEnOperacion() {
        atencion = responde(1, usuario(2, "Juan Perez"));

        Usuario obtenido = service.buscarUsuarioPorIdentificacion("111");

        assertEquals(2, obtenido.getId());
        assertEquals("Juan Perez", obtenido.getNombre());
        assertEquals(List.of(Protocol.BUSCAR_USUARIO_POR_IDENTIFICACION), operaciones);
        assertEquals(List.of("111"), parametros);
    }

    @Test
    @DisplayName("Los métodos sin parámetros y los que devuelven listas también funcionan")
    void listaSinParametros() {
        List<Usuario> lista = new ArrayList<>(List.of(usuario(1, "A"), usuario(2, "B")));
        atencion = responde(0, lista);

        List<Usuario> obtenida = service.listarUsuarios();

        assertEquals(2, obtenida.size());
        assertEquals(List.of(Protocol.LISTAR_USUARIOS), operaciones);
        assertTrue(parametros.isEmpty());
    }

    @Test
    @DisplayName("Los parámetros primitivos y los booleanos de resultado se convierten bien")
    void primitivosYBooleanos() {
        atencion = responde(1, Boolean.TRUE);

        assertTrue(service.eliminarUsuario(7));

        assertEquals(List.of(Protocol.ELIMINAR_USUARIO), operaciones);
        assertEquals(List.of(7), parametros);
    }

    @Test
    @DisplayName("autenticar y cambiarClave viajan con sus parámetros")
    void autenticarYCambiarClave() {
        atencion = responde(2, usuario(1, "Admin"));
        assertEquals("Admin", service.autenticar("admin", "admin").getNombre());

        parametros.clear();
        atencion = responde(3, Boolean.TRUE);
        assertTrue(service.cambiarClave("admin", "admin", "nueva123"));

        assertEquals(List.of(Protocol.AUTENTICAR, Protocol.CAMBIAR_CLAVE), operaciones);
        assertEquals(List.of("admin", "admin", "nueva123"), parametros);
    }

    @Test
    @DisplayName("Un ERROR_SECURITY llega como SecurityException con el mensaje del Backend")
    void errorDePermisos() {
        atencion = fallaCon(1, Protocol.ERROR_SECURITY, "La clave es incorrecta.");

        SecurityException ex = assertThrows(SecurityException.class,
                () -> service.listarFuncionarios(usuario(2, "Juan")));

        assertEquals("La clave es incorrecta.", ex.getMessage());
    }

    @Test
    @DisplayName("ERROR_STATE y ERROR_ARGUMENT conservan su tipo; ERROR_ERROR es RuntimeException")
    void otrosTiposDeError() {
        atencion = fallaCon(1, Protocol.ERROR_STATE, "Ya existe");
        assertThrows(IllegalStateException.class, () -> service.eliminarUsuario(1));

        atencion = fallaCon(1, Protocol.ERROR_ARGUMENT, "Dato inválido");
        assertThrows(IllegalArgumentException.class, () -> service.eliminarUsuario(1));

        atencion = fallaCon(1, Protocol.ERROR_ERROR, "Error interno");
        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.eliminarUsuario(1));
        assertEquals("Error interno", ex.getMessage());
    }

    @Test
    @DisplayName("Después de un error del Backend la conexión sigue sirviendo para la siguiente llamada")
    void despuesDeUnErrorSigueFuncionando() {
        atencion = fallaCon(1, Protocol.ERROR_STATE, "Ya existe");
        assertThrows(IllegalStateException.class, () -> service.eliminarUsuario(1));

        atencion = responde(1, Boolean.TRUE);
        assertTrue(service.eliminarUsuario(1));
        assertEquals(1, sesiones, "no debió abrir otra sesión");
    }

    @Test
    @DisplayName("guardarCategoria: el id que asignó el Backend queda en el objeto original del Controller")
    void guardarCopiaElIdAsignado() {
        atencion = (op, in, out) -> {
            in.readObject();
            in.readObject();
            Categoria conId = new Categoria();
            conId.setId("CAT-000007");
            conId.setDescripcion("Salas");
            out.writeInt(Protocol.ERROR_NO_ERROR);
            out.writeObject(Boolean.TRUE);
            out.writeObject(conId);
        };
        Categoria categoria = new Categoria();
        categoria.setDescripcion("Salas");

        boolean ok = service.guardarCategoria(categoria, usuario(1, "Admin"));

        assertTrue(ok);
        assertEquals("CAT-000007", categoria.getId());
        assertEquals(List.of(Protocol.GUARDAR_CATEGORIA), operaciones);
    }

    @Test
    @DisplayName("Al conectar, el Backend entrega el sid de la sesión")
    void recibeElSid() throws Exception {
        service.conectar();

        assertEquals("sid-1", service.getSid());
    }

    @Test
    @DisplayName("Si el Backend no está, el error explica a dónde se intentó conectar")
    void servidorCaido() throws IOException {
        int puertoMuerto = servidor.getLocalPort();
        servidor.close();
        Service sinServidor = new Service("localhost", puertoMuerto);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> sinServidor.listarUsuarios());

        assertTrue(ex.getMessage().contains("localhost:" + puertoMuerto), ex.getMessage());
    }

    @Test
    @DisplayName("Si se cae la conexión, la siguiente llamada reconecta sola")
    void reconexion() {
        atencion = responde(1, usuario(1, "A"));
        assertEquals("A", service.buscarUsuarioPorId(1).getNombre());

        Atencion normal = atencion;
        atencion = (op, in, out) -> {
            in.readObject();
            throw new RuntimeException("simula caída"); // el hilo del servidor falso muere y cierra el socket
        };
        assertThrows(RuntimeException.class, () -> service.buscarUsuarioPorId(1));

        atencion = normal;
        assertEquals("A", service.buscarUsuarioPorId(1).getNombre());
        assertEquals(2, sesiones, "la reconexión abre una sesión nueva");
    }

    @Test
    @DisplayName("stop() le avisa al Backend con DISCONNECT y cierra la conexión")
    void stopCierra() throws Exception {
        service.conectar();
        assertNotNull(service.s);

        service.stop();

        assertNull(service.s);
    }

    @Test
    @DisplayName("ALERTAS: el SocketMonitor se une con el sid y entrega cada reserva nueva al listener en el hilo de Swing")
    void alertas() throws Exception {
        service.conectar();
        BlockingQueue<Reserva> recibidas = new LinkedBlockingQueue<>();
        BlockingQueue<Boolean> enSwing = new LinkedBlockingQueue<>();

        SocketMonitor monitor = new SocketMonitor(r -> {
            enSwing.add(SwingUtilities.isEventDispatchThread());
            recibidas.add(r);
        }, service.getSid(), "localhost", servidor.getLocalPort());
        monitor.start();

        assertEquals("sid-1", sidsAsync.poll(3, TimeUnit.SECONDS));
        ObjectOutputStream canal = canalesAsync.poll(3, TimeUnit.SECONDS);
        assertNotNull(canal);

        Reserva reserva = new Reserva();
        reserva.setActividad("Reunion clientes");
        reserva.setFecha(LocalDate.of(2027, 5, 20));
        reserva.setHoraInicio(LocalTime.of(8, 0));
        reserva.setHoraFin(LocalTime.of(10, 0));
        reserva.setFuncionario(usuario(2, "Juan Perez"));
        canal.writeInt(Protocol.DELIVER_RESERVA);
        canal.writeObject(reserva);
        canal.flush();

        Reserva llegada = recibidas.poll(3, TimeUnit.SECONDS);
        assertNotNull(llegada, "el listener debería recibir la alerta");
        assertEquals("Reunion clientes", llegada.getActividad());
        assertEquals("Juan Perez", llegada.getFuncionario().getNombre());
        assertEquals(LocalTime.of(10, 0), llegada.getHoraFin());
        assertEquals(Boolean.TRUE, enSwing.poll(1, TimeUnit.SECONDS), "debe entregarse en el hilo de Swing");
        monitor.stop();
    }

    @Test
    @DisplayName("monitor.stop() cierra la conexión asincrónica y el Backend deja de poder escribirle")
    void detenerAlertas() throws Exception {
        service.conectar();
        SocketMonitor monitor = new SocketMonitor(r -> { }, service.getSid(), "localhost", servidor.getLocalPort());
        monitor.start();
        ObjectOutputStream canal = canalesAsync.poll(3, TimeUnit.SECONDS);
        assertNotNull(canal);

        monitor.stop();

        // escribir en un socket cerrado por el otro lado termina fallando
        boolean fallo = false;
        for (int i = 0; i < 50 && !fallo; i++) {
            try {
                canal.writeInt(Protocol.DELIVER_RESERVA);
                canal.writeObject(new Reserva());
                canal.flush();
                canal.reset();
                Thread.sleep(50);
            } catch (IOException ex) {
                fallo = true;
            }
        }
        assertTrue(fallo, "la conexión asincrónica debería haberse cerrado");
    }
}
