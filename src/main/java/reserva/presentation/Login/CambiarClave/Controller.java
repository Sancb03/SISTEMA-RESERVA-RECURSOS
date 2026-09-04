package reserva.presentation.Login.CambiarClave;

import reserva.data.UsuarioDao;
import reserva.logic.Usuario;

class CambiarClaveController {

    private final CambiarClaveView view;
    private final CambiarClaveModel model;
    private final UsuarioDao usuarioDao;

    CambiarClaveController(CambiarClaveView view) {
        this(view, new CambiarClaveModel());
    }

    CambiarClaveController(CambiarClaveView view, CambiarClaveModel model) {
        this.view = view;
        this.model = model;
        this.usuarioDao = new UsuarioDao();
    }

    CambiarClaveView getView() {
        return view;
    }

    CambiarClaveModel getModel() {
        return model;
    }

    /**
     * Cambia la clave del usuario identificado por 'identificacion'.
     * Lanza Exception con un mensaje entendible por el usuario si algo falla.
     */
    void cambiarClave(String identificacion, String claveActual, String claveNueva, String confirmacion) throws Exception {
        if (identificacion == null || identificacion.isBlank()) {
            throw new Exception("Primero escriba su identificación en la pantalla de ingreso.");
        }
        if (claveNueva == null || claveNueva.length() < 6) {
            throw new Exception("La nueva clave debe tener al menos 6 caracteres.");
        }
        if (!claveNueva.equals(confirmacion)) {
            throw new Exception("La confirmación no coincide con la nueva clave.");
        }

        Usuario usuario = usuarioDao.buscarPorIdentificacion(identificacion.trim());
        if (usuario == null) {
            throw new Exception("No se encontró un usuario con esa identificación.");
        }
        if (!usuario.getClave().equals(claveActual)) {
            throw new Exception("La clave actual no coincide.");
        }

        usuario.setClave(claveNueva);
        usuarioDao.actualizar(usuario);
    }
}