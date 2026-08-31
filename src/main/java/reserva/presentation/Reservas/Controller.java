package reserva.presentation.Reservas;

class ReservasController {
    private final ReservasView view;
    private final ReservasModel model;
    private final TablaModel tableModel;

    public ReservasController(ReservasView view) {
        this.view = view;
        this.model = new ReservasModel();
        this.tableModel = new TablaModel();
    }

    public ReservasController(ReservasView view, ReservasModel model, TablaModel tableModel) {
        this.view = view;
        this.model = model;
        this.tableModel = tableModel;
    }

    public ReservasView getView() {
        return view;
    }

    public ReservasModel getModel() {
        return model;
    }

    public TablaModel getTableModel() {
        return tableModel;
    }
}
