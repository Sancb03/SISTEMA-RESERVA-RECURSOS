package reserva.presentation.Recursos;

import reserva.logic.Categoria;
import reserva.logic.Usuario;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class RecursosView {

    // Componentes creados por el .form (IntelliJ GUI Designer)
    private JPanel panel1;
    private JComboBox Categoria_cBox;
    private JTextField Descripcion_tField;
    private JButton buscarButton;
    private JButton imprimirButton;
    private JTextField idRec_tField;
    private JComboBox CategoriaRec_cBox;
    private JTextField DescripcionRec_tField;
    private JButton guardarButton;
    private JButton borrarButton;
    private JButton limpiarButton;
    private JTable Listadotable;
    private JPanel FiltradoPanel;
    private JLabel CategoriaLabel;
    private JLabel DescripcionLabel;
    private JPanel RecursosPanel;
    private JLabel idRecLabel;
    private JLabel CategoriaRecLabel;
    private JLabel DescripcionRecLabel;
    private JScrollPane ListadoScroll;

    private RecursosModel model;
    private RecursosController controller;
    private TablaModel tableModel;

    public RecursosView(Usuario usuarioActual) {
        model = new RecursosModel();
        tableModel = new TablaModel();

        ListCellRenderer<Object> renderizador = new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index,
                                                            boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Categoria categoria) {
                    setText(categoria.getDescripcion());
                } else if (value == null) {
                    setText("(todas)");
                }
                return this;
            }
        };
        Categoria_cBox.setRenderer(renderizador);
        CategoriaRec_cBox.setRenderer(renderizador);

        controller = new RecursosController(this, model, tableModel, usuarioActual);
        Listadotable.setModel(tableModel);
    }

    /** Llena los dos combos de categoría: el de filtro (con opción "todas") y el del formulario. */
    void cargarCategoriasEnCombos(List<Categoria> categorias) {
        Categoria_cBox.removeAllItems();
        Categoria_cBox.addItem(null); // "(todas)", ver renderer
        for (Categoria c : categorias) {
            Categoria_cBox.addItem(c);
        }

        CategoriaRec_cBox.removeAllItems();
        for (Categoria c : categorias) {
            CategoriaRec_cBox.addItem(c);
        }
    }

    Categoria getCategoriaSeleccionadaBusqueda() {
        Object seleccionado = Categoria_cBox.getSelectedItem();
        return (seleccionado instanceof Categoria) ? (Categoria) seleccionado : null;
    }

    Categoria getCategoriaSeleccionadaRec() {
        Object seleccionado = CategoriaRec_cBox.getSelectedItem();
        return (seleccionado instanceof Categoria) ? (Categoria) seleccionado : null;
    }

    void seleccionarCategoriaRec(Categoria categoria) {
        if (categoria == null) {
            CategoriaRec_cBox.setSelectedItem(null);
            return;
        }
        for (int i = 0; i < CategoriaRec_cBox.getItemCount(); i++) {
            Object item = CategoriaRec_cBox.getItemAt(i);
            if (item instanceof Categoria c && c.getId().equals(categoria.getId())) {
                CategoriaRec_cBox.setSelectedIndex(i);
                return;
            }
        }
    }

    void limpiarCombos() {
        if (CategoriaRec_cBox.getItemCount() > 0) {
            CategoriaRec_cBox.setSelectedIndex(0);
        }
    }

    public JPanel getPanel1() {
        return panel1;
    }

    public JComboBox getCategoriaCBox() {
        return Categoria_cBox;
    }

    public JTextField getDescripcionTField() {
        return Descripcion_tField;
    }

    public JButton getBuscarButton() {
        return buscarButton;
    }

    public JButton getImprimirButton() {
        return imprimirButton;
    }

    public JTextField getIdRecTField() {
        return idRec_tField;
    }

    public JComboBox getCategoriaRecCBox() {
        return CategoriaRec_cBox;
    }

    public JTextField getDescripcionRecTField() {
        return DescripcionRec_tField;
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

    public JTable getListadotable() {
        return Listadotable;
    }

    public RecursosModel getModel() {
        return model;
    }

    public RecursosController getController() {
        return controller;
    }

    public TablaModel getTableModel() {
        return tableModel;
    }
}
