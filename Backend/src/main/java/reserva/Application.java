package reserva;

import reserva.data.Database;
import reserva.logic.Protocol;
import reserva.logic.Server;

/**
 * Arranca el Backend: java -jar RESERVA-BACKEND-...-jar-with-dependencies.jar [puerto]
 */
public class Application {

    public static void main(String[] args) {
        int puerto = Protocol.PORT;
        if (args.length > 0) {
            try {
                puerto = Integer.parseInt(args[0].trim());
                if (puerto < 1 || puerto > 65535) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException ex) {
                System.err.println("Puerto inválido: \"" + args[0] + "\". Use un número entre 1 y 65535.");
                System.exit(1);
            }
        }
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
