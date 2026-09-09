package reserva.presentation.Funcionarios;

import reserva.logic.Usuario;

import javax.swing.*;

public class FuncionariosView {

    private JPanel panel1;
    private JTextField id_tField;
    private JTextField Nombre_tField;
    private JButton buscarButton;
    private JButton imprimirButton;
    private JButton guardarButton;
    private JButton borrarButton;
    private JButton limpiarButton;
    private JTextField idFun_tField;
    private JTextField NombreFun_tField;
    private JTextField TelefonoFun_tField;
    private JTable Listadotable;
    private JPanel BusquedaPanel;
    private JPanel FuncionariosPanel;
    private JPanel ListadoPanel;
    private JScrollPane ListadoScroll;
    private JLabel idLabel;
    private JLabel NombreLabel;
    private JLabel idFunLabel;
    private JLabel NombreFunLabel;
    private JLabel TelefonoFunLabel;
    private JPanel buttonsPanel;


    private FuncionariosModel model;
    private FuncionariosController controller;
    private TablaModel tableModel;


    public FuncionariosView(Usuario usuarioActual) {

        model = new FuncionariosModel();
        tableModel = new TablaModel();

        controller = new FuncionariosController(
                this, model, tableModel, usuarioActual
        );
        Listadotable.setModel(tableModel);
    }


    public JPanel getPanel1() {
        return panel1;
    }

    public JTextField getIdTField() {
        return id_tField;
    }

    public JTextField getNombreTField() {
        return Nombre_tField;
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

    public JTextField getIdFunTField() {
        return idFun_tField;
    }

    public JTextField getNombreFunTField() {
        return NombreFun_tField;
    }

    public JTextField getTelefonoFunTField() {
        return TelefonoFun_tField;
    }

    public JTable getListadotable() {
        return Listadotable;
    }



    public FuncionariosModel getModel() {
        return model;
    }
    public FuncionariosController getController() {
        return controller;
    }
    public TablaModel getTableModel() {
        return tableModel;
    }

}

