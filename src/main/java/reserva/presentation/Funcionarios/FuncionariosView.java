package reserva.presentation.Funcionarios;

import reserva.logic.Funcionario;
import reserva.logic.Usuario;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class FuncionariosView implements PropertyChangeListener {

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

    public FuncionariosView(Usuario usuarioActual) {

        model = new FuncionariosModel();

        controller = new FuncionariosController(
                this,
                model,
                usuarioActual
        );

        buscarButton.addActionListener(e -> controller.buscar());

        guardarButton.addActionListener(e -> {
            Funcionario f = take();
            controller.guardar(f);
        });

        borrarButton.addActionListener(e ->
                controller.borrar(idFun_tField.getText())
        );

        limpiarButton.addActionListener(e ->
                controller.limpiar()
        );

        imprimirButton.addActionListener(e ->
                controller.generarPDF()
        );
    }

    public Funcionario take() {

        Funcionario f = new Funcionario();

        f.setId(Integer.parseInt(idFun_tField.getText().trim()));
        f.setNombre(NombreFun_tField.getText().trim());
        f.setTelefono(TelefonoFun_tField.getText().trim());

        return f;
    }

    public void setController(FuncionariosController controller) {
        this.controller = controller;
    }

    public void setModel(FuncionariosModel model) {
        this.model = model;
        model.addPropertyChangeListener(this);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {

        switch (evt.getPropertyName()) {

            case FuncionariosModel.LIST:

                int[] cols = {
                        TablaModel.ID,
                        TablaModel.NOMBRE,
                        TablaModel.TELEFONO
                };

                Listadotable.setModel(
                        new TablaModel(
                                cols,
                                model.getListado()
                        )
                );

                break;

            case FuncionariosModel.CURRENT:

                Funcionario actual = model.getCurrent();

                if (actual != null) {

                    idFun_tField.setText(
                            String.valueOf(actual.getId())
                    );

                    NombreFun_tField.setText(
                            actual.getNombre()
                    );

                    TelefonoFun_tField.setText(
                            actual.getTelefono()
                    );
                }

                break;
        }

        panel1.revalidate();
        panel1.repaint();
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
}