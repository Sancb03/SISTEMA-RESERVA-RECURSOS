package reserva.presentation.Categoria;

import reserva.logic.Categoria;
import reserva.logic.Usuario;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class CategoriasView implements PropertyChangeListener {

    private JTextField Descripcion_tField;
    private JButton buscarButton;
    private JButton imprimirButton;
    private JButton guardarButton;
    private JButton borrarButton;
    private JButton limpiarButton;
    private JTextField id_tField;
    private JTextField descripcion_tField;
    private JTable Categoriatable;
    private JPanel BusquedaPanel;
    private JLabel DescripcionLabel;
    private JPanel CategoriaPanel;
    private JLabel idLabel;
    private JLabel descripcionLabel;
    private JScrollPane ListadoScroll;
    private JPanel panelPrincipal;

    private CategoriaModel model;
    private CategoriaController controller;

    public CategoriasView(Usuario usuarioActual) {

        model = new CategoriaModel();

        controller = new CategoriaController(
                this,
                model,
                usuarioActual
        );

        buscarButton.addActionListener(e ->
                controller.buscar()
        );

        guardarButton.addActionListener(e ->
                controller.guardar(take())
        );

        borrarButton.addActionListener(e ->
                controller.borrar()
        );

        limpiarButton.addActionListener(e ->
                controller.limpiar()
        );

        imprimirButton.addActionListener(e ->
                controller.generarPDF()
        );

        Categoriatable.getSelectionModel()
                .addListSelectionListener(e -> {

                    if (!e.getValueIsAdjusting()) {
                        controller.seleccionarCategoria(
                                Categoriatable.getSelectedRow()
                        );
                    }
                });
    }

    public Categoria take() {

        Categoria c = new Categoria();

        c.setId(
                id_tField.getText().trim()
        );

        c.setDescripcion(
                descripcion_tField.getText().trim()
        );

        return c;
    }

    public void setController(CategoriaController controller) {
        this.controller = controller;
    }

    public void setModel(CategoriaModel model) {
        this.model = model;
        model.addPropertyChangeListener(this);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {

        switch (evt.getPropertyName()) {

            case CategoriaModel.LIST:

                int[] cols = {
                        TablaModel.ID,
                        TablaModel.DESCRIPCION
                };

                Categoriatable.setModel(
                        new TablaModel(
                                cols,
                                model.getListado()
                        )
                );

                break;

            case CategoriaModel.CURRENT:

                Categoria actual = model.getCurrent();

                if (actual != null) {

                    id_tField.setText(
                            actual.getId() == null
                                    ? ""
                                    : actual.getId()
                    );

                    descripcion_tField.setText(
                            actual.getDescripcion() == null
                                    ? ""
                                    : actual.getDescripcion()
                    );
                }

                break;
        }

        CategoriaPanel.revalidate();
        CategoriaPanel.repaint();
    }

    public JPanel getCategoriaPanel() {
        return CategoriaPanel;
    }

    public JPanel getPanelPrincipal() { return panelPrincipal; }

    public JTextField getDescripcionBusquedaTField() {
        return Descripcion_tField;
    }

    public JButton getBuscarButton() {
        return buscarButton;
    }

    public JButton getImprimirButton() {
        return imprimirButton;
    }

    public JButton getGuardarButton() {
        return guardarButton;
    }

    public JButton getBorrarButton() {
        return borrarButton;
    }

    public JButton getLimpiarButton() {
        return limpiarButton;
    }

    public JTextField getIdTField() {
        return id_tField;
    }

    public JTextField getDescripcionTField() {
        return descripcion_tField;
    }

    public JTable getCategoriatable() {
        return Categoriatable;
    }

    public CategoriaModel getModel() {
        return model;
    }

    public CategoriaController getController() {
        return controller;
    }
}