package reserva;

import reserva.data.Database;
import reserva.logic.Service;
import reserva.logic.ServiceImpl;
import reserva.red.Despachador;
import reserva.red.ManejadorCliente;
import reserva.red.Protocolo;
import reserva.red.RegistroAlertas;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * El Backend: un proceso SIN interfaz gráfica que escucha en un puerto.
 * Por cada Frontend que se conecta crea un hilo (ManejadorCliente) que
 * atiende sus peticiones, y cuando alguien crea una reserva les avisa a los
 * demás por el canal de alertas.
 *
 * Se arranca con:  java -jar RESERVA-BACKEND.jar [puerto]   (por defecto 5000)
 */
public class Servidor {

    private final int puertoSolicitado;
    private final Despachador despachador;
    private final RegistroAlertas registro;

    private ServerSocket serverSocket;
    private Thread hiloAceptador;
    private final Set<Socket> conexiones = ConcurrentHashMap.newKeySet();

    public Servidor(int puerto, Service service, RegistroAlertas registro) {
        this.puertoSolicitado = puerto;
        this.despachador = new Despachador(service);
        this.registro = registro;
    }

    /** Abre el puerto y empieza a aceptar Frontends en un hilo aparte. @return el puerto realmente abierto. */
    public int iniciar() throws IOException {
        serverSocket = new ServerSocket(puertoSolicitado);
        hiloAceptador = new Thread(this::aceptarConexiones, "servidor-aceptador");
        hiloAceptador.setDaemon(true);
        hiloAceptador.start();
        return serverSocket.getLocalPort();
    }

    private void aceptarConexiones() {
        while (!serverSocket.isClosed()) {
            try {
                Socket socket = serverSocket.accept();
                conexiones.add(socket);
                Thread hilo = new Thread(() -> {
                    try {
                        new ManejadorCliente(socket, despachador, registro).run();
                    } finally {
                        conexiones.remove(socket);
                    }
                }, "cliente-" + socket.getRemoteSocketAddress());
                hilo.setDaemon(true);
                hilo.start();
            } catch (SocketException ex) {
                break; // detener() cerró el ServerSocket
            } catch (IOException ex) {
                System.err.println("Error aceptando una conexión: " + ex);
            }
        }
    }

    public void detener() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException ignored) {
        }
        for (Socket s : conexiones) {
            try {
                s.close();
            } catch (IOException ignored) {
            }
        }
    }

    public static void main(String[] args) throws Exception {
        int puerto = args.length > 0 ? Integer.parseInt(args[0]) : Protocolo.PUERTO_POR_DEFECTO;

        try {
            Database.instance(); // si MySQL no está disponible, que falle YA y con mensaje claro
        } catch (RuntimeException ex) {
            System.err.println(ex.getMessage());
            System.exit(1);
        }

        ServiceImpl service = new ServiceImpl();
        RegistroAlertas registro = new RegistroAlertas();
        service.agregarOyenteNuevaReserva(registro::difundirNuevaReserva);

        Servidor servidor = new Servidor(puerto, service, registro);
        int abierto = servidor.iniciar();
        System.out.println("Backend escuchando en el puerto " + abierto + ". Ctrl+C para detenerlo.");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            servidor.detener();
            Database.instance().close();
        }));
        servidor.hiloAceptador.join();
    }
}
