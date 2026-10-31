package reserva.logic;

import java.io.IOException;
import java.io.ObjectInputFilter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * El servidor del Backend. Acepta conexiones de los Frontend en un solo puerto.
 * El primer dato que manda cada conexión dice para qué es:
 *   - SYNC:  conexión de peticiones. Se le crea un Worker (con su propio hilo) y se le
 *            devuelve un identificador de sesión (sid).
 *   - ASYNC: conexión de alertas. Trae el sid y se une al Worker de esa sesión, para que
 *            el Backend pueda empujarle notificaciones cuando quiera.
 */
public class Server {

    /** Solo se aceptan objetos de nuestro dominio y tipos básicos de Java. */
    private static final ObjectInputFilter FILTRO = ObjectInputFilter.Config.createFilter(
            "reserva.**;java.lang.*;java.util.*;java.time.*;!*");

    ServerSocket srv;
    List<Worker> workers;
    IService service;
    boolean continuar;

    public Server() {
        this(Protocol.PORT, Service.instance());
    }

    public Server(int port) {
        this(port, Service.instance());
    }

    public Server(int port, IService service) {
        this.service = service;
        try {
            srv = new ServerSocket(port);
            workers = new ArrayList<Worker>();
            System.out.println("Servidor iniciado en el puerto " + srv.getLocalPort() + "...");
        } catch (IOException ex) {
            System.out.println(ex);
            throw new IllegalStateException("No se pudo abrir el puerto " + port + ": " + ex.getMessage(), ex);
        }
    }

    /** Puerto realmente abierto (útil cuando se pidió el puerto 0 en las pruebas). */
    public int getPort() {
        return srv.getLocalPort();
    }

    public void run() {
        Socket s;
        Worker worker;
        String sid;  // Session Id
        continuar = true;
        while (continuar) {
            try {
                s = srv.accept();
                System.out.println("Conexion Establecida...");
                ObjectOutputStream os = new ObjectOutputStream(s.getOutputStream());
                os.flush();
                ObjectInputStream is = new ObjectInputStream(s.getInputStream());
                is.setObjectInputFilter(FILTRO);
                int type = is.readInt();
                switch (type) {
                    case Protocol.SYNC:
                        sid = s.getRemoteSocketAddress().toString();
                        System.out.println("SYNCH: " + sid);
                        worker = new Worker(this, s, os, is, sid, service);
                        this.add(worker);
                        worker.start();
                        os.writeObject(sid); // send Session Id back
                        break;
                    case Protocol.ASYNC:
                        sid = (String) is.readObject(); // recieves Session Id
                        System.out.println("ASYNCH: " + sid);
                        join(s, os, is, sid);
                        break;
                    default:
                        s.close();
                }
                os.flush();
            } catch (IOException | ClassNotFoundException ex) {
                if (continuar) System.out.println(ex);
            }
        }
    }

    /** Deja de aceptar conexiones y cierra las que hay (para apagar el Backend o terminar una prueba). */
    public void stop() {
        continuar = false;
        try {
            srv.close();
        } catch (IOException ignored) {
        }
        List<Worker> copia;
        synchronized (workers) {
            copia = new ArrayList<>(workers);
        }
        for (Worker w : copia) {
            w.stop();
        }
    }

    public void add(Worker w) {
        synchronized (workers) {
            workers.add(w);
            System.out.println("Quedan: " + workers.size());
        }
    }

    public void remove(Worker w) {
        synchronized (workers) {
            workers.remove(w);
            System.out.println("Quedan: " + workers.size());
        }
    }

    public int cantidadDeSesiones() {
        synchronized (workers) {
            return workers.size();
        }
    }

    public void join(Socket as, ObjectOutputStream aos, ObjectInputStream ais, String sid) {
        synchronized (workers) {
            for (Worker w : workers) {
                if (w.sid.equals(sid)) {
                    w.setAs(as, aos, ais);
                    break;
                }
            }
        }
    }

    /** Avisa de una reserva nueva a TODOS los Frontend conectados menos al que la hizo. */
    public void deliver_reserva(Worker from, Reserva e) {
        synchronized (workers) {
            for (Worker w : workers) {
                if (w != from) w.deliver_reserva(e);
            }
        }
    }

}
