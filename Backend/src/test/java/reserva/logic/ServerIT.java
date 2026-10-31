package reserva.logic;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reserva.data.BaseDePruebas;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Prueba de INTEGRACION de punta a punta del Backend: Server + Worker + Service + DAOs + MySQL
 * (base "reservarecursos_test"), con clientes que hablan el Protocol por sockets reales.
 * Si no hay MySQL corriendo, la clase se salta con un aviso.
 */
@DisplayName("Server + MySQL: login, reservas, choques de horario y alertas por sockets")
class ServerIT {

    private static final AtomicInteger contador = new AtomicInteger();

    private static Server server;

    @BeforeAll
    static void arrancar() {
        assumeTrue(BaseDePruebas.preparar(), "No hay MySQL disponible: se salta la prueba de integración");
        server = new Server(0, new Service());
        Thread t = new Thread(server::run);
        t.setDaemon(true);
        t.start();
    }

    @AfterAll
    static void detener() {
        if (server != null) server.stop();
    }

    private static Usuario autenticarAdmin(ClienteDePrueba c) throws Exception {
        ClienteDePrueba.Resultado r = c.pedir(Protocol.AUTENTICAR, "admin", "admin");
        assertEquals(Protocol.ERROR_NO_ERROR, r.codigo());
        return (Usuario) r.valor();
    }

    @Test
    @DisplayName("AUTENTICAR: clave correcta devuelve el usuario sin la clave; incorrecta, ERROR_SECURITY")
    void login() throws Exception {
        try (ClienteDePrueba c = new ClienteDePrueba(server.getPort())) {
            Usuario admin = autenticarAdmin(c);
            assertEquals("admin", admin.getIdentificacion());
            assertNull(admin.getClave());

            ClienteDePrueba.Resultado mala = c.pedir(Protocol.AUTENTICAR, "admin", "mala");
            assertEquals(Protocol.ERROR_SECURITY, mala.codigo());
            assertEquals("La clave es incorrecta.", mala.valor());

            assertEquals(Protocol.ERROR_ARGUMENT, c.pedir(Protocol.AUTENTICAR, "no-existe-xyz", "1").codigo());
        }
    }

    @Test
    @DisplayName("Un funcionario no puede listar funcionarios (ERROR_SECURITY)")
    void permisos() throws Exception {
        try (ClienteDePrueba c = new ClienteDePrueba(server.getPort())) {
            Usuario juan = (Usuario) c.pedir(Protocol.BUSCAR_USUARIO_POR_IDENTIFICACION, "111").valor();

            assertEquals(Protocol.ERROR_SECURITY, c.pedir(Protocol.LISTAR_FUNCIONARIOS, juan).codigo());
        }
    }

    @Test
    @DisplayName("Reservar: el otro Frontend recibe la alerta, el autor no, y un choque de horario da ERROR_STATE")
    void reservaConAlertaYChoque() throws Exception {
        try (ClienteDePrueba juanCliente = new ClienteDePrueba(server.getPort());
             ClienteDePrueba mariaCliente = new ClienteDePrueba(server.getPort())) {
            Usuario admin = autenticarAdmin(juanCliente);
            Usuario juan = (Usuario) juanCliente.pedir(Protocol.BUSCAR_USUARIO_POR_IDENTIFICACION, "111").valor();
            juanCliente.iniciarAlertas();
            mariaCliente.iniciarAlertas();
            Thread.sleep(300);

            // una categoría y un recurso propios de esta prueba
            Categoria categoria = new Categoria();
            categoria.setDescripcion("Cat-ServerIT-" + contador.incrementAndGet());
            ClienteDePrueba.Resultado rc = juanCliente.pedirGuardar(Protocol.GUARDAR_CATEGORIA, categoria, admin);
            assertEquals(Boolean.TRUE, rc.valor());
            categoria = (Categoria) rc.entidad();
            assertNotNull(categoria.getId());

            Recurso recurso = new Recurso();
            recurso.setId("SIT-" + System.nanoTime());
            recurso.setCategoria(categoria);
            recurso.setDescripcion("Sala de la prueba");
            assertEquals(Boolean.TRUE, juanCliente.pedirGuardar(Protocol.GUARDAR_RECURSO, recurso, admin).valor());

            Reserva reserva = new Reserva();
            reserva.setActividad("Reunion por sockets");
            reserva.setFecha(LocalDate.of(2031, 3, 4));
            reserva.setHoraInicio(LocalTime.of(9, 0));
            reserva.setHoraFin(LocalTime.of(10, 0));
            reserva.setFuncionario(juan);
            reserva.setRecursos(new ArrayList<>(List.of(recurso)));

            ClienteDePrueba.Resultado guardada = juanCliente.pedirGuardar(Protocol.GUARDAR_RESERVA, reserva);
            assertEquals(Boolean.TRUE, guardada.valor());
            Reserva conId = (Reserva) guardada.entidad();
            assertTrue(conId.getId().startsWith("RES-"), conId.getId());

            Reserva alerta = mariaCliente.esperarAlerta(3000);
            assertNotNull(alerta, "María debería recibir la alerta");
            assertEquals("Reunion por sockets", alerta.getActividad());
            assertEquals(conId.getId(), alerta.getId());
            assertEquals(1, alerta.getRecursos().size());
            assertNull(juanCliente.esperarAlerta(500), "Juan no debe recibir su propia alerta");

            // otra reserva que choca en el mismo recurso
            Reserva choca = new Reserva();
            choca.setActividad("Choca");
            choca.setFecha(LocalDate.of(2031, 3, 4));
            choca.setHoraInicio(LocalTime.of(9, 30));
            choca.setHoraFin(LocalTime.of(11, 0));
            choca.setFuncionario(juan);
            choca.setRecursos(new ArrayList<>(List.of(recurso)));
            assertEquals(Protocol.ERROR_STATE, mariaCliente.pedir(Protocol.GUARDAR_RESERVA, choca).codigo());
            assertNull(juanCliente.esperarAlerta(500), "una reserva rechazada no genera alertas");

            // limpieza
            assertEquals(Boolean.TRUE, juanCliente.pedir(Protocol.ELIMINAR_RESERVA, conId.getId()).valor());
        }
    }
}
