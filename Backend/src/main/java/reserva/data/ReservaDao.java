package reserva.data;

import reserva.logic.Recurso;
import reserva.logic.Reserva;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Acceso a "reservas" y "reserva_recursos". Una Reserva se devuelve completa:
 * con su Funcionario (join con usuarios) y su lista de Recursos (cada uno con
 * su Categoria). Crear/actualizar/borrar una reserva toca dos tablas, así que
 * se hace en una transacción (o se guardan las dos, o ninguna).
 */
public class ReservaDao {

    private static final String PREFIJO = "RES-";
    private static final int DIGITOS = 6;

    private static final String SELECT_RESERVA =
            "select r.id as r_id, r.actividad as r_actividad, r.fecha as r_fecha, "
                    + "r.horaInicio as r_horaInicio, r.horaFin as r_horaFin, r.estado as r_estado, "
                    + "u.id as u_id, u.identificacion as u_identificacion, u.nombre as u_nombre, "
                    + "u.clave as u_clave, u.rol as u_rol, u.telefono as u_telefono "
                    + "from reservas r inner join usuarios u on r.funcionario = u.id ";

    private final Database db;

    public ReservaDao() {
        this.db = Database.instance();
    }

    public String siguienteId() {
        String sql = "select max(cast(substring(id, ?) as unsigned)) from reservas where id like ?";
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

    /** Asigna el id (RES-xxxxxx) a la reserva y la guarda junto con sus recursos. */
    public void create(Reserva r) {
        r.setId(siguienteId());
        Connection cnx = null;
        try {
            cnx = db.getConnection();
            cnx.setAutoCommit(false);
            insertarReserva(cnx, r);
            insertarRecursos(cnx, r);
            cnx.commit();
        } catch (SQLException ex) {
            deshacer(cnx);
            if (DbErrores.esReferenciaInvalida(ex)) {
                throw new IllegalArgumentException("La reserva usa un funcionario o un recurso que ya no existe.");
            }
            throw DbErrores.error(ex);
        } finally {
            restaurarAutoCommit(cnx);
        }
    }

    /** @return false si no existe una reserva con ese id. */
    public boolean update(Reserva r) {
        Connection cnx = null;
        try {
            cnx = db.getConnection();
            cnx.setAutoCommit(false);
            String sql = "update reservas set actividad=?, fecha=?, horaInicio=?, horaFin=?, funcionario=?, estado=? where id=?";
            int filas;
            try (PreparedStatement stm = cnx.prepareStatement(sql)) {
                stm.setString(1, r.getActividad());
                stm.setObject(2, r.getFecha());
                stm.setObject(3, r.getHoraInicio());
                stm.setObject(4, r.getHoraFin());
                stm.setInt(5, r.getFuncionario().getId());
                stm.setString(6, r.getEstado());
                stm.setString(7, r.getId());
                filas = stm.executeUpdate();
            }
            if (filas == 0) {
                cnx.rollback();
                return false;
            }
            borrarRecursos(cnx, r.getId());
            insertarRecursos(cnx, r);
            cnx.commit();
            return true;
        } catch (SQLException ex) {
            deshacer(cnx);
            if (DbErrores.esReferenciaInvalida(ex)) {
                throw new IllegalArgumentException("La reserva usa un funcionario o un recurso que ya no existe.");
            }
            throw DbErrores.error(ex);
        } finally {
            restaurarAutoCommit(cnx);
        }
    }

    /** @return false si no existía. */
    public boolean delete(String id) {
        Connection cnx = null;
        try {
            cnx = db.getConnection();
            cnx.setAutoCommit(false);
            borrarRecursos(cnx, id);
            int filas;
            try (PreparedStatement stm = cnx.prepareStatement("delete from reservas where id=?")) {
                stm.setString(1, id);
                filas = stm.executeUpdate();
            }
            cnx.commit();
            return filas > 0;
        } catch (SQLException ex) {
            deshacer(cnx);
            throw DbErrores.error(ex);
        } finally {
            restaurarAutoCommit(cnx);
        }
    }

    public Reserva read(String id) {
        List<Reserva> lista = buscar("where r.id = ? ", id);
        return lista.isEmpty() ? null : lista.get(0);
    }

    public List<Reserva> findAll() {
        return buscar("", null);
    }

    public List<Reserva> findByFuncionario(int funcionarioId) {
        return buscar("where r.funcionario = ? ", funcionarioId);
    }

    /**
     * ¿El recurso ya está tomado en esa fecha y se cruza con [horaInicio, horaFin)?
     * Solo cuentan las reservas ACTIVAS, y se puede excluir una reserva (la que se
     * está modificando). Es la misma regla que tenía Data.existeSolape, pero ahora
     * la resuelve MySQL.
     */
    public boolean existeSolape(String recursoId, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin,
                                String excluirReservaId) {
        String sql = "select count(*) from reservas r "
                + "inner join reserva_recursos rr on rr.reserva = r.id "
                + "where rr.recurso = ? and r.fecha = ? and r.estado = 'ACTIVA' "
                + "and r.id <> ? and ? < r.horaFin and r.horaInicio < ?";
        try (PreparedStatement stm = db.prepareStatement(sql)) {
            stm.setString(1, recursoId);
            stm.setObject(2, fecha);
            stm.setString(3, excluirReservaId == null ? "" : excluirReservaId);
            stm.setObject(4, horaInicio);
            stm.setObject(5, horaFin);
            try (ResultSet rs = stm.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException ex) {
            throw DbErrores.error(ex);
        }
    }

    // ------------------------------------------------------------------
    // lectura: primero las reservas (con funcionario), luego todos sus recursos en una sola consulta
    // ------------------------------------------------------------------

    private List<Reserva> buscar(String where, Object parametro) {
        Map<String, Reserva> porId = new LinkedHashMap<>();
        try (PreparedStatement stm = db.prepareStatement(SELECT_RESERVA + where + "order by r.id")) {
            if (parametro != null) {
                stm.setObject(1, parametro);
            }
            try (ResultSet rs = stm.executeQuery()) {
                while (rs.next()) {
                    Reserva r = new Reserva();
                    r.setId(rs.getString("r_id"));
                    r.setActividad(rs.getString("r_actividad"));
                    r.setFecha(rs.getObject("r_fecha", LocalDate.class));
                    r.setHoraInicio(rs.getObject("r_horaInicio", LocalTime.class));
                    r.setHoraFin(rs.getObject("r_horaFin", LocalTime.class));
                    r.setEstado(rs.getString("r_estado"));
                    r.setFuncionario(UsuarioDao.from(rs, "u_"));
                    // la clave del funcionario NO viaja por la red dentro de cada reserva
                    r.getFuncionario().setClave(null);
                    porId.put(r.getId(), r);
                }
            }
            cargarRecursos(porId);
        } catch (SQLException ex) {
            throw DbErrores.error(ex);
        }
        return new ArrayList<>(porId.values());
    }

    private void cargarRecursos(Map<String, Reserva> porId) throws SQLException {
        if (porId.isEmpty()) {
            return;
        }
        String marcas = String.join(",", java.util.Collections.nCopies(porId.size(), "?"));
        String sql = "select rr.reserva as rr_reserva, r.id as r_id, r.descripcion as r_descripcion, "
                + "c.id as c_id, c.descripcion as c_descripcion "
                + "from reserva_recursos rr "
                + "inner join recursos r on rr.recurso = r.id "
                + "inner join categorias c on r.categoria = c.id "
                + "where rr.reserva in (" + marcas + ") order by r.id";
        try (PreparedStatement stm = db.prepareStatement(sql)) {
            int i = 1;
            for (String id : porId.keySet()) {
                stm.setString(i++, id);
            }
            try (ResultSet rs = stm.executeQuery()) {
                while (rs.next()) {
                    Recurso recurso = RecursoDao.from(rs);
                    porId.get(rs.getString("rr_reserva")).getRecursos().add(recurso);
                }
            }
        }
    }

    // ------------------------------------------------------------------
    // escritura (dentro de la transacción que abre create/update/delete)
    // ------------------------------------------------------------------

    private void insertarReserva(Connection cnx, Reserva r) throws SQLException {
        String sql = "insert into reservas (id, actividad, fecha, horaInicio, horaFin, funcionario, estado) "
                + "values (?,?,?,?,?,?,?)";
        try (PreparedStatement stm = cnx.prepareStatement(sql)) {
            stm.setString(1, r.getId());
            stm.setString(2, r.getActividad());
            stm.setObject(3, r.getFecha());
            stm.setObject(4, r.getHoraInicio());
            stm.setObject(5, r.getHoraFin());
            stm.setInt(6, r.getFuncionario().getId());
            stm.setString(7, r.getEstado());
            stm.executeUpdate();
        }
    }

    private void insertarRecursos(Connection cnx, Reserva r) throws SQLException {
        try (PreparedStatement stm = cnx.prepareStatement("insert into reserva_recursos (reserva, recurso) values (?,?)")) {
            for (Recurso recurso : r.getRecursos()) {
                stm.setString(1, r.getId());
                stm.setString(2, recurso.getId());
                stm.addBatch();
            }
            stm.executeBatch();
        }
    }

    private void borrarRecursos(Connection cnx, String reservaId) throws SQLException {
        try (PreparedStatement stm = cnx.prepareStatement("delete from reserva_recursos where reserva=?")) {
            stm.setString(1, reservaId);
            stm.executeUpdate();
        }
    }

    private void deshacer(Connection cnx) {
        try {
            if (cnx != null) {
                cnx.rollback();
            }
        } catch (SQLException ignored) {
        }
    }

    private void restaurarAutoCommit(Connection cnx) {
        try {
            if (cnx != null) {
                cnx.setAutoCommit(true);
            }
        } catch (SQLException ignored) {
        }
    }
}
