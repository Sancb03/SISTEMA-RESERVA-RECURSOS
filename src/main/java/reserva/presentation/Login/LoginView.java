package reserva.presentation.Login;

import reserva.presentation.Login.CambiarClave.CambiarClaveView;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class LoginView extends JFrame implements PropertyChangeListener {

    // Componentes creados por el .form (IntelliJ GUI Designer) - no se instancian a mano,
    // el diseñador los llena en tiempo de compilación a partir de LoginView.form
    private JPanel panel1;
    private JLabel idLabel;
    private JLabel claveLabel;
    private JTextField id_tField;
    private JPasswordField clave_tField;
    private JButton Visibilidadbutton;
    private JButton ingresarButton;
    private JButton cancelarButton;
    private JButton cambiarClaveButton;

    private LoginModel model;
    private LoginController controller;
    private CambiarClaveView cambiarClaveView;
    private boolean claveVisible = false;

    public LoginView() {
        setTitle("Sistema de Reserva de Recursos - Ingreso");
        setContentPane(panel1);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
        setResizable(false);
        getRootPane().setDefaultButton(ingresarButton);

        model = new LoginModel();
        controller = new LoginController(this, model);
        model.addPropertyChangeListener(this);

        cambiarClaveView = new CambiarClaveView();

        ingresarButton.addActionListener(this::onIngresar);
        cancelarButton.addActionListener(e -> dispose());
        cambiarClaveButton.addActionListener(e -> cambiarClaveView.mostrarPara(id_tField.getText()));
        Visibilidadbutton.addActionListener(e -> alternarVisibilidadClave());
    }

    private void onIngresar(ActionEvent e) {
        try {
            controller.login(id_tField.getText(), new String(clave_tField.getPassword()));
            JOptionPane.showMessageDialog(panel1,
                    "Bienvenido, " + model.getUsuarioAutenticado().getNombre());
            // TODO (equipo): cuando TabsView esté implementada, aquí se debe abrir
            // la ventana principal (ya con el usuario autenticado en Sesion) y
            // cerrar este login, por ejemplo:
            //   new TabsView().setVisible(true);
            //   dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(panel1, ex.getMessage(),
                    "Error de autenticación", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void alternarVisibilidadClave() {
        claveVisible = !claveVisible;
        clave_tField.setEchoChar(claveVisible ? (char) 0 : '\u2022');
        Visibilidadbutton.setText(claveVisible ? "🙈" : "👁️");
    }

    public JPanel getPanel() {
        return panel1;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        // Reservado por si en el futuro la vista necesita reaccionar
        // a cambios del modelo (por ejemplo, refrescar algo en pantalla).
    }
}
