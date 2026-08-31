package reserva.presentation.Funcionarios;

class FuncionariosController {
    private final FuncionariosView view;
    private final FuncionariosModel model;
    private final TablaModel tableModel;

    public FuncionariosController(FuncionariosView view) {
        this.view = view;
        this.model = new FuncionariosModel();
        this.tableModel = new TablaModel();
    }

    public FuncionariosController(FuncionariosView view, FuncionariosModel model, TablaModel tableModel) {
        this.view = view;
        this.model = model;
        this.tableModel = tableModel;
    }

    public FuncionariosView getView() {
        return view;
    }

    public FuncionariosModel getModel() {
        return model;
    }

    public TablaModel getTableModel() {
        return tableModel;
    }
}
