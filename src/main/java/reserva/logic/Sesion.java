package reserva.logic;

/**
 * Guarda el usuario que inició sesión actualmente en la aplicación.
 * Cualquier otro módulo (Tabs, Reservas, Funcionarios, etc.) puede
 * consultar Sesion.getUsuarioActual() para saber quién está usando
 * el sistema y qué rol tiene, sin depender directamente del módulo Login.
 */
public class Sesion {

    private static Usuario usuarioActual;

    private Sesion() {
        // Clase de utilidad: no se instancia
    }

    public static Usuario getUsuarioActual() {
        return usuarioActual;
    }

    public static void setUsuarioActual(Usuario usuario) {
        usuarioActual = usuario;
    }

    public static boolean hayUsuarioAutenticado() {
        return usuarioActual != null;
    }

    public static void cerrarSesion() {
        usuarioActual = null;
    }
}
