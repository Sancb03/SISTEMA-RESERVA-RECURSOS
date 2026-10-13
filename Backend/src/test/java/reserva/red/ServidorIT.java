package reserva.red;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reserva.Servidor;
import reserva.data.BaseDePruebas;
import reserva.logic.Categoria;
import reserva.logic.Recurso;
import reserva.logic.Reserva;
import reserva.logic.ServiceImpl;
import reserva.logic.Usuario;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Prueba de INTEGRACION de punta a punta del Backend: un Servidor de verdad
 * (sockets + hilos + ServiceImpl + MySQL de pruebas), al que se conectan
 * "Frontends" crudos hechos en la propia prueba con ObjectStreams.
 * Verifica el protocolo de peticiones y, sobre todo, las ALERTAS: cuando un
 * usuario crea una reserva, los demás conectados reciben la notificación y
 * el autor no.
 */
@DisplayName("Servidor: peticiones y alertas por sockets")
class ServidorIT {

    private static Servidor servidor;
    private static RegistroAlertas registro;
    private static int puerto;

    /** Un Frontend mínimo: abre un socket, dice para qué canal es y habla con ObjectStreams. */
    private static class ClienteCrudo implements AutoCloseable {
        final Socket socket;
        final ObjectOutputStream salida;
        final ObjectInputStream entrada;

        ClienteCrudo(String canal) throws IOException {
            socket = new Socket("localhost", puerto);
            salida = new ObjectOutputStream(socket.getOutputStream());
            salida.flush();
            entrada = new ObjectInputStream(socket.getInputStream());
            salida.writeObject(canal);
            salida.flush();
        }

        Respuesta pedir(String operacion, Object... parametros) throws Exception {
            salida.writeObject(new Peticion(operacion, parametros));
            salida.flush();
            salida.reset();
            return (Respuesta) entrada.readObject();
        }

        Notificacion esperarNotificacion(int milisegundos) throws Exception {
            socket.setSoTimeout(milisegundos);
            return (Notificacion) entrada.readObject();
        }

        @Override
        public void close() throws IOException {
            socket.close();
        }
    }

    @BeforeAll
    static void arrancarServidor() throws Exception {
        assumeTrue(BaseDePruebas.preparar(), "No hay MySQL disponible: se salta la prueba de integración");
        ServiceImpl service = new ServiceImpl();
        registro = new RegistroAlertas();
        service.agregarOyenteNuevaReserva(registro::difundirNuevaReserva);
        servidor = new Servidor(0, service, registro); // puerto 0: el sistema elige uno libre
        puerto = servidor.iniciar();
    }

    @AfterAll
    static void detenerServidor() {
        if (servidor != null) {
            servidor.detener();
        }
    }

    private static void esperarSuscriptores(int cantidad) throws InterruptedException {
        for (int i = 0; i < 100 && registro.cantidad() < cantidad; i++) {
            Thread.sleep(50);
        }
        assertEquals(cantidad, registro.cantidad(), "los Frontends deberían estar registrados para alertas");
    }

    @Test
    @DisplayName("Una Peticion llega al ServiceImpl y la Respuesta trae el Usuario de MySQL")
    void peticionLoginDeAdmin() throws Exception {
        try (ClienteCrudo cliente = new ClienteCrudo(Protocolo.CANAL_PETICIONES)) {
            Respuesta r = cliente.pedir("buscarUsuarioPorIdentificacion", "admin");

            assertTrue(r.isExito());
            Usuario admin = (Usuario) r.getResultado();
            assertEquals("ADMINISTRADOR", admin.getRol());
            assertEquals("admin", admin.getClave());
        }
    }

    @Test
    @DisplayName("Un error del Service (permisos) viaja como Respuesta de error con su tipo")
    void errorDePermisos() throws Exception {
        try (ClienteCrudo cliente = new ClienteCrudo(Protocolo.CANAL_PETICIONES)) {
            Usuario juan = (Usuario) cliente.pedir("buscarUsuarioPorIdentificacion", "111").getResultado();

            Respuesta r = cliente.pedir("listarFuncionarios", juan);

            assertFalse(r.isExito());
            assertEquals(SecurityException.class.getName(), r.getTipoError());
        }
    }

    @Test
    @DisplayName("Una misma conexión atiende muchas peticiones seguidas, y guardarCategoria devuelve el id asignado")
    void variasPeticionesEnLaMismaConexion() throws Exception {
        try (ClienteCrudo cliente = new ClienteCrudo(Protocolo.CANAL_PETICIONES)) {
            Usuario admin = (Usuario) cliente.pedir("buscarUsuarioPorIdentificacion", "admin").getResultado();

            Categoria nueva = new Categoria();
            nueva.setDescripcion("Creada por ServidorIT");
            Respuesta guardada = cliente.pedir("guardarCategoria", nueva, admin);
            assertEquals(true, guardada.getResultado());
            String id = ((Categoria) guardada.getEntidadActualizada()).getId();
            assertTrue(id.matches("CAT-\\d{6}"));

            @SuppressWarnings("unchecked")
            List<Categoria> todas = (List<Categoria>) cliente.pedir("listarCategorias", admin).getResultado();
            assertTrue(todas.stream().anyMatch(c -> c.getId().equals(id)));

            // el stream no debe "recordar" la versión vieja de un objeto que cambió
            admin.setNombre("Administrador cambiado");
            Usuario devuelto = (Usuario) cliente.pedir("buscarUsuarioPorIdentificacion", "admin").getResultado();
            assertEquals("Administrador", devuelto.getNombre());
        }
    }

