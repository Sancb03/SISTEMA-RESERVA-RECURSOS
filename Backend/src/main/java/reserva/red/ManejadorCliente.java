package reserva.red;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputFilter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;

/**
 * Un hilo por cada conexión que llega al Servidor. Lo primero que manda el
 * Frontend es para qué es la conexión (ver Protocolo):
 *
 *  - PETICIONES: ciclo "leo una Peticion -> la ejecuto con el Despachador ->
 *                escribo la Respuesta", hasta que el Frontend cierre.
 *  - ALERTAS:    el Frontend dice quién es (su id de usuario), se registra en
 *                RegistroAlertas, y este hilo solo se queda esperando a que la
 *                conexión se cierre. Las Notificacion las escribe el registro.
 */
public class ManejadorCliente implements Runnable {

    /** Solo se aceptan objetos de nuestro propio paquete y de java.lang/util/time. */
    private static final ObjectInputFilter FILTRO = ObjectInputFilter.Config.createFilter(
            "reserva.**;java.lang.*;java.util.*;java.time.*;!*");

    private final Socket socket;
    private final Despachador despachador;
    private final RegistroAlertas registro;

    public ManejadorCliente(Socket socket, Despachador despachador, RegistroAlertas registro) {
        this.socket = socket;
        this.despachador = despachador;
        this.registro = registro;
    }

    @Override
    public void run() {
        try (socket) {
            // Orden importante: primero el OutputStream (y flush), luego el InputStream;
            // el Frontend hace lo mismo, así ninguno se queda esperando el encabezado del otro.
            ObjectOutputStream salida = new ObjectOutputStream(socket.getOutputStream());
            salida.flush();
            ObjectInputStream entrada = new ObjectInputStream(socket.getInputStream());
            entrada.setObjectInputFilter(FILTRO);

            Object canal = entrada.readObject();
            if (Protocolo.CANAL_PETICIONES.equals(canal)) {
                atenderPeticiones(entrada, salida);
            } else if (Protocolo.CANAL_ALERTAS.equals(canal)) {
                atenderAlertas(entrada, salida);
            }
        } catch (EOFException | SocketException ex) {
            // el Frontend se desconectó: es lo normal al cerrar la aplicación
        } catch (IOException | ClassNotFoundException ex) {
            System.err.println("Conexión terminada con error: " + ex);
        }
    }

    private void atenderPeticiones(ObjectInputStream entrada, ObjectOutputStream salida)
            throws IOException, ClassNotFoundException {
        while (true) {
            Peticion peticion = (Peticion) entrada.readObject();
            Respuesta respuesta = despachador.despachar(peticion);
            salida.writeObject(respuesta);
            salida.flush();
            salida.reset(); // sin esto el stream reenvía copias viejas de objetos que cambiaron
        }
    }

    private void atenderAlertas(ObjectInputStream entrada, ObjectOutputStream salida)
            throws IOException, ClassNotFoundException {
        int usuarioId = (Integer) entrada.readObject();
        registro.agregar(socket, salida, usuarioId);
        try {
            // nada más llega por este canal: readObject solo "despierta" cuando el Frontend cierra
            while (true) {
                entrada.readObject();
            }
        } finally {
            registro.quitar(salida);
        }
    }
}
