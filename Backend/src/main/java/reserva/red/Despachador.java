package reserva.red;

import reserva.logic.Service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;


public class Despachador {

    /** Excepciones que sí se le avisan al Frontend con su tipo exacto. */
    private static final Set<String> TIPOS_CONOCIDOS = Set.of(
            SecurityException.class.getName(),
            IllegalStateException.class.getName(),
            IllegalArgumentException.class.getName());

    private final Service service;

    public Despachador(Service service) {
        this.service = service;
    }

    public Respuesta despachar(Peticion peticion) {
        try {
            Method metodo = buscarMetodo(peticion);
            Object resultado = metodo.invoke(service, peticion.getParametros());
            if (peticion.getOperacion().startsWith("guardar") && peticion.getParametros().length > 0) {
                // los guardar* le ponen el id (u otros datos) a la entidad recibida: se devuelve esa copia
                return Respuesta.exito(resultado, peticion.getParametros()[0]);
            }
            return Respuesta.exito(resultado);
        } catch (InvocationTargetException ex) {
            return errorDe(ex.getCause() != null ? ex.getCause() : ex);
        } catch (Exception ex) {
            return errorDe(ex);
        }
    }

    private Method buscarMetodo(Peticion peticion) throws NoSuchMethodException {
        int cantidad = peticion.getParametros() == null ? 0 : peticion.getParametros().length;
        for (Method m : Service.class.getMethods()) {
            if (m.getName().equals(peticion.getOperacion()) && m.getParameterCount() == cantidad) {
                return m;
            }
        }
        throw new NoSuchMethodException("Operación desconocida: " + peticion.getOperacion());
    }

    private Respuesta errorDe(Throwable ex) {
        String tipo = ex.getClass().getName();
        if (!TIPOS_CONOCIDOS.contains(tipo)) {
            // cualquier otra cosa es un error inesperado: queda en la consola del Backend
            // y el Frontend solo recibe un mensaje genérico (RuntimeException).
            if (!(ex instanceof NoSuchMethodException)) {
                ex.printStackTrace();
            }
            tipo = RuntimeException.class.getName();
        }
        return Respuesta.error(tipo, ex.getMessage());
    }
}
