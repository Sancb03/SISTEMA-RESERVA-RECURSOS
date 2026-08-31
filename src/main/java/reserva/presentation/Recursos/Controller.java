package reserva.presentation.Recursos;

class RecursosController {
    private final RecursosView view;
    private final RecursosModel model;
    private final TablaModel tableModel;

    public RecursosController(RecursosView view) {
        this.view = view;
        this.model = new RecursosModel();
        this.tableModel = new TablaModel();
    }

    public RecursosController(RecursosView view, RecursosModel model, TablaModel tableModel) {
        this.view = view;
        this.model = model;
        this.tableModel = tableModel;
    }

    public RecursosView getView() {
        return view;
    }

    public RecursosModel getModel() {
        return model;
    }

    public TablaModel getTableModel() {
        return tableModel;
    }
}
