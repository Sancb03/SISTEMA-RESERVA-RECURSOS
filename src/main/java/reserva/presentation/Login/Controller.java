package reserva.presentation.Login;

import reserva.data.Data;
import reserva.logic.Sesion;
import reserva.logic.Usuario;

class LoginController {

    private final LoginView view;
    private final LoginModel model;
    private final Data data;

    LoginController(LoginView view) {
        this(view, new LoginModel());
    }

    LoginController(LoginView view, LoginModel model) {
        this.view = view;
        this.model = model;
        this.data = Data.instance();
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

        Usuario usuario = data.buscarUsuarioPorIdentificacion(identificacion.trim());
        if (usuario == null) {
            throw new Exception("No existe un usuario con esa identificación.");
        }
        if (!usuario.getClave().equals(clave)) {
            throw new Exception("La clave es incorrecta.");
        }

        Sesion.setUsuarioActual(usuario);
        model.setUsuarioAutenticado(usuario);
    }
}