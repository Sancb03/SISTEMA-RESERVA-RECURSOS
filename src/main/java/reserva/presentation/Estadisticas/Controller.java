package reserva.presentation.Estadisticas;

class EstadisticasController {
    private final EstadisticasView view;
    private final EstadisticasModel model;
    private final TablaModel tableModel;

    public EstadisticasController(EstadisticasView view) {
        this.view = view;
        this.model = new EstadisticasModel();
        this.tableModel = new TablaModel();
    }

    public EstadisticasController(EstadisticasView view, EstadisticasModel model, TablaModel tableModel) {
        this.view = view;
        this.model = model;
        this.tableModel = tableModel;
    }

    public EstadisticasView getView() {
        return view;
    }

    public EstadisticasModel getModel() {
        return model;
    }

    public TablaModel getTableModel() {
        return tableModel;
    }
}
