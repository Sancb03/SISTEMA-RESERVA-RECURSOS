package reserva.data;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Properties;

/**
 * Prepara una base de datos de PRUEBA desde cero para las pruebas de
 * integración: ejecuta sql/sistema_reservas.sql (que Maven copia al classpath
 * de pruebas, ver testResources en el pom) pero apuntando a
 * "reservarecursos_test", así los datos reales nunca se tocan.
 *
 * Aunque la prueba se corra desde IntelliJ (sin Failsafe), este inicializador
 * fuerza el nombre de la base de pruebas ANTES de que Database la abra.
 */
public final class BaseDePruebas {

    public static final String NOMBRE = "reservarecursos_test";

    static {
        System.setProperty("reserva.db.name", NOMBRE);
    }

    private BaseDePruebas() {
    }

    /** @return true si MySQL respondió y la base de pruebas quedó lista; false si no hay MySQL disponible. */
    public static boolean preparar() {
        try {
            Properties p = new Properties();
            try (InputStream in = BaseDePruebas.class.getResourceAsStream(Database.PROPERTIES_FILE_NAME)) {
                p.load(in);
            }
            Class.forName(p.getProperty("database_driver"));
            String url = "jdbc:mysql://" + p.getProperty("database_server") + ":" + p.getProperty("database_port")
                    + "/?serverTimezone=UTC&allowMultiQueries=true";

            String script;
            try (InputStream in = BaseDePruebas.class.getResourceAsStream("/sistema_reservas.sql")) {
                script = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            }
            script = script.replace("reservarecursos", NOMBRE);

            try (Connection cnx = DriverManager.getConnection(url,
                    p.getProperty("database_user"), System.getenv().getOrDefault("RESERVA_DB_PASSWORD",
                            p.getProperty("database_password", "")));
                 Statement stm = cnx.createStatement()) {
                stm.execute("DROP DATABASE IF EXISTS " + NOMBRE);
                stm.execute(script);
            }
            return true;
        } catch (Exception ex) {
            System.err.println("No se pudo preparar la base de pruebas (¿está corriendo MySQL?): " + ex);
            return false;
        }
    }
}
