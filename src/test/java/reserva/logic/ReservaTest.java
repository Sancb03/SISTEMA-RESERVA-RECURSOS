package reserva.logic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Reserva: getters/setters y valores por defecto")
class ReservaTest {

    private Reserva reserva;

    @BeforeEach
    void setUp() {
        reserva = new Reserva();
    }

    @Test
    @DisplayName("Una reserva nueva empieza ACTIVA y sin recursos asignados")
    void estadoInicial() {
        assertEquals(Reserva.ACTIVA, reserva.getEstado());
        assertNotNull(reserva.getRecursos());
        assertTrue(reserva.getRecursos().isEmpty());
    }

    @Test
    @DisplayName("Los datos que se asignan se pueden volver a leer")
    void gettersYSetters() {
        Usuario funcionario = new Funcionario();
        funcionario.setId(111);
        funcionario.setNombre("Juan Perez");

        Recurso recurso = new Recurso();
        recurso.setId("238715");

        reserva.setId("RES-000001");
        reserva.setActividad("Reunion de trabajo");
        reserva.setFecha(LocalDate.of(2026, 8, 14));
        reserva.setHoraInicio(LocalTime.of(8, 0));
        reserva.setHoraFin(LocalTime.of(10, 0));
        reserva.setFuncionario(funcionario);
        reserva.setRecursos(List.of(recurso));

        assertEquals("RES-000001", reserva.getId());
        assertEquals("Reunion de trabajo", reserva.getActividad());
        assertEquals(LocalDate.of(2026, 8, 14), reserva.getFecha());
        assertEquals(LocalTime.of(8, 0), reserva.getHoraInicio());
        assertEquals(LocalTime.of(10, 0), reserva.getHoraFin());
        assertEquals(funcionario, reserva.getFuncionario());
        assertEquals(1, reserva.getRecursos().size());
        assertEquals("238715", reserva.getRecursos().get(0).getId());
    }

    @Test
    @DisplayName("setRecursos(null) no debe dejar la lista en null")
    void setRecursosNuloQuedaListaVacia() {
        reserva.setRecursos(null);
        assertNotNull(reserva.getRecursos());
        assertTrue(reserva.getRecursos().isEmpty());
    }
}
