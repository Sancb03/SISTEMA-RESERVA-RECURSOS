package reserva;

import reserva.logic.Service;
import reserva.presentation.Login.LoginView;

import javax.swing.*;

public class Application {
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

            if (!conectarAlBackend()) {
                System.exit(0);
            }

            LoginView loginView = new LoginView();
            loginView.setVisible(true);
        });
    }

    /**
     * El Frontend no sirve sin el Backend: se intenta conectar antes de mostrar el Login
     * y, si no se puede, se explica dónde se buscó y se deja reintentar.
     */
    private static boolean conectarAlBackend() {
        Service proxy = Service.instance();
        while (true) {
            try {
                proxy.conectar();
                return true;
            } catch (java.io.IOException ex) {
                Object[] opciones = {"Reintentar", "Salir"};
                int eleccion = JOptionPane.showOptionDialog(null,
                        "No se pudo conectar con el servidor en " + proxy.getHost() + ":" + proxy.getPort()
                                + ".\nVerifique que el Backend (reserva.Application) esté corriendo.",
                        "Sin conexión", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE,
                        null, opciones, opciones[0]);
                if (eleccion != 0) {
                    return false;
                }
            }
        }
    }
}
