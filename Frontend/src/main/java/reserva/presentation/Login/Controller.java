package reserva.presentation.Login;

import reserva.logic.Service;
import reserva.logic.ServiceProxy;
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
        this.service = ServiceProxy.instance();
    }

    LoginView getView() {
        return view;
    }

    LoginModel getModel() {
        return model;
    }

    void login(String identificacion, String clave) throws Exception {
        Usuario usuario = service.login(identificacion, clave);

        Sesion.setUsuarioActual(usuario);
        model.setUsuarioAutenticado(usuario);
    }
}