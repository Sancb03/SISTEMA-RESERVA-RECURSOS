package reserva.logic;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Prueba de UNIDAD del Server y los Worker: se levanta el Server de verdad (puerto libre) pero con
 * un IService falso, así que no hace falta MySQL. Un cliente de prueba habla el Protocol por sockets.
 */
@DisplayName("Server + Worker: Protocol por sockets, errores, sesiones y alertas")
class ServerTest {

    private Server server;
    private final List<String> llamadas = new CopyOnWriteArrayList<>();
    private final List<Object> argumentos = new CopyOnWriteArrayList<>();
    /** Qué hace el Service falso; por defecto true para boolean y lista vacía para List. */
    private volatile BiFunction<String, Object[], Object> comportamiento = (m, a) -> null;
    private final List<ClienteDePrueba> clientes = new ArrayList<>();

    @BeforeEach
    void arrancar() {
        InvocationHandler handler = (proxy, method, args) -> {
            llamadas.add(method.getName());
            if (args != null) argumentos.addAll(java.util.Arrays.asList(args));
            Object r = comportamiento.apply(method.getName(), args);
            if (r != null) return r;
            return porDefecto(method);
        };
        IService falso = (IService) Proxy.newProxyInstance(IService.class.getClassLoader(),
                new Class<?>[]{IService.class}, handler);
        server = new Server(0, falso);
        Thread t = new Thread(server::run);
        t.setDaemon(true);
        t.start();
    }

    private static Object porDefecto(Method m) {
        if (m.getReturnType() == boolean.class) return Boolean.TRUE;
        if (List.class.isAssignableFrom(m.getReturnType())) return new ArrayList<>();
        return null;
    }

    @AfterEach
    void detener() {
        for (ClienteDePrueba c : clientes) c.close();
        server.stop();
    }

    private ClienteDePrueba cliente() throws Exception {
        ClienteDePrueba c = new ClienteDePrueba(server.getPort());
        clientes.add(c);
        return c;
    }

    private static Usuario usuario(int id, String nombre) {
        Funcionario f = new Funcionario();
        f.setId(id);
        f.setNombre(nombre);
        return f;
    }

    private static Reserva reserva(String id, String actividad) {
        Reserva r = new Reserva();
        r.setId(id);
        r.setActividad(actividad);
        r.setFecha(LocalDate.of(2027, 5, 20));
        r.setHoraInicio(LocalTime.of(8, 0));
        r.setHoraFin(LocalTime.of(10, 0));
        r.setFuncionario(usuario(2, "Juan Perez"));
        return r;
    }

    private static void esperarHasta(java.util.function.BooleanSupplier condicion) throws InterruptedException {
        for (int i = 0; i < 100 && !condicion.getAsBoolean(); i++) Thread.sleep(50);
    }

    @Test
    @DisplayName("SYNC: el Server crea un Worker para el Frontend y le devuelve un sid")
    void syncDevuelveSid() throws Exception {
        ClienteDePrueba c = cliente();

        assertNotNull(c.sid);
        assertEquals(1, server.cantidadDeSesiones());
    }

    @Test
    @DisplayName("Una operación llega al Service con sus parámetros y el resultado vuelve al Frontend")
    void operacionConParametros() throws Exception {
        comportamiento = (m, a) -> m.equals("buscarUsuarioPorIdentificacion") ? usuario(2, "Juan Perez") : null;
        ClienteDePrueba c = cliente();

        ClienteDePrueba.Resultado r = c.pedir(Protocol.BUSCAR_USUARIO_POR_IDENTIFICACION, "111");

        assertEquals(Protocol.ERROR_NO_ERROR, r.codigo());
        assertEquals("Juan Perez", ((Usuario) r.valor()).getNombre());
        assertEquals(List.of("buscarUsuarioPorIdentificacion"), llamadas);
        assertEquals(List.of("111"), argumentos);
    }