    @Test
    @DisplayName("ALERTAS: al crear una reserva, los demás Frontends la reciben y el autor no")
    void alertasDeNuevaReserva() throws Exception {
        try (ClienteCrudo peticiones = new ClienteCrudo(Protocolo.CANAL_PETICIONES);
             ClienteCrudo alertasJuan = new ClienteCrudo(Protocolo.CANAL_ALERTAS);
             ClienteCrudo alertasMaria = new ClienteCrudo(Protocolo.CANAL_ALERTAS);
             ClienteCrudo alertasKeren = new ClienteCrudo(Protocolo.CANAL_ALERTAS)) {

            // cada Frontend dice quién es (id de usuario) por su canal de alertas
            alertasJuan.salida.writeObject(2);
            alertasJuan.salida.flush();
            alertasMaria.salida.writeObject(3);
            alertasMaria.salida.flush();
            alertasKeren.salida.writeObject(444);
            alertasKeren.salida.flush();
            esperarSuscriptores(3);

            // Juan (id 2) crea una reserva por el canal de peticiones
            Usuario juan = (Usuario) peticiones.pedir("buscarUsuarioPorIdentificacion", "111").getResultado();
            Usuario admin = (Usuario) peticiones.pedir("buscarUsuarioPorIdentificacion", "admin").getResultado();
            Categoria cat = new Categoria();
            cat.setDescripcion("Alertas");
            cat = (Categoria) peticiones.pedir("guardarCategoria", cat, admin).getEntidadActualizada();
            Recurso recurso = new Recurso();
            recurso.setId("ALERTA-1");
            recurso.setCategoria(cat);
            recurso.setDescripcion("Sala de alertas");
            assertEquals(true, peticiones.pedir("guardarRecurso", recurso, admin).getResultado());

            Reserva reserva = new Reserva();
            reserva.setActividad("Reunion clientes");
            reserva.setFecha(LocalDate.of(2027, 5, 20));
            reserva.setHoraInicio(LocalTime.of(8, 0));
            reserva.setHoraFin(LocalTime.of(10, 0));
            reserva.setFuncionario(juan);
            reserva.setRecursos(new ArrayList<>(List.of(recurso)));
            assertEquals(true, peticiones.pedir("guardarReserva", reserva).getResultado());

            // María y Keren la reciben, completa
            for (ClienteCrudo otro : List.of(alertasMaria, alertasKeren)) {
                Notificacion n = otro.esperarNotificacion(3000);
                assertEquals(Notificacion.NUEVA_RESERVA, n.getTipo());
                Reserva recibida = (Reserva) n.getDatos();
                assertEquals("Reunion clientes", recibida.getActividad());
                assertEquals("Juan Perez", recibida.getFuncionario().getNombre());
                assertEquals(LocalTime.of(10, 0), recibida.getHoraFin());
                assertEquals("Sala de alertas", recibida.getRecursos().get(0).getDescripcion());
                assertNull(recibida.getFuncionario().getClave(), "la clave de Juan no debe viajar en la alerta");
            }

            // Juan (el autor) no recibe nada
            assertThrows(SocketTimeoutException.class, () -> alertasJuan.esperarNotificacion(700));
        }
    }

    @Test
    @DisplayName("Un Frontend que se desconecta sale del registro de alertas")
    void desconexionSaleDelRegistro() throws Exception {
        // otras pruebas cierran sus conexiones al terminar: primero se espera a que el registro quede vacío
        for (int i = 0; i < 100 && registro.cantidad() > 0; i++) {
            Thread.sleep(50);
        }
        assertEquals(0, registro.cantidad());

        ClienteCrudo alertas = new ClienteCrudo(Protocolo.CANAL_ALERTAS);
        alertas.salida.writeObject(3);
        alertas.salida.flush();
        esperarSuscriptores(1);

        alertas.close();

        for (int i = 0; i < 100 && registro.cantidad() > 0; i++) {
            Thread.sleep(50);
        }
        assertEquals(0, registro.cantidad());
    }

    @Test
    @DisplayName("Un objeto de una clase ajena a la aplicación es rechazado (filtro de deserialización)")
    void filtroRechazaClasesAjenas() throws Exception {
        try (ClienteCrudo cliente = new ClienteCrudo(Protocolo.CANAL_PETICIONES)) {
            cliente.salida.writeObject(new java.io.File("/etc/passwd")); // java.io.File no está permitido
            cliente.salida.flush();
            cliente.socket.setSoTimeout(2000);
            // el Backend cierra esa conexión en vez de procesarla
            assertThrows(IOException.class, () -> cliente.entrada.readObject());
        }
    }
}
