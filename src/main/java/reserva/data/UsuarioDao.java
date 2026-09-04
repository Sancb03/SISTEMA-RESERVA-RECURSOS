package reserva.data;

import reserva.logic.Administrador;
import reserva.logic.Funcionario;
import reserva.logic.Usuario;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO (equipo): esta clase usa por ahora una lista en memoria como
 * almacenamiento temporal, solo para poder probar el login mientras se
 * implementa la persistencia real en XML del proyecto.
 * <p>
 * Las firmas de los métodos públicos (guardar, actualizar, eliminar,
 * buscarPorId, buscarPorIdentificacion, listar) no deberían cambiar
 * cuando se reemplace esto por XML: solo hay que cambiar lo que hay
 * dentro de cada método. Así el resto de módulos que ya usan este DAO
 * no se ven afectados.
 */
public class UsuarioDao {

    private static final List<Usuario> USUARIOS = new ArrayList<>();
    private static int siguienteId = 1;

    static {
        Administrador admin = new Administrador();
        admin.setId(siguienteId++);
        admin.setIdentificacion("admin");
        admin.setNombre("Administrador");
        admin.setClave("admin");
        USUARIOS.add(admin);

        Funcionario juan = new Funcionario();
        juan.setId(siguienteId++);
        juan.setIdentificacion("111");
        juan.setNombre("Juan Perez");
        juan.setClave("111");
        juan.setTelefono("3323");
        USUARIOS.add(juan);

        Funcionario maria = new Funcionario();
        maria.setId(siguienteId++);
        maria.setIdentificacion("222");
        maria.setNombre("Maria Perez");
        maria.setClave("222");
        maria.setTelefono("222222");
        USUARIOS.add(maria);
    }

    public boolean guardar(Usuario usuario) {
        if (usuario == null) {
            return false;
        }
        usuario.setId(siguienteId++);
        return USUARIOS.add(usuario);
    }

    public boolean actualizar(Usuario usuario) {
        if (usuario == null) {
            return false;
        }
        for (int i = 0; i < USUARIOS.size(); i++) {
            if (USUARIOS.get(i).getId() == usuario.getId()) {
                USUARIOS.set(i, usuario);
                return true;
            }
        }
        return false;
    }

    public boolean eliminar(int id) {
        return USUARIOS.removeIf(u -> u.getId() == id);
    }

    public Usuario buscarPorId(int id) {
        return USUARIOS.stream()
                .filter(u -> u.getId() == id)
                .findFirst()
                .orElse(null);
    }

    /** Necesario para el login: busca por el _id/identificación que el usuario escribe (ej. "admin", "111"). */
    public Usuario buscarPorIdentificacion(String identificacion) {
        if (identificacion == null) {
            return null;
        }
        return USUARIOS.stream()
                .filter(u -> identificacion.equals(u.getIdentificacion()))
                .findFirst()
                .orElse(null);
    }

    public List<Usuario> listar() {
        return new ArrayList<>(USUARIOS);
    }
}