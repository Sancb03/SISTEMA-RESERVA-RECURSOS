import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import reserva.presentation.Login.LoginView;

public class main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LoginView loginView = new LoginView();
            JFrame frame = new JFrame("Sistema de Reserva de Recursos");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(loginView.getPanel());
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
