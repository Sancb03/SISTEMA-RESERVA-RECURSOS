package reserva.data;

import java.sql.SQLException;

/**
 * Códigos de error de MySQL que los DAOs necesitan distinguir para dar
 * mensajes entendibles (en vez de mostrarle al usuario un stack de SQL).
 */
final class DbErrores {

    private DbErrores() {
    }

    /** 1062: Duplicate entry (llave primaria o UNIQUE repetida). */
    static boolean esDuplicado(SQLException e) {
        return e.getErrorCode() == 1062;
    }

    /** 1451: no se puede borrar/cambiar la fila padre porque otra tabla la referencia. */
    static boolean esReferenciado(SQLException e) {
        return e.getErrorCode() == 1451;
    }

    /** 1452: no se puede insertar/cambiar la fila hija porque el padre no existe. */
    static boolean esReferenciaInvalida(SQLException e) {
        return e.getErrorCode() == 1452;
    }

    static RuntimeException error(SQLException e) {
        return new RuntimeException("Error de base de datos: " + e.getMessage(), e);
    }
}
