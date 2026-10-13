package reserva.presentation.Tabs;

class TabsController {
    private final TabsView view;
    private final TabsModel model;

    public TabsController(TabsView view) {
        this.view = view;
        this.model = new TabsModel();
    }

    public TabsController(TabsView view, TabsModel model) {
        this.view = view;
        this.model = model;
    }

    public TabsView getView() {
        return view;
    }

    public TabsModel getModel() {
        return model;
    }
}
