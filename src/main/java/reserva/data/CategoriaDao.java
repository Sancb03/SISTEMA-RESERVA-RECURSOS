package reserva.data;

import reserva.logic.Categoria;

import java.util.ArrayList;
import java.util.List;

public class CategoriaDao {
    public boolean guardar(Categoria categoria) {
        return categoria != null;
    }

    public boolean actualizar(Categoria categoria) {
        return categoria != null;
    }

    public boolean eliminar(int id) {
        return id > 0;
    }

    public Categoria buscarPorId(int id) {
        return null;
    }

    public List<Categoria> listar() {
        return new ArrayList<>();
    }
}
