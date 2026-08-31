package reserva.data;

import reserva.logic.Recurso;

import java.util.ArrayList;
import java.util.List;

public class RecursoDao {
    public boolean guardar(Recurso recurso) {
        return recurso != null;
    }

    public boolean actualizar(Recurso recurso) {
        return recurso != null;
    }

    public boolean eliminar(int id) {
        return id > 0;
    }

    public Recurso buscarPorId(int id) {
        return null;
    }

    public List<Recurso> listar() {
        return new ArrayList<>();
    }
}
