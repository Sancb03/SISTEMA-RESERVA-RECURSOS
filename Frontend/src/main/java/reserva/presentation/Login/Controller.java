package reserva.presentation.Login;

import reserva.logic.Service;
import reserva.logic.Sesion;
import reserva.logic.Usuario;

class LoginController {

    private final LoginView view;
    private final LoginModel model;
    private final Service service;

    LoginController(LoginView view) {
        this(view, new LoginModel());
    }

    LoginController(LoginView view, LoginModel model) {
        this.view = view;
        this.model = model;
        this.service = Service.instance();
    }

    LoginView getView() {
        return view;
    }

    LoginModel getModel() {
        return model;
    }

    void login(String identificacion, String clave) throws Exception {
        if (identificacion == null || identificacion.isBlank()) {
            throw new Exception("Debe indicar su identificación.");
        }
        if (clave == null || clave.isBlank()) {
            throw new Exception("Debe indicar su clave.");
        }

        Usuario usuario;
        try {
            usuario = service.autenticar(identificacion.trim(), clave);
        } catch (RuntimeException ex) {
            // el Backend explica que paso (no existe, clave incorrecta, sin conexion...)
            throw new Exception(ex.getMessage());
        }

        Sesion.setUsuarioActual(usuario);
        model.setUsuarioAutenticado(usuario);
    }
}