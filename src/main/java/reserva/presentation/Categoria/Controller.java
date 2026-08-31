package reserva.presentation.Categoria;

class CategoriaController {
    private final CategoriasView view;
    private final CategoriaModel model;
    private final TablaModel tableModel;

    public CategoriaController(CategoriasView view) {
        this.view = view;
        this.model = new CategoriaModel();
        this.tableModel = new TablaModel();
    }

    public CategoriaController(CategoriasView view, CategoriaModel model, TablaModel tableModel) {
        this.view = view;
        this.model = model;
        this.tableModel = tableModel;
    }

    public CategoriasView getView() {
        return view;
    }

    public CategoriaModel getModel() {
        return model;
    }

    public TablaModel getTableModel() {
        return tableModel;
    }
}