    @Test
    @DisplayName("Los parámetros int y las listas funcionan")
    void enterosYListas() throws Exception {
        ClienteDePrueba c = cliente();

        ClienteDePrueba.Resultado eliminar = c.pedir(Protocol.ELIMINAR_USUARIO, 5);
        ClienteDePrueba.Resultado lista = c.pedir(Protocol.LISTAR_USUARIOS);

        assertEquals(Boolean.TRUE, eliminar.valor());
        assertEquals(Integer.valueOf(5), argumentos.get(0));
        assertTrue(((List<?>) lista.valor()).isEmpty());
    }

    @Test
    @DisplayName("Cada tipo de excepción del Service viaja como su propio código de error, con el mensaje")
    void erroresConTipo() throws Exception {
        ClienteDePrueba c = cliente();

        comportamiento = (m, a) -> { throw new SecurityException("Solo el administrador"); };
        ClienteDePrueba.Resultado seg = c.pedir(Protocol.LISTAR_FUNCIONARIOS, usuario(2, "Juan"));
        assertEquals(Protocol.ERROR_SECURITY, seg.codigo());
        assertEquals("Solo el administrador", seg.valor());

        comportamiento = (m, a) -> { throw new IllegalStateException("Ya existe"); };
        assertEquals(Protocol.ERROR_STATE, c.pedir(Protocol.ELIMINAR_USUARIO, 1).codigo());

        comportamiento = (m, a) -> { throw new IllegalArgumentException("Dato inválido"); };
        assertEquals(Protocol.ERROR_ARGUMENT, c.pedir(Protocol.ELIMINAR_USUARIO, 1).codigo());

        comportamiento = (m, a) -> { throw new RuntimeException("Error interno"); };
        ClienteDePrueba.Resultado raro = c.pedir(Protocol.ELIMINAR_USUARIO, 1);
        assertEquals(Protocol.ERROR_ERROR, raro.codigo());
        assertEquals("Error interno", raro.valor());
    }

    @Test
    @DisplayName("Después de un error la sesión sigue viva y atiende la siguiente petición")
    void sigueViva() throws Exception {
        ClienteDePrueba c = cliente();
        comportamiento = (m, a) -> { throw new IllegalStateException("Ya existe"); };
        assertEquals(Protocol.ERROR_STATE, c.pedir(Protocol.ELIMINAR_USUARIO, 1).codigo());

        comportamiento = (m, a) -> null;
        assertEquals(Protocol.ERROR_NO_ERROR, c.pedir(Protocol.ELIMINAR_USUARIO, 1).codigo());
    }

    @Test
    @DisplayName("guardar*: la respuesta trae la entidad con el id que le puso el Service")
    void guardarDevuelveLaEntidad() throws Exception {
        comportamiento = (m, a) -> {
            ((Categoria) a[0]).setId("CAT-000009");
            return Boolean.TRUE;
        };
        ClienteDePrueba c = cliente();
        Categoria categoria = new Categoria();
        categoria.setDescripcion("Salas");

        ClienteDePrueba.Resultado r = c.pedirGuardar(Protocol.GUARDAR_CATEGORIA, categoria, usuario(1, "Admin"));

        assertEquals(Boolean.TRUE, r.valor());
        assertEquals("CAT-000009", ((Categoria) r.entidad()).getId());
    }

