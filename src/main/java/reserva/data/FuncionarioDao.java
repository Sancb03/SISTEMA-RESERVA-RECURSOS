package reserva.data;

import reserva.logic.Administrador;
import reserva.logic.Funcionario;
import reserva.logic.Usuario;

import java.util.ArrayList;
import java.util.List;

public class FuncionarioDao {

    private final UsuarioDao usuarioDao = new UsuarioDao();

    private void validarAdmin(Usuario usuarioActual) {
        if (!(usuarioActual instanceof Administrador)) {
            throw new SecurityException("Solo un administrador puede gestionar funcionarios.");
        }
    }

    public boolean guardar(Funcionario funcionario, Usuario usuarioActual) {
        validarAdmin(usuarioActual);
        if (funcionario == null) return false;

        if (funcionario.getIdentificacion() == null || funcionario.getIdentificacion().isBlank()) {
            funcionario.setIdentificacion(String.valueOf(funcionario.getId()));
        }
        funcionario.setClave(String.valueOf(funcionario.getId())); // clave inicial = id

        return usuarioDao.guardar(funcionario);
    }

    public boolean actualizar(Funcionario funcionario, Usuario usuarioActual) {
        validarAdmin(usuarioActual);
        if (funcionario == null) return false;

        Usuario existente = usuarioDao.buscarPorId(funcionario.getId());
        if (!(existente instanceof Funcionario)) {
            return false; // no existe, o el id pertenece a otro tipo de usuario
        }
        return usuarioDao.actualizar(funcionario);
    }

    public boolean eliminar(int id, Usuario usuarioActual) {
        validarAdmin(usuarioActual);
        Usuario existente = usuarioDao.buscarPorId(id);
        if (!(existente instanceof Funcionario)) {
            return false;
        }
        return usuarioDao.eliminar(id);
    }

    public Funcionario buscarPorId(int id, Usuario usuarioActual) {
        validarAdmin(usuarioActual);
        Usuario u = usuarioDao.buscarPorId(id);
        return (u instanceof Funcionario f) ? f : null;
    }

    public List<Funcionario> buscarPorNombre(String nombre, Usuario usuarioActual) {
        validarAdmin(usuarioActual);
        List<Funcionario> resultado = new ArrayList<>();
        if (nombre == null || nombre.isBlank()) return resultado;
        String buscado = nombre.toLowerCase();
        for (Usuario u : usuarioDao.listar()) {
            if (u instanceof Funcionario f && f.getNombre() != null && f.getNombre().toLowerCase().contains(buscado)) {
                resultado.add(f);
            }
        }
        return resultado;
    }

    public List<Funcionario> listar(Usuario usuarioActual) {
        validarAdmin(usuarioActual);
        List<Funcionario> resultado = new ArrayList<>();
        for (Usuario u : usuarioDao.listar()) {
            if (u instanceof Funcionario f) {
                resultado.add(f);
            }
        }
        return resultado;
    }

}

