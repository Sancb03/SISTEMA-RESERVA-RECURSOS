package reserva.logic;


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
