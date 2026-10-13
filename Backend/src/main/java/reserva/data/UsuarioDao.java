package reserva.data;

import reserva.logic.Administrador;
import reserva.logic.Funcionario;
import reserva.logic.Usuario;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a la tabla "usuarios". Misma forma que PersonaDao del ejemplo del
 * profe: PreparedStatement + un método privado from(rs) que arma el objeto.
 * Una sola tabla guarda Administrador y Funcionario; la columna "rol"
 * decide cuál clase Java se construye.
 */
public class UsuarioDao {

    private final Database db;

    public UsuarioDao() {
        this.db = Database.instance();
    }

    /** @return false si ya existe un usuario con ese id o esa identificación. */
    public boolean create(Usuario u) {
        String sql = "insert into usuarios (id, identificacion, nombre, clave, rol, telefono) values (?,?,?,?,?,?)";
        try (PreparedStatement stm = db.prepareStatement(sql)) {
            stm.setInt(1, u.getId());
            stm.setString(2, u.getIdentificacion());
            stm.setString(3, u.getNombre());
            stm.setString(4, u.getClave());
            stm.setString(5, u.getRol());
            asignarTelefono(stm, 6, u);
            return stm.executeUpdate() > 0;
        } catch (SQLException ex) {
            if (DbErrores.esDuplicado(ex)) {
                return false;
            }
            throw DbErrores.error(ex);
        }
    }

    /** @return false si no existe ese id, o si la nueva identificación ya la tiene otro usuario. */
    public boolean update(Usuario u) {
        String sql = "update usuarios set identificacion=?, nombre=?, clave=?, rol=?, telefono=? where id=?";
        try (PreparedStatement stm = db.prepareStatement(sql)) {
            stm.setString(1, u.getIdentificacion());
            stm.setString(2, u.getNombre());
            stm.setString(3, u.getClave());
            stm.setString(4, u.getRol());
            asignarTelefono(stm, 5, u);
            stm.setInt(6, u.getId());
            return stm.executeUpdate() > 0;
        } catch (SQLException ex) {
            if (DbErrores.esDuplicado(ex)) {
                return false;
            }
            throw DbErrores.error(ex);
        }
    }

    /** @return false si no existía. Lanza IllegalStateException si el usuario ya tiene reservas. */
    public boolean delete(int id) {
        try (PreparedStatement stm = db.prepareStatement("delete from usuarios where id=?")) {
            stm.setInt(1, id);
            return stm.executeUpdate() > 0;
        } catch (SQLException ex) {
            if (DbErrores.esReferenciado(ex)) {
                throw new IllegalStateException("No se puede eliminar: el funcionario tiene reservas registradas.");
            }
            throw DbErrores.error(ex);
        }
    }

    public Usuario read(int id) {
        try (PreparedStatement stm = db.prepareStatement("select * from usuarios where id=?")) {
            stm.setInt(1, id);
            try (ResultSet rs = stm.executeQuery()) {
                return rs.next() ? from(rs) : null;
            }
        } catch (SQLException ex) {
            throw DbErrores.error(ex);
        }
    }

    public Usuario readByIdentificacion(String identificacion) {
        try (PreparedStatement stm = db.prepareStatement("select * from usuarios where identificacion=?")) {
            stm.setString(1, identificacion);
            try (ResultSet rs = stm.executeQuery()) {
                return rs.next() ? from(rs) : null;
            }
        } catch (SQLException ex) {
            throw DbErrores.error(ex);
        }
    }

    public List<Usuario> findAll() {
        List<Usuario> resultado = new ArrayList<>();
        try (PreparedStatement stm = db.prepareStatement("select * from usuarios order by id");
             ResultSet rs = stm.executeQuery()) {
            while (rs.next()) {
                resultado.add(from(rs));
            }
        } catch (SQLException ex) {
            throw DbErrores.error(ex);
        }
        return resultado;
    }

    public List<Funcionario> findFuncionarios() {
        return buscarFuncionarios("select * from usuarios where rol='FUNCIONARIO' order by id", null);
    }

    public List<Funcionario> findFuncionariosByNombre(String nombre) {
        return buscarFuncionarios(
                "select * from usuarios where rol='FUNCIONARIO' and lower(nombre) like ? order by id",
                "%" + nombre.toLowerCase() + "%");
    }

    private List<Funcionario> buscarFuncionarios(String sql, String filtro) {
        List<Funcionario> resultado = new ArrayList<>();
        try (PreparedStatement stm = db.prepareStatement(sql)) {
            if (filtro != null) {
                stm.setString(1, filtro);
            }
            try (ResultSet rs = stm.executeQuery()) {
                while (rs.next()) {
                    resultado.add((Funcionario) from(rs));
                }
            }
        } catch (SQLException ex) {
            throw DbErrores.error(ex);
        }
        return resultado;
    }

    private void asignarTelefono(PreparedStatement stm, int indice, Usuario u) throws SQLException {
        if (u instanceof Funcionario f && f.getTelefono() != null) {
            stm.setString(indice, f.getTelefono());
        } else {
            stm.setNull(indice, Types.VARCHAR);
        }
    }

    /** Arma el Usuario de la fila actual; también lo usa ReservaDao (join con usuarios). */
    static Usuario from(ResultSet rs) throws SQLException {
        return from(rs, "");
    }

    static Usuario from(ResultSet rs, String prefijo) throws SQLException {
        Usuario u;
        if ("ADMINISTRADOR".equals(rs.getString(prefijo + "rol"))) {
            u = new Administrador();
        } else {
            Funcionario f = new Funcionario();
            f.setTelefono(rs.getString(prefijo + "telefono"));
            u = f;
        }
        u.setId(rs.getInt(prefijo + "id"));
        u.setIdentificacion(rs.getString(prefijo + "identificacion"));
        u.setNombre(rs.getString(prefijo + "nombre"));
        u.setClave(rs.getString(prefijo + "clave"));
        return u;
    }
}
