package reserva.data;

import reserva.logic.Usuario;

import java.util.ArrayList;
import java.util.List;

public class UsuarioDao {
    public boolean guardar(Usuario usuario) {
        return usuario != null;
    }

    public boolean actualizar(Usuario usuario) {
        return usuario != null;
    }

    public boolean eliminar(int id) {
        return id > 0;
    }

    public Usuario buscarPorId(int id) {
        return null;
    }

    public List<Usuario> listar() {
        return new ArrayList<>();
    }
}
