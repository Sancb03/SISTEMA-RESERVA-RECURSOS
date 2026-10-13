package reserva.data;

import reserva.logic.Recurso;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a la tabla "recursos". Cada Recurso se lee con un join a
 * "categorias" para devolverlo con su Categoria ya adentro (igual que
 * PersonaDao arma Persona + Departamento + EstadoCivil en el ejemplo
 * del profe).
 */
public class RecursoDao {

    static final String SELECT_CON_CATEGORIA =
            "select r.id as r_id, r.descripcion as r_descripcion, "
                    + "c.id as c_id, c.descripcion as c_descripcion "
                    + "from recursos r inner join categorias c on r.categoria = c.id ";

    private final Database db;

    public RecursoDao() {
        this.db = Database.instance();
    }

    /** @return false si ya existe un recurso con ese id. */
    public boolean create(Recurso r) {
        String sql = "insert into recursos (id, categoria, descripcion) values (?,?,?)";
        try (PreparedStatement stm = db.prepareStatement(sql)) {
            stm.setString(1, r.getId());
            stm.setString(2, r.getCategoria().getId());
            stm.setString(3, r.getDescripcion());
            return stm.executeUpdate() > 0;
        } catch (SQLException ex) {
            if (DbErrores.esDuplicado(ex)) {
                return false;
            }
            if (DbErrores.esReferenciaInvalida(ex)) {
                throw new IllegalArgumentException("La categoría del recurso no existe.");
            }
            throw DbErrores.error(ex);
        }
    }

    public boolean update(Recurso r) {
        String sql = "update recursos set categoria=?, descripcion=? where id=?";
        try (PreparedStatement stm = db.prepareStatement(sql)) {
            stm.setString(1, r.getCategoria().getId());
            stm.setString(2, r.getDescripcion());
            stm.setString(3, r.getId());
            return stm.executeUpdate() > 0;
        } catch (SQLException ex) {
            if (DbErrores.esReferenciaInvalida(ex)) {
                throw new IllegalArgumentException("La categoría del recurso no existe.");
            }
            throw DbErrores.error(ex);
        }
    }

    /** @return false si no existía. Lanza IllegalStateException si el recurso ya está en alguna reserva. */
    public boolean delete(String id) {
        try (PreparedStatement stm = db.prepareStatement("delete from recursos where id=?")) {
            stm.setString(1, id);
            return stm.executeUpdate() > 0;
        } catch (SQLException ex) {
            if (DbErrores.esReferenciado(ex)) {
                throw new IllegalStateException("No se puede eliminar: el recurso está en alguna reserva.");
            }
            throw DbErrores.error(ex);
        }
    }

    public Recurso read(String id) {
        List<Recurso> lista = buscar(SELECT_CON_CATEGORIA + "where r.id = ?", id);
        return lista.isEmpty() ? null : lista.get(0);
    }

    public List<Recurso> findAll() {
        return buscar(SELECT_CON_CATEGORIA + "order by r.id", null);
    }

    public List<Recurso> findByCategoria(String categoriaId) {
        return buscar(SELECT_CON_CATEGORIA + "where c.id = ? order by r.id", categoriaId);
    }

    private List<Recurso> buscar(String sql, String parametro) {
        List<Recurso> resultado = new ArrayList<>();
        try (PreparedStatement stm = db.prepareStatement(sql)) {
            if (parametro != null) {
                stm.setString(1, parametro);
            }
            try (ResultSet rs = stm.executeQuery()) {
                while (rs.next()) {
                    resultado.add(from(rs));
                }
            }
        } catch (SQLException ex) {
            throw DbErrores.error(ex);
        }
        return resultado;
    }

    /** Arma el Recurso (con su Categoria) de una fila de SELECT_CON_CATEGORIA. */
    static Recurso from(ResultSet rs) throws SQLException {
        Recurso r = new Recurso();
        r.setId(rs.getString("r_id"));
        r.setDescripcion(rs.getString("r_descripcion"));
        r.setCategoria(CategoriaDao.from(rs, "c_"));
        return r;
    }
}
