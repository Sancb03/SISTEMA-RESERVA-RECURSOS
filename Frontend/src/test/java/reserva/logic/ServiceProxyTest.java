package reserva.logic;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reserva.red.Notificacion;
import reserva.red.Peticion;
import reserva.red.Protocolo;
import reserva.red.Respuesta;

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
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Prueba de UNIDAD del Proxy: en vez del Backend real hay un servidor falso, hecho aquí
 * mismo con sockets, que anota las Peticiones que recibe y contesta lo que la prueba le diga.
 * Así se comprueba, sin MySQL ni Backend, que el Proxy traduce cada llamada a una Peticion,
 * interpreta la Respuesta (resultado, errores, entidad actualizada) y entrega las alertas.
 */
@DisplayName("ServiceProxy: traduce llamadas a Peticiones y Respuestas a resultados")
class ServiceProxyTest {

    private ServerSocket servidor;
    private ServiceProxy proxy;
    private final List<Peticion> recibidas = new CopyOnWriteArrayList<>();
    private volatile Function<Peticion, Respuesta> contestador = p -> Respuesta.exito(null);
    private final BlockingQueue<ObjectOutputStream> canalesDeAlertas = new LinkedBlockingQueue<>();
    private final BlockingQueue<Integer> usuariosDeAlertas = new LinkedBlockingQueue<>();

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
        proxy = new ServiceProxy("localhost", servidor.getLocalPort());
    }

    @AfterEach
    void detener() throws IOException {
        proxy.stop();
        servidor.close();
    }

    private void atender(Socket cliente) {
        try (cliente) {
            ObjectOutputStream salida = new ObjectOutputStream(cliente.getOutputStream());
            salida.flush();
            ObjectInputStream entrada = new ObjectInputStream(cliente.getInputStream());
            String canal = (String) entrada.readObject();
            if (Protocolo.CANAL_ALERTAS.equals(canal)) {
                usuariosDeAlertas.add((Integer) entrada.readObject());
                canalesDeAlertas.add(salida);
                entrada.readObject(); // se queda esperando hasta que el cliente cierre
            } else {
                while (true) {
                    Peticion p = (Peticion) entrada.readObject();
                    recibidas.add(p);
                    salida.writeObject(contestador.apply(p));
                    salida.flush();
                    salida.reset();
                }
            }
        } catch (IOException | ClassNotFoundException | RuntimeException ignorada) {
            // el cliente se desconectó (o la prueba simuló una caída): se cierra el socket y listo
        }
    }

    private static Usuario usuario(int id, String nombre) {
        Funcionario f = new Funcionario();
        f.setId(id);
        f.setNombre(nombre);
        return f;
    }

    @Test
    @DisplayName("Una llamada al Service viaja como Peticion con su operación y parámetros, y devuelve el resultado")
    void llamadaSeConvierteEnPeticion() {
        Usuario esperado = usuario(2, "Juan Perez");
        contestador = p -> Respuesta.exito(esperado);

        Usuario obtenido = proxy.buscarUsuarioPorIdentificacion("111");

        assertEquals(2, obtenido.getId());
        assertEquals("Juan Perez", obtenido.getNombre());
        assertEquals(1, recibidas.size());
        assertEquals("buscarUsuarioPorIdentificacion", recibidas.get(0).getOperacion());
        assertEquals(List.of("111"), List.of(recibidas.get(0).getParametros()));
    }

    @Test
    @DisplayName("Los métodos sin parámetros y los que devuelven listas también funcionan")
    void listaSinParametros() {
        List<Usuario> lista = new ArrayList<>(List.of(usuario(1, "A"), usuario(2, "B")));
        contestador = p -> Respuesta.exito(lista);

        List<Usuario> obtenida = proxy.listarUsuarios();

        assertEquals(2, obtenida.size());
        assertEquals(0, recibidas.get(0).getParametros().length);
    }

    @Test
    @DisplayName("Un error SecurityException del Backend llega como SecurityException")
    void errorDePermisos() {
        contestador = p -> Respuesta.error(SecurityException.class.getName(), "Solo el administrador");

        SecurityException ex = assertThrows(SecurityException.class,
                () -> proxy.listarFuncionarios(usuario(2, "Juan")));

        assertEquals("Solo el administrador", ex.getMessage());
    }

    @Test
    @DisplayName("IllegalStateException e IllegalArgumentException conservan su tipo; lo demás es RuntimeException")
    void otrosTiposDeError() {
        contestador = p -> Respuesta.error(IllegalStateException.class.getName(), "Ya existe");
        assertThrows(IllegalStateException.class, () -> proxy.eliminarUsuario(1));

        contestador = p -> Respuesta.error(IllegalArgumentException.class.getName(), "Dato inválido");
        assertThrows(IllegalArgumentException.class, () -> proxy.eliminarUsuario(1));

        contestador = p -> Respuesta.error(RuntimeException.class.getName(), "Error interno");
        RuntimeException ex = assertThrows(RuntimeException.class, () -> proxy.eliminarUsuario(1));
        assertEquals("Error interno", ex.getMessage());
    }

    @Test
    @DisplayName("guardarCategoria: el id que asignó el Backend queda en el objeto original del Controller")
    void guardarCopiaElIdAsignado() {
        contestador = p -> {
            Categoria conId = new Categoria();
            conId.setId("CAT-000007");
            conId.setDescripcion("Salas");
            return Respuesta.exito(true, conId);
        };
        Categoria categoria = new Categoria();
        categoria.setDescripcion("Salas");

        boolean ok = proxy.guardarCategoria(categoria, usuario(1, "Admin"));

        assertTrue(ok);
        assertEquals("CAT-000007", categoria.getId());
    }

    @Test
    @DisplayName("Si el Backend no está, el error explica a dónde se intentó conectar")
    void servidorCaido() throws IOException {
        int puertoMuerto = servidor.getLocalPort();
        servidor.close();
        ServiceProxy sinServidor = new ServiceProxy("localhost", puertoMuerto);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> sinServidor.listarUsuarios());

        assertTrue(ex.getMessage().contains("localhost:" + puertoMuerto), ex.getMessage());
    }

    @Test
    @DisplayName("Si se cae la conexión, la siguiente llamada reconecta sola")
    void reconexion() throws Exception {
        contestador = p -> Respuesta.exito(usuario(1, "A"));
        assertEquals("A", proxy.buscarUsuarioPorId(1).getNombre());

        // el Backend corta la conexión a mitad de la pregunta
        Function<Peticion, Respuesta> normal = contestador;
        contestador = p -> {
            throw new RuntimeException("simula caída"); // el hilo del servidor falso muere y cierra el socket
        };
        assertThrows(RuntimeException.class, () -> proxy.buscarUsuarioPorId(1));

        contestador = normal;
        assertEquals("A", proxy.buscarUsuarioPorId(1).getNombre());
    }

    @Test
    @DisplayName("ALERTAS: el Proxy se registra con el id del usuario y entrega cada reserva nueva al listener")
    void alertas() throws Exception {
        BlockingQueue<Reserva> recibidasPorListener = new LinkedBlockingQueue<>();

        proxy.iniciarAlertas(usuario(3, "Maria"), recibidasPorListener::add);

        assertEquals(Integer.valueOf(3), usuariosDeAlertas.poll(3, TimeUnit.SECONDS));
        ObjectOutputStream canal = canalesDeAlertas.poll(3, TimeUnit.SECONDS);
        assertNotNull(canal);

        Reserva reserva = new Reserva();
        reserva.setActividad("Reunion clientes");
        reserva.setFecha(LocalDate.of(2027, 5, 20));
        reserva.setHoraInicio(LocalTime.of(8, 0));
        reserva.setHoraFin(LocalTime.of(10, 0));
        reserva.setFuncionario(usuario(2, "Juan Perez"));
        canal.writeObject(new Notificacion(Notificacion.NUEVA_RESERVA, reserva));
        canal.flush();

        Reserva llegada = recibidasPorListener.poll(3, TimeUnit.SECONDS);
        assertNotNull(llegada, "el listener debería recibir la alerta");
        assertEquals("Reunion clientes", llegada.getActividad());
        assertEquals("Juan Perez", llegada.getFuncionario().getNombre());
        assertEquals(LocalTime.of(10, 0), llegada.getHoraFin());
    }

    @Test
    @DisplayName("detenerAlertas cierra el canal y el Backend deja de ver al suscriptor")
    void detenerAlertas() throws Exception {
        proxy.iniciarAlertas(usuario(3, "Maria"), r -> { });
        ObjectOutputStream canal = canalesDeAlertas.poll(3, TimeUnit.SECONDS);
        assertNotNull(canal);

        proxy.detenerAlertas();

        // escribir en un socket cerrado por el otro lado termina fallando
        boolean fallo = false;
        for (int i = 0; i < 50 && !fallo; i++) {
            try {
                canal.writeObject(new Notificacion(Notificacion.NUEVA_RESERVA, new Reserva()));
                canal.flush();
                canal.reset();
                Thread.sleep(50);
            } catch (IOException ex) {
                fallo = true;
            }
        }
        assertTrue(fallo, "el canal de alertas debería haberse cerrado");
    }
}
