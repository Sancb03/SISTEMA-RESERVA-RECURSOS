package reserva.presentation.Estadisticas;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reserva.logic.Categoria;
import reserva.logic.Recurso;
import reserva.logic.Reserva;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("EstadisticasCalculo: recursos por categoría y actividades por semana")
class EstadisticasCalculoTest {

    private static Categoria categoria(String descripcion) {
        Categoria c = new Categoria();
        c.setDescripcion(descripcion);
        return c;
    }

    private static Recurso recurso(String id, Categoria categoria) {
        Recurso r = new Recurso();
        r.setId(id);
        r.setCategoria(categoria);
        return r;
    }

    private static Reserva reserva(LocalDate fecha, String estado, Recurso... recursos) {
        Reserva r = new Reserva();
        r.setFecha(fecha);
        r.setEstado(estado);
        r.setRecursos(new ArrayList<>(List.of(recursos)));
        return r;
    }

    private final Categoria salas = categoria("Salas");
    private final Categoria equipo = categoria("Equipo");

    @Test
    @DisplayName("Cuenta los recursos reservados por categoría dentro del rango")
    void recursosPorCategoria() {
        List<Reserva> reservas = List.of(
                reserva(LocalDate.of(2026, 10, 27), Reserva.ACTIVA, recurso("S1", salas), recurso("E1", equipo)),
                reserva(LocalDate.of(2026, 10, 28), Reserva.ACTIVA, recurso("S1", salas)));

        List<Object[]> filas = EstadisticasCalculo.recursosPorCategoria(reservas,
                LocalDate.of(2026, 10, 26), LocalDate.of(2026, 11, 1));

        assertEquals(2, filas.size());
        assertEquals("Salas", filas.get(0)[0]);
        assertEquals(Integer.valueOf(2), filas.get(0)[1]);
        assertEquals("Equipo", filas.get(1)[0]);
        assertEquals(Integer.valueOf(1), filas.get(1)[1]);
    }

    @Test
    @DisplayName("Ignora las reservas canceladas y las que caen fuera del rango")
    void ignoraCanceladasYFueraDeRango() {
        List<Reserva> reservas = List.of(
                reserva(LocalDate.of(2026, 10, 27), Reserva.CANCELADA, recurso("S1", salas)),
                reserva(LocalDate.of(2026, 9, 1), Reserva.ACTIVA, recurso("S1", salas)),
                reserva(LocalDate.of(2026, 10, 27), Reserva.ACTIVA, recurso("E1", equipo)));

        List<Object[]> filas = EstadisticasCalculo.recursosPorCategoria(reservas,
                LocalDate.of(2026, 10, 26), LocalDate.of(2026, 11, 1));

        assertEquals(1, filas.size());
        assertEquals("Equipo", filas.get(0)[0]);
    }

    @Test
    @DisplayName("Sin reservas no hay filas")
    void sinReservas() {
        assertTrue(EstadisticasCalculo.recursosPorCategoria(List.of(),
                LocalDate.of(2026, 10, 26), LocalDate.of(2026, 11, 1)).isEmpty());
    }

    @Test
    @DisplayName("Agrupa las actividades por semana (lunes a domingo) y muestra también las semanas en cero")
    void actividadesPorSemana() {
        // 2026-10-26 es lunes
        List<Reserva> reservas = List.of(
                reserva(LocalDate.of(2026, 10, 26), Reserva.ACTIVA),
                reserva(LocalDate.of(2026, 11, 1), Reserva.ACTIVA),
                reserva(LocalDate.of(2026, 11, 2), Reserva.ACTIVA),
                reserva(LocalDate.of(2026, 11, 3), Reserva.CANCELADA));

        List<Object[]> filas = EstadisticasCalculo.actividadesPorSemana(reservas,
                LocalDate.of(2026, 10, 26), LocalDate.of(2026, 11, 15));

        assertEquals(3, filas.size());
        assertEquals("26/10/2026 - 01/11/2026", filas.get(0)[0]);
        assertEquals(Integer.valueOf(2), filas.get(0)[1]);
        assertEquals("02/11/2026 - 08/11/2026", filas.get(1)[0]);
        assertEquals(Integer.valueOf(1), filas.get(1)[1]);
        assertEquals(Integer.valueOf(0), filas.get(2)[1]);
    }
}
