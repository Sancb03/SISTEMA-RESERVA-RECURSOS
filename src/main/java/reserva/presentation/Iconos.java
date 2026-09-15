package reserva.presentation;

import javax.swing.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Carga los íconos del proyecto (carpeta src/main/resources/icons) una sola vez
 * y los deja en caché. Si algún ícono no aparece (por ejemplo, en un entorno sin
 * los recursos empaquetados), devuelve null en vez de reventar: los botones
 * simplemente se ven sin ícono, sin tumbar la aplicación.
 */
public final class Iconos {

    private static final Map<String, ImageIcon> CACHE = new HashMap<>();

    private Iconos() {
    }

    /** @param nombre sin extensión ni carpeta, ej: "save", "cancel", "pdf" */
    public static ImageIcon get(String nombre) {
        return CACHE.computeIfAbsent(nombre, Iconos::cargar);
    }

    private static ImageIcon cargar(String nombre) {
        URL recurso = Iconos.class.getResource("/icons/" + nombre + ".png");
        return (recurso != null) ? new ImageIcon(recurso) : null;
    }
}
