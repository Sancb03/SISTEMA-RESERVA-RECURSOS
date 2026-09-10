package reserva.presentation.Recursos;

import javax.swing.*;

public class RecursosView {
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

    public JComboBox getCategoriaCBox(){ return Categoria_cBox; }

    public JTextField getDescripcionTField(){ return Descripcion_tField; }

    public JButton getBuscarButton(){ return buscarButton; }

    public JButton getImprimirButton(){ return imprimirButton; }

    public JTextField getIdRecTField(){ return idRec_tField; }

    public JComboBox getCategoriaRecCBox(){ return CategoriaRec_cBox; }

    public JTextField getDescripcion_tField(){ return DescripcionRec_tField; }

    public JButton getGuardarButton(){ return guardarButton; }

    public JButton getBorrarButton(){ return borrarButton; }

    public JButton getLimpiarButton(){ return limpiarButton; }

    public JTable getListadoTable(){ return Listadotable; }

    public JScrollPane getListadoScroll(){ return ListadoScroll; }
}
