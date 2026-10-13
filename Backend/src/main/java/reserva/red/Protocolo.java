package reserva.red;

/**
 * Constantes del "saludo" que hace el Frontend apenas abre un socket contra
 * el Backend. Como el Backend escucha en UN solo puerto, lo primero que el
 * Frontend manda (un String con ObjectOutputStream) es para qué va a usar
 * esa conexión:
 *
 *   CANAL_PETICIONES: conexión síncrona. Luego el Frontend manda Peticion
 *                     y el Backend contesta con una Respuesta, una a una.
 *
 *   CANAL_ALERTAS:    conexión que solo recibe. Luego el Frontend manda un
 *                     Integer (el id del usuario que inició sesión) y de
 *                     ahí en adelante el Backend le empuja Notificacion
 *                     cada vez que OTRO usuario crea una reserva.
 */
public final class Protocolo {

    public static final String CANAL_PETICIONES = "PETICIONES";
    public static final String CANAL_ALERTAS = "ALERTAS";

    public static final int PUERTO_POR_DEFECTO = 5000;

    private Protocolo() {
    }
}
