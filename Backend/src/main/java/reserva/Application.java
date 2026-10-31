package reserva;

import reserva.data.Database;
import reserva.logic.Protocol;
import reserva.logic.Server;

/**
 * Arranca el Backend: java -jar RESERVA-BACKEND-...-jar-with-dependencies.jar [puerto]
 */
public class Application {

    public static void main(String[] args) {
        int puerto = args.length > 0 ? Integer.parseInt(args[0]) : Protocol.PORT;

        try {
            Database.instance(); // si MySQL no está disponible, que falle YA y con mensaje claro
        } catch (RuntimeException ex) {
            System.err.println(ex.getMessage());
            System.exit(1);
        }

        Server server = new Server(puerto);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            server.stop();
            Database.instance().close();
        }));
        server.run();
    }
}
