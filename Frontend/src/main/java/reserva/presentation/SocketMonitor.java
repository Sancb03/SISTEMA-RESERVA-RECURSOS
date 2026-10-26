package reserva.presentation;

import reserva.logic.Protocol;
import reserva.logic.Reserva;
import reserva.logic.Service;

import javax.swing.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Conexión ASINCRÓNICA con el Backend: se une a la sesión del Frontend con su sid y un hilo
 * se queda escuchando. Cada vez que el Backend avisa de una reserva nueva (de otro usuario),
 * se la entrega al SocketListener en el hilo de Swing.
 */
public class SocketMonitor {
    SocketListener listener;
    String sid; // Session Id
    Socket as; // Asynchronous Socket
    ObjectOutputStream aos;
    ObjectInputStream ais;

    public SocketMonitor(SocketListener listener, String sid) throws Exception {
        this(listener, sid, Service.instance().getHost(), Service.instance().getPort());
    }

    public SocketMonitor(SocketListener listener, String sid, String host, int port) throws Exception {
        this.listener = listener;
        this.sid = sid;
        as = new Socket(host, port);
        aos = new ObjectOutputStream(as.getOutputStream());
        aos.flush();
        ais = new ObjectInputStream(as.getInputStream());
        aos.writeInt(Protocol.ASYNC);
        aos.writeObject(sid);
        aos.flush();
    }

    boolean condition = true;
    private Thread t;

    public void start() {
        t = new Thread(new Runnable() {
            public void run() {
                listen();
            }
        });
        t.setDaemon(true); // que no impida cerrar la aplicación
        condition = true;
        t.start();
    }

    public void stop() {
        condition = false;
        try {
            as.close(); // despierta al hilo, que sale por la IOException
        } catch (IOException ignored) {
        }
    }

    public void listen() {
        int method;
        while (condition) {
            try {
                method = ais.readInt();
                switch (method) {
                    case Protocol.DELIVER_RESERVA:
                        try {
                            Reserva e = (Reserva) ais.readObject();
                            SwingUtilities.invokeLater(new Runnable() {
                                public void run() {
                                    listener.deliver_reserva(e);
                                }
                            });
                        } catch (ClassNotFoundException ex) {
                        }
                        break;
                }
            } catch (IOException ex) {
                condition = false;
            }
        }
        try {
            as.shutdownOutput();
            as.close();
        } catch (IOException e) {
        }
    }
}
