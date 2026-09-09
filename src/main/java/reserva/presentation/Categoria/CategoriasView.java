package reserva.presentation.Categoria;

import javax.swing.*;

public class CategoriasView {
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

    public JPanel getCategoriaPanel() {
        return CategoriaPanel;
    }

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
}