    @Test
    @DisplayName("ALERTAS: una reserva nueva se avisa a los DEMÁS Frontend, no a quien la hizo")
    void alertasANoAutores() throws Exception {
        comportamiento = (m, a) -> m.equals("buscarReservaPorId") ? reserva("RES-000001", "Reunion clientes") : null;
        ClienteDePrueba autor = cliente();
        ClienteDePrueba otro = cliente();
        ClienteDePrueba sinAlertas = cliente(); // no abrió su conexión asincrónica: no debe estorbar
        autor.iniciarAlertas();
        otro.iniciarAlertas();
        esperarHasta(() -> true);
        Thread.sleep(300); // que el Server termine de unir los sockets asincrónicos

        ClienteDePrueba.Resultado r = autor.pedirGuardar(Protocol.GUARDAR_RESERVA, reserva(null, "Reunion clientes"));

        assertEquals(Boolean.TRUE, r.valor());
        Reserva alerta = otro.esperarAlerta(3000);
        assertNotNull(alerta, "el otro Frontend debería recibir la alerta");
        assertEquals("Reunion clientes", alerta.getActividad());
        assertEquals("Juan Perez", alerta.getFuncionario().getNombre());
        assertNull(autor.esperarAlerta(500), "el autor no debe recibir su propia alerta");
        assertNotNull(sinAlertas.sid);
    }

    @Test
    @DisplayName("Si guardarReserva devuelve false no se avisa a nadie")
    void sinAlertaSiNoSeGuardo() throws Exception {
        comportamiento = (m, a) -> m.equals("guardarReserva") ? Boolean.FALSE : null;
        ClienteDePrueba autor = cliente();
        ClienteDePrueba otro = cliente();
        otro.iniciarAlertas();
        Thread.sleep(300);

        autor.pedirGuardar(Protocol.GUARDAR_RESERVA, reserva(null, "No se guardó"));

        assertNull(otro.esperarAlerta(700));
    }

    @Test
    @DisplayName("Si guardarReserva falla (choque de horario) tampoco se avisa y el autor recibe ERROR_STATE")
    void sinAlertaSiHuboError() throws Exception {
        comportamiento = (m, a) -> {
            if (m.equals("guardarReserva")) throw new IllegalStateException("El recurso ya está reservado");
            return null;
        };
        ClienteDePrueba autor = cliente();
        ClienteDePrueba otro = cliente();
        otro.iniciarAlertas();
        Thread.sleep(300);

        ClienteDePrueba.Resultado r = autor.pedir(Protocol.GUARDAR_RESERVA, reserva(null, "Choca"));

        assertEquals(Protocol.ERROR_STATE, r.codigo());
        assertNull(otro.esperarAlerta(700));
    }

    @Test
    @DisplayName("DISCONNECT: el Server quita la sesión de su lista")
    void disconnectQuitaLaSesion() throws Exception {
        ClienteDePrueba c = cliente();
        assertEquals(1, server.cantidadDeSesiones());

        c.desconectar();

        esperarHasta(() -> server.cantidadDeSesiones() == 0);
        assertEquals(0, server.cantidadDeSesiones());
    }

    @Test
    @DisplayName("Si el Frontend se cae sin avisar, el Server también quita la sesión")
    void caidaSinAviso() throws Exception {
        ClienteDePrueba c = cliente();
        c.close();

        esperarHasta(() -> server.cantidadDeSesiones() == 0);
        assertEquals(0, server.cantidadDeSesiones());
    }

    @Test
    @DisplayName("Un código de operación desconocido cierra la sesión")
    void operacionDesconocida() throws Exception {
        ClienteDePrueba c = cliente();

        c.os.writeInt(999);
        c.os.flush();

        assertThrows(IOException.class, () -> c.is.readInt());
        esperarHasta(() -> server.cantidadDeSesiones() == 0);
        assertEquals(0, server.cantidadDeSesiones());
    }

    @Test
    @DisplayName("Un objeto de una clase no permitida es rechazado y la sesión se cierra (filtro de deserialización)")
    void objetoNoPermitido() throws Exception {
        ClienteDePrueba c = cliente();

        c.os.writeInt(Protocol.ELIMINAR_USUARIO);
        c.os.writeObject(new AtomicInteger(5)); // java.util.concurrent.atomic no está en la lista permitida
        c.os.flush();

        assertThrows(IOException.class, () -> c.is.readInt());
        assertTrue(llamadas.isEmpty(), "el Service no debió ser invocado");
    }
}
