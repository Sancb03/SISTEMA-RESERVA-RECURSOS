package reserva.data;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Conexión JDBC a MySQL. Mismo esquema que Database.java del ejemplo del
 * profe (Personas): lee database.properties del classpath y arma el URL.
 *
 * Diferencias a propósito:
 *  - En vez de System.exit(-1) si no conecta, lanza RuntimeException con un
 *    mensaje claro (el Servidor lo muestra y termina).
 *  - Si MySQL cerró la conexión por inactividad, se reconecta sola.
 *  - La base se puede cambiar con -Dreserva.db.name=... (así las pruebas
 *    de integración usan otra base y no tocan la de verdad).
 *
 * Todos los DAOs comparten esta única conexión, por eso ServiceImpl
 * sincroniza sus métodos (un cliente a la vez toca la base).
 */
public class Database {

    public static final String PROPERTIES_FILE_NAME = "/database.properties";

    private static Database theInstance;

    public static synchronized Database instance() {
        if (theInstance == null) {
            theInstance = new Database();
        }
        return theInstance;
    }

    private final Properties prop = new Properties();
    private Connection cnx;

    private Database() {
        try (InputStream in = getClass().getResourceAsStream(PROPERTIES_FILE_NAME)) {
            if (in == null) {
                throw new IllegalStateException("No se encontró " + PROPERTIES_FILE_NAME + " en src/main/resources");
            }
            prop.load(in);
        } catch (Exception e) {
            throw new RuntimeException("No se pudo leer database.properties: " + e.getMessage(), e);
        }
        cnx = conectar();
    }

    private Connection conectar() {
        try {
            String driver = prop.getProperty("database_driver");
            String server = prop.getProperty("database_server");
            String port = prop.getProperty("database_port");
            String user = prop.getProperty("database_user");
            String password = System.getenv().getOrDefault("RESERVA_DB_PASSWORD",
                    prop.getProperty("database_password", ""));
            String database = System.getProperty("reserva.db.name", prop.getProperty("database_name"));

            String url = "jdbc:mysql://" + server + ":" + port + "/" + database
                    + "?serverTimezone=UTC";
            Class.forName(driver);
            return DriverManager.getConnection(url, user, password);
        } catch (Exception e) {
            throw new RuntimeException("No se pudo conectar a MySQL: " + e.getMessage(), e);
        }
    }

    private void asegurarConexion() throws SQLException {
        if (cnx == null || cnx.isClosed() || !cnx.isValid(2)) {
            cnx = conectar();
        }
    }

    public synchronized Connection getConnection() throws SQLException {
        asegurarConexion();
        return cnx;
    }

    public PreparedStatement prepareStatement(String sql) throws SQLException {
        return getConnection().prepareStatement(sql);
    }

    public synchronized void close() {
        try {
            if (cnx != null && !cnx.isClosed()) {
                cnx.close();
            }
        } catch (SQLException ignored) {
        }
    }
}
