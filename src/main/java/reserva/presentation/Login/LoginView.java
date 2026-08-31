package reserva.presentation.Login;

import javax.swing.*;
import java.awt.*;

public class LoginView {
    private final JPanel panel1;
    private final JTextField id_tField;
    private final JButton ingresarButton;
    private final JButton cancelarButton;
    private final JButton cambiarClaveButton;
    private final JPasswordField clave_tField;
    private final JLabel idLabel;
    private final JLabel claveLabel;

    public LoginView() {
        panel1 = new JPanel(new GridBagLayout());
        panel1.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        idLabel = new JLabel("Identificación:");
        claveLabel = new JLabel("Clave:");
        id_tField = new JTextField(15);
        clave_tField = new JPasswordField(15);
        ingresarButton = new JButton("Ingresar");
        cancelarButton = new JButton("Cancelar");
        cambiarClaveButton = new JButton("Cambiar clave");

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel1.add(idLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        panel1.add(id_tField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel1.add(claveLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        panel1.add(clave_tField, gbc);

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonsPanel.add(ingresarButton);
        buttonsPanel.add(cancelarButton);
        buttonsPanel.add(cambiarClaveButton);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        panel1.add(buttonsPanel, gbc);
    }

    public JPanel getPanel() {
        return panel1;
    }

    public JTextField getId_tField() {
        return id_tField;
    }

    public JPasswordField getClave_tField() {
        return clave_tField;
    }

    public JButton getIngresarButton() {
        return ingresarButton;
    }

    public JButton getCancelarButton() {
        return cancelarButton;
    }

    public JButton getCambiarClaveButton() {
        return cambiarClaveButton;
    }
}
