package reserva.red;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Lo que viaja del Frontend al Backend por el socket, empacado con
 * ObjectOutputStream. "operacion" es el nombre del método de Service
 * que se quiere invocar (ej. "login", "guardarCategoria"), y
 * "parametros" son los argumentos, en el mismo orden que ese método.
 *
 * El ServiceProxy arma una Peticion por cada método del Service y el
 * Backend la despacha por reflexión contra su ServiceImpl (ver
 * ManejadorCliente en el Backend) - así no hay que tocar el protocolo
 * cada vez que se agrega un método nuevo.
 */
public class Peticion implements Serializable {

    private final String operacion;
    private final Object[] parametros;

    public Peticion(String operacion, Object... parametros) {
        this.operacion = operacion;
        this.parametros = (parametros != null) ? parametros : new Object[0];
    }

    public String getOperacion() {
        return operacion;
    }

    public Object[] getParametros() {
        return parametros;
    }

    @Override
    public String toString() {
        return operacion + Arrays.toString(parametros);
    }
}
