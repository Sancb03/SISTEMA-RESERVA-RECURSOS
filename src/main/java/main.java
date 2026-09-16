import reserva.presentation.Login.LoginView;

import javax.swing.*;

public class main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    try {
                        UIManager.setLookAndFeel(info.getClassName());
                    } catch (ReflectiveOperationException | UnsupportedLookAndFeelException e) {
                        throw new IllegalStateException("No se pudo configurar el look and feel Nimbus", e);
                    }
                    break;
                }
            }

            LoginView loginView = new LoginView();
            loginView.setVisible(true);
        });
    }
}
