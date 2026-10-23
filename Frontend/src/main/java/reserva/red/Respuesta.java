package reserva.red;

import java.io.Serializable;

public class Respuesta implements Serializable {

    private final boolean exito;
    private final Object resultado;
    private final Object entidadActualizada;
    private final String tipoError;
    private final String mensajeError;

    private Respuesta(boolean exito, Object resultado, Object entidadActualizada,
                      String tipoError, String mensajeError) {
        this.exito = exito;
        this.resultado = resultado;
        this.entidadActualizada = entidadActualizada;
        this.tipoError = tipoError;
        this.mensajeError = mensajeError;
    }

    public static Respuesta exito(Object resultado) {
        return new Respuesta(true, resultado, null, null, null);
    }

    public static Respuesta exito(Object resultado, Object entidadActualizada) {
        return new Respuesta(true, resultado, entidadActualizada, null, null);
    }

    public static Respuesta error(String tipoError, String mensajeError) {
        return new Respuesta(false, null, null, tipoError, mensajeError);
    }

    public boolean isExito() {
        return exito;
    }

    public Object getResultado() {
        return resultado;
    }

    public Object getEntidadActualizada() {
        return entidadActualizada;
    }

    public String getTipoError() {
        return tipoError;
    }

    public String getMensajeError() {
        return mensajeError;
    }
}
