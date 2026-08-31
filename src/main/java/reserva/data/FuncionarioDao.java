package reserva.data;

import reserva.logic.Funcionario;

import java.util.ArrayList;
import java.util.List;

public class FuncionarioDao {
    public boolean guardar(Funcionario funcionario) {
        return funcionario != null;
    }

    public boolean actualizar(Funcionario funcionario) {
        return funcionario != null;
    }

    public boolean eliminar(int id) {
        return id > 0;
    }

    public Funcionario buscarPorId(int id) {
        return null;
    }

    public List<Funcionario> listar() {
        return new ArrayList<>();
    }
}
