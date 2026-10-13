package reserva.data;

import reserva.logic.Categoria;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Acceso a la tabla "categorias". Los ids son CAT-000001, CAT-000002... y los genera este DAO. */
public class CategoriaDao {

    private static final String PREFIJO = "CAT-";
    private static final int DIGITOS = 6;

    private final Database db;

    public CategoriaDao() {
        this.db = Database.instance();
    }

    /** Siguiente id libre: toma el número más alto que haya en la tabla y le suma 1. */
    public String siguienteId() {
        String sql = "select max(cast(substring(id, ?) as unsigned)) from categorias where id like ?";
        try (PreparedStatement stm = db.prepareStatement(sql)) {
            stm.setInt(1, PREFIJO.length() + 1);
            stm.setString(2, PREFIJO + "%");
            try (ResultSet rs = stm.executeQuery()) {
                int max = rs.next() ? rs.getInt(1) : 0;
                return PREFIJO + String.format("%0" + DIGITOS + "d", max + 1);
            }
        } catch (SQLException ex) {
            throw DbErrores.error(ex);
        }
    }

    /** Asigna el id (CAT-xxxxxx) a la categoría y la inserta. */
    public void create(Categoria c) {
        c.setId(siguienteId());
        try (PreparedStatement stm = db.prepareStatement("insert into categorias (id, descripcion) values (?,?)")) {
            stm.setString(1, c.getId());
            stm.setString(2, c.getDescripcion());
            stm.executeUpdate();
        } catch (SQLException ex) {
            throw DbErrores.error(ex);
        }
    }

    public boolean update(Categoria c) {
        try (PreparedStatement stm = db.prepareStatement("update categorias set descripcion=? where id=?")) {
            stm.setString(1, c.getDescripcion());
            stm.setString(2, c.getId());
            return stm.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw DbErrores.error(ex);
        }
    }

    /** @return false si no existía. Lanza IllegalStateException si todavía tiene recursos. */
    public boolean delete(String id) {
        try (PreparedStatement stm = db.prepareStatement("delete from categorias where id=?")) {
            stm.setString(1, id);
            return stm.executeUpdate() > 0;
        } catch (SQLException ex) {
            if (DbErrores.esReferenciado(ex)) {
                throw new IllegalStateException("No se puede eliminar: la categoría tiene recursos asociados.");
            }
            throw DbErrores.error(ex);
        }
    }

    public Categoria read(String id) {
        try (PreparedStatement stm = db.prepareStatement("select * from categorias where id=?")) {
            stm.setString(1, id);
            try (ResultSet rs = stm.executeQuery()) {
                return rs.next() ? from(rs, "") : null;
            }
        } catch (SQLException ex) {
            throw DbErrores.error(ex);
        }
    }

    public List<Categoria> findAll() {
        return buscar("select * from categorias order by id", null);
    }

    public List<Categoria> findByDescripcion(String texto) {
        return buscar("select * from categorias where lower(descripcion) like ? order by id",
                "%" + texto.toLowerCase() + "%");
    }

    private List<Categoria> buscar(String sql, String filtro) {
        List<Categoria> resultado = new ArrayList<>();
        try (PreparedStatement stm = db.prepareStatement(sql)) {
            if (filtro != null) {
                stm.setString(1, filtro);
            }
            try (ResultSet rs = stm.executeQuery()) {
                while (rs.next()) {
                    resultado.add(from(rs, ""));
                }
            }
        } catch (SQLException ex) {
            throw DbErrores.error(ex);
        }
        return resultado;
    }

    /** prefijo: para los joins, donde las columnas vienen como "c_id", "c_descripcion". */
    static Categoria from(ResultSet rs, String prefijo) throws SQLException {
        Categoria c = new Categoria();
        c.setId(rs.getString(prefijo + "id"));
        c.setDescripcion(rs.getString(prefijo + "descripcion"));
        return c;
    }
}
