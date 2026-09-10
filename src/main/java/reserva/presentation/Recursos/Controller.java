package reserva.presentation.Recursos;

import reserva.GeneradorPDF;

class RecursosController {
    private final RecursosView view;
    private final RecursosModel model;
    private final TablaModel tableModel;

    public RecursosController(RecursosView view) {
        this.view = view;
        this.model = new RecursosModel();
        this.tableModel = new TablaModel();

        configurarEventos();
    }

    public RecursosController(RecursosView view, RecursosModel model, TablaModel tableModel) {
        this.view = view;
        this.model = model;
        this.tableModel = tableModel;

        configurarEventos();
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

    private void configurarEventos(){

        view.getImprimirButton().addActionListener(e -> {GeneradorPDF.generarDesdeTabla
                (view.getListadoTable(), "Lista de Recursos", "Recursos.pdf");});
    }
}
