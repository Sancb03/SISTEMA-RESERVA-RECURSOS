package reserva.data;

import reserva.logic.Reserva;

import java.util.ArrayList;
import java.util.List;

public class ReservaDao {
    public boolean guardar(Reserva reserva) {
        return reserva != null;
    }

    public boolean actualizar(Reserva reserva) {
        return reserva != null;
    }

    public boolean eliminar(int id) {
        return id > 0;
    }

    public Reserva buscarPorId(int id) {
        return null;
    }

    public List<Reserva> listar() {
        return new ArrayList<>();
    }
}
