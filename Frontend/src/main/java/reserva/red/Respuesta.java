package reserva.red;

import java.io.Serializable;

/**
 * Lo que el Backend contesta por cada Peticion.
 *
 * Si exito=true, "resultado" trae lo que haya devuelto el método del Service
 * (puede ser null si era void). Si exito=false, el Proxy relanza una excepción
 * con tipoError/mensajeError para que el Controller la atrape exactamente igual
 * que atrapaba las de Data.java.
 *
 * "entidadActualizada" resuelve una diferencia entre llamar un método local y
 * llamarlo por la red: en el Proyecto 1, guardarCategoria(c) le ponía el id
 * (CAT-000004) al MISMO objeto c que tenía el Controller. Por la red el Backend
 * trabaja con una COPIA, así que en los métodos guardar* devuelve esa copia ya
 * modificada (con el id generado, la clave inicial, etc.) y el ServiceProxy
 * se la copia de vuelta al objeto original. Así los Controllers no se enteran.
 */
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
