package reserva.logic;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Un "Frontend" mínimo para las pruebas del Backend: habla el Protocol directamente por sockets
 * (conexión SYNC para las peticiones y, si se pide, ASYNC para recibir alertas).
 */
class ClienteDePrueba implements AutoCloseable {

    /** Lo que contestó el Backend: el código, el resultado (o el mensaje si hubo error) y la entidad de los guardar*. */
    record Resultado(int codigo, Object valor, Object entidad) {
    }

    final Socket s;
    final ObjectOutputStream os;
    final ObjectInputStream is;
    final String sid;

    private Socket as;
    private ObjectInputStream ais;
    private final BlockingQueue<Reserva> alertas = new LinkedBlockingQueue<>();

    ClienteDePrueba(int puerto) throws Exception {
        s = new Socket("localhost", puerto);
        s.setSoTimeout(10_000);
        os = new ObjectOutputStream(s.getOutputStream());
        os.flush();
        is = new ObjectInputStream(s.getInputStream());
        os.writeInt(Protocol.SYNC);
        os.flush();
        sid = (String) is.readObject();
    }

    void iniciarAlertas() throws Exception {
        iniciarAlertas(sid);
    }

    void iniciarAlertas(String sidQueSeUne) throws Exception {
        as = new Socket("localhost", s.getPort());
        ObjectOutputStream aos = new ObjectOutputStream(as.getOutputStream());
        aos.flush();
        ais = new ObjectInputStream(as.getInputStream());
        aos.writeInt(Protocol.ASYNC);
        aos.writeObject(sidQueSeUne);
        aos.flush();

        // un hilo lee las alertas que empuja el Backend (como hace el SocketMonitor del Frontend)
        Thread lector = new Thread(() -> {
            try {
                while (true) {
                    int tipo = ais.readInt();
                    if (tipo == Protocol.DELIVER_RESERVA) {
                        alertas.add((Reserva) ais.readObject());
                    }
                }
            } catch (Exception ex) {
                // la conexión se cerró: fin de la prueba
            }
        });
        lector.setDaemon(true);
        lector.start();
    }

    /** Manda una operación con sus parámetros y lee la respuesta (sin entidad). */
    Resultado pedir(int operacion, Object... parametros) throws Exception {
        return pedir(operacion, false, parametros);
    }

    /** Para los guardar*: la respuesta trae además la entidad con el id que puso el Backend. */
    Resultado pedirGuardar(int operacion, Object... parametros) throws Exception {
        return pedir(operacion, true, parametros);
    }

    private Resultado pedir(int operacion, boolean conEntidad, Object[] parametros) throws Exception {
        os.writeInt(operacion);
        for (Object p : parametros) {
            os.writeObject(p);
        }
        os.flush();
        os.reset();
        int codigo = is.readInt();
        Object valor = is.readObject();
        Object entidad = codigo == Protocol.ERROR_NO_ERROR && conEntidad ? is.readObject() : null;
        return new Resultado(codigo, valor, entidad);
    }

    /** Espera una alerta hasta 'milisegundos'; null si no llega ninguna. */
    Reserva esperarAlerta(int milisegundos) throws Exception {
        return alertas.poll(milisegundos, TimeUnit.MILLISECONDS);
    }

    void desconectar() throws IOException {
        os.writeInt(Protocol.DISCONNECT);
        os.flush();
    }

    @Override
    public void close() {
        try {
            s.close();
            if (as != null) as.close();
        } catch (IOException ignored) {
        }
    }
}
