package reserva.red;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reserva.logic.Categoria;
import reserva.logic.Funcionario;
import reserva.logic.Recurso;
import reserva.logic.Reserva;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de UNIDAD del protocolo: todo lo que viaja por el socket tiene que
 * sobrevivir a ObjectOutputStream/ObjectInputStream sin perder datos
 * (si alguna clase de dominio dejara de ser Serializable, esto falla).
 */
@DisplayName("Protocolo: Peticion, Respuesta y Notificacion viajan completas")
class PeticionRespuestaTest {

    @SuppressWarnings("unchecked")
    private static <T> T viajar(T objeto) throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bytes)) {
            out.writeObject(objeto);
        }
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
            return (T) in.readObject();
        }
    }

    private static Reserva reservaDeEjemplo() {
        Categoria sala = new Categoria();
        sala.setId("CAT-000001");
        sala.setDescripcion("Sala");

        Recurso recurso = new Recurso();
        recurso.setId("1001");
        recurso.setCategoria(sala);
        recurso.setDescripcion("Sala de 10 personas");

        Funcionario juan = new Funcionario();
        juan.setId(2);
        juan.setIdentificacion("111");
        juan.setNombre("Juan Perez");
        juan.setTelefono("3323");

        Reserva reserva = new Reserva();
        reserva.setId("RES-000007");
        reserva.setActividad("Reunion clientes");
        reserva.setFecha(LocalDate.of(2026, 11, 3));
        reserva.setHoraInicio(LocalTime.of(8, 0));
        reserva.setHoraFin(LocalTime.of(10, 30));
        reserva.setFuncionario(juan);
        reserva.setRecursos(new ArrayList<>(List.of(recurso)));
        return reserva;
    }

    @Test
    @DisplayName("Una Peticion con una Reserva adentro llega igual")
    void peticionConReserva() throws Exception {
        Peticion recibida = viajar(new Peticion("guardarReserva", reservaDeEjemplo()));

        assertEquals("guardarReserva", recibida.getOperacion());
        assertEquals(1, recibida.getParametros().length);

        Reserva reserva = (Reserva) recibida.getParametros()[0];
        assertEquals("RES-000007", reserva.getId());
        assertEquals(LocalDate.of(2026, 11, 3), reserva.getFecha());
        assertEquals(LocalTime.of(10, 30), reserva.getHoraFin());
        assertEquals("Juan Perez", reserva.getFuncionario().getNombre());
        assertEquals("FUNCIONARIO", reserva.getFuncionario().getRol());
        assertEquals("Sala", reserva.getRecursos().get(0).getCategoria().getDescripcion());
    }

    @Test
    @DisplayName("Una Respuesta exitosa trae el resultado; una de error trae tipo y mensaje")
    void respuestas() throws Exception {
        Respuesta ok = viajar(Respuesta.exito(new ArrayList<>(List.of("a", "b"))));
        assertTrue(ok.isExito());
        assertEquals(List.of("a", "b"), ok.getResultado());

        Respuesta error = viajar(Respuesta.error("java.lang.SecurityException", "Solo un administrador"));
        assertFalse(error.isExito());
        assertEquals("java.lang.SecurityException", error.getTipoError());
        assertEquals("Solo un administrador", error.getMensajeError());
        assertNull(error.getResultado());
    }

    @Test
    @DisplayName("Una Notificacion de NUEVA_RESERVA lleva la reserva completa")
    void notificacion() throws Exception {
        Notificacion recibida = viajar(new Notificacion(Notificacion.NUEVA_RESERVA, reservaDeEjemplo()));

        assertEquals(Notificacion.NUEVA_RESERVA, recibida.getTipo());
        assertEquals("Reunion clientes", ((Reserva) recibida.getDatos()).getActividad());
    }
}
