package reserva.data;

import reserva.logic.Reserva;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * TODO (equipo): en memoria por ahora (igual que UsuarioDao), mientras se
 * implementa la persistencia real en XML. Las firmas públicas no deberían
 * cambiar cuando eso pase.
 */
public class ReservaDao {

    private static final List<Reserva> RESERVAS = new ArrayList<>();
    private static int siguienteId = 1;

    public boolean guardar(Reserva reserva) {
        if (reserva == null) {
            return false;
        }
        reserva.setId(siguienteId++);
        return RESERVAS.add(reserva);
    }

    public boolean actualizar(Reserva reserva) {
        if (reserva == null) {
            return false;
        }
        for (int i = 0; i < RESERVAS.size(); i++) {
            if (RESERVAS.get(i).getId() == reserva.getId()) {
                RESERVAS.set(i, reserva);
                return true;
            }
        }
        return false;
    }

    public boolean eliminar(int id) {
        return RESERVAS.removeIf(r -> r.getId() == id);
    }

    public Reserva buscarPorId(int id) {
        return RESERVAS.stream()
                .filter(r -> r.getId() == id)
                .findFirst()
                .orElse(null);
    }

    public List<Reserva> listar() {
        return new ArrayList<>(RESERVAS);
    }

    public List<Reserva> listarPorFuncionario(int funcionarioId) {
        List<Reserva> resultado = new ArrayList<>();
        for (Reserva r : RESERVAS) {
            if (r.getFuncionarioId() == funcionarioId) {
                resultado.add(r);
            }
        }
        return resultado;
    }

    /**
     * ¿Ese recurso ya tiene una reserva ACTIVA que se cruza con el horario pedido,
     * en esa misma fecha? Se usa para saber si el recurso está disponible.
     * 'excluirReservaId' se usa al editar/recalcular una reserva para no chocar consigo misma (puede ser null).
     */
    public boolean existeSolape(int recursoId, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin, Integer excluirReservaId) {
        for (Reserva r : RESERVAS) {
            if (excluirReservaId != null && r.getId() == excluirReservaId) {
                continue;
            }
            if (!Reserva.ACTIVA.equals(r.getEstado())) {
                continue;
            }
            if (!r.getFecha().equals(fecha)) {
                continue;
            }
            if (!r.getRecursosIds().contains(recursoId)) {
                continue;
            }
            // se solapan si el inicio de uno es antes del fin del otro, en ambos sentidos
            boolean seCruzan = horaInicio.isBefore(r.getHoraFin()) && r.getHoraInicio().isBefore(horaFin);
            if (seCruzan) {
                return true;
            }
        }
        return false;
    }
}
