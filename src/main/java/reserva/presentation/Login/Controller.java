package reserva.presentation.Login;

class LoginController {
    private final LoginView view;
    private final LoginModel model;

    public LoginController(LoginView view) {
        this.view = view;
        this.model = new LoginModel();
    }

    public LoginController(LoginView view, LoginModel model) {
        this.view = view;
        this.model = model;
    }

    public LoginView getView() {
        return view;
    }

    public LoginModel getModel() {
        return model;
    }
}
