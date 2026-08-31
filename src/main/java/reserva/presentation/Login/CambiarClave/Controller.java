package reserva.presentation.Login.CambiarClave;

class CambiarClaveController {
    private final CambiarClaveView view;
    private final CambiarClaveModel model;

    public CambiarClaveController(CambiarClaveView view) {
        this.view = view;
        this.model = new CambiarClaveModel();
    }

    public CambiarClaveController(CambiarClaveView view, CambiarClaveModel model) {
        this.view = view;
        this.model = model;
    }

    public CambiarClaveView getView() {
        return view;
    }

    public CambiarClaveModel getModel() {
        return model;
    }
}
