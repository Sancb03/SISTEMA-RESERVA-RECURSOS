package reserva.presentation.Estadisticas;

import reserva.logic.Recurso;
import reserva.logic.Reserva;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Hace las cuentas de las estadisticas a partir de una lista de reservas.
 * Esta separado del Controller para poder probarlo sin ventanas ni Backend.
 */
public class EstadisticasCalculo {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private EstadisticasCalculo() {
    }

    /** Cantidad de recursos reservados por categoria (solo reservas ACTIVAS dentro del rango). */
    public static List<Object[]> recursosPorCategoria(List<Reserva> reservas, LocalDate inicio, LocalDate fin) {
        Map<String, Integer> cantidades = new LinkedHashMap<>();

        for (Reserva reserva : reservas) {
            if (!cuenta(reserva, inicio, fin)) {
                continue;
            }
            for (Recurso recurso : reserva.getRecursos()) {
                if (recurso.getCategoria() == null) {
                    continue;
                }
                String categoria = recurso.getCategoria().getDescripcion();
                cantidades.put(categoria, cantidades.getOrDefault(categoria, 0) + 1);
            }
        }

        List<Object[]> filas = new ArrayList<>();
        for (Map.Entry<String, Integer> dato : cantidades.entrySet()) {
            filas.add(new Object[]{dato.getKey(), dato.getValue()});
        }
        return filas;
    }

    /** Cantidad de actividades (reservas ACTIVAS) por cada semana (lunes a domingo) del rango. */
    public static List<Object[]> actividadesPorSemana(List<Reserva> reservas, LocalDate inicio, LocalDate fin) {
        List<Object[]> filas = new ArrayList<>();

        LocalDate lunes = inicio.with(DayOfWeek.MONDAY);
        while (!lunes.isAfter(fin)) {
            LocalDate domingo = lunes.plusDays(6);
            int cantidad = 0;

            for (Reserva reserva : reservas) {
                if (!cuenta(reserva, inicio, fin)) {
                    continue;
                }
                LocalDate fecha = reserva.getFecha();
                if (!fecha.isBefore(lunes) && !fecha.isAfter(domingo)) {
                    cantidad++;
                }
            }

            filas.add(new Object[]{lunes.format(FORMATO) + " - " + domingo.format(FORMATO), cantidad});
            lunes = lunes.plusWeeks(1);
        }
        return filas;
    }

    private static boolean cuenta(Reserva reserva, LocalDate inicio, LocalDate fin) {
        return reserva.getFecha() != null
                && Reserva.ACTIVA.equals(reserva.getEstado())
                && !reserva.getFecha().isBefore(inicio)
                && !reserva.getFecha().isAfter(fin);
    }
}
