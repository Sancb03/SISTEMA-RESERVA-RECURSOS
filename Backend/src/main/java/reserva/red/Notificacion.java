package reserva.red;

import java.io.Serializable;

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
