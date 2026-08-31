package reserva.presentation.Calendario;

class CalendarioController {
    private final CalendarioRecursosView recursosView;
    private final CalendarioActividadesView actividadesView;
    private final CalendarioModel model;
    private final TablaModel tableModel;

    public CalendarioController(CalendarioRecursosView recursosView, CalendarioActividadesView actividadesView) {
        this.recursosView = recursosView;
        this.actividadesView = actividadesView;
        this.model = new CalendarioModel();
        this.tableModel = new TablaModel();
    }

    public CalendarioController(CalendarioRecursosView recursosView, CalendarioActividadesView actividadesView, CalendarioModel model, TablaModel tableModel) {
        this.recursosView = recursosView;
        this.actividadesView = actividadesView;
        this.model = model;
        this.tableModel = tableModel;
    }

    public CalendarioRecursosView getRecursosView() {
        return recursosView;
    }

    public CalendarioActividadesView getActividadesView() {
        return actividadesView;
    }

    public CalendarioModel getModel() {
        return model;
    }

    public TablaModel getTableModel() {
        return tableModel;
    }
}
