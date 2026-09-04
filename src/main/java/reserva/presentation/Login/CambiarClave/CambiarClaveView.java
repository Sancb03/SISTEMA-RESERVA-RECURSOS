package reserva.presentation.Login.CambiarClave;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class CambiarClaveView extends JFrame {

    // Componentes creados por el .form (IntelliJ GUI Designer)
    private JPanel panel1;
    private JLabel claveActualLabel;
    private JLabel claveNuevaLabel;
    private JLabel claveConfirmarLabel;
    private JPasswordField claveActual_pField;
    private JPasswordField claveNueva_pField;
    private JPasswordField claveConfirmar_pField;
    private JButton claveActualbutton;
    private JButton claveNuevabutton;
    private JButton claveConfirmarbutton;
    private JButton guardarButton;
    private JButton cancelarButton;

    private CambiarClaveModel model;
    private CambiarClaveController controller;
    private String identificacion;

    public CambiarClaveView() {
        setTitle("Cambiar Clave");
        setContentPane(panel1);
        setSize(420, 260);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);

        model = new CambiarClaveModel();
        controller = new CambiarClaveController(this, model);

        guardarButton.addActionListener(this::onGuardar);
        cancelarButton.addActionListener(e -> {
            limpiar();
            dispose();
        });

        // Los botones "..." muestran/ocultan cada clave, igual que en el login
        claveActualbutton.addActionListener(e -> alternarVisibilidad(claveActual_pField, claveActualbutton));
        claveNuevabutton.addActionListener(e -> alternarVisibilidad(claveNueva_pField, claveNuevabutton));
        claveConfirmarbutton.addActionListener(e -> alternarVisibilidad(claveConfirmar_pField, claveConfirmarbutton));
    }

    /** Abre esta ventana para cambiar la clave del usuario con esa identificación (la que está escrita en el login). */
    public void mostrarPara(String identificacion) {
        this.identificacion = identificacion;
        limpiar();
        setVisible(true);
    }

    private void onGuardar(ActionEvent e) {
        try {
            controller.cambiarClave(
                    identificacion,
                    new String(claveActual_pField.getPassword()),
                    new String(claveNueva_pField.getPassword()),
                    new String(claveConfirmar_pField.getPassword())
            );
            JOptionPane.showMessageDialog(panel1, "Clave actualizada correctamente.");
            limpiar();
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(panel1, ex.getMessage(),
                    "No se pudo cambiar la clave", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void alternarVisibilidad(JPasswordField campo, JButton boton) {
        boolean estabaOculta = campo.getEchoChar() != 0;
        campo.setEchoChar(estabaOculta ? (char) 0 : '\u2022');
        boton.setText(estabaOculta ? "🙈" : "...");
    }

    private void limpiar() {
        claveActual_pField.setText("");
        claveNueva_pField.setText("");
        claveConfirmar_pField.setText("");
    }
}