package reserva.red;

import java.io.Serializable;

/**
 * Esto NO es respuesta a ninguna Peticion: es lo que el Backend empuja
 * de manera asíncrona (sin que el Frontend lo haya pedido) cada vez que
 * otro usuario crea una reserva, para alimentar el panel de Alertas.
 *
 * Viaja por un socket aparte, dedicado solo a notificaciones (ver
 * README de la entrega) - así no se mezcla con el socket de
 * peticiones/respuestas que es síncrono (uno pregunta, uno contesta).
 */
public class Notificacion implements Serializable {

    public static final String NUEVA_RESERVA = "NUEVA_RESERVA";

    private final String tipo;
    private final Object datos;

    public Notificacion(String tipo, Object datos) {
        this.tipo = tipo;
        this.datos = datos;
    }

    public String getTipo() {
        return tipo;
    }

    public Object getDatos() {
        return datos;
    }
}
