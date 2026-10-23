package reserva.red;

import java.io.Serializable;
import java.util.Arrays;

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
