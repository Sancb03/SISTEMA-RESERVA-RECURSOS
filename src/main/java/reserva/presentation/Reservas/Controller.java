package reserva.presentation.Reservas;

import reserva.GeneradorPDF;

class ReservasController {
    private final ReservasView view;
    private final ReservasModel model;
    private final TablaModel tableModel;

    public ReservasController(ReservasView view) {
        this.view = view;
        this.model = new ReservasModel();
        this.tableModel = new TablaModel();

        configurarEventos();
    }

    public ReservasController(ReservasView view, ReservasModel model, TablaModel tableModel) {
        this.view = view;
        this.model = model;
        this.tableModel = tableModel;

        configurarEventos();
    }

    private void configurarEventos() {
        view.getImprimirButton().addActionListener(e -> {GeneradorPDF.generarDesdeTabla
                (view.getMiReservaTable(), "Mis Reservas", "MisReservas.pdf");});
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
