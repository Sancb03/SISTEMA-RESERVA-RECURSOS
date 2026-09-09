import reserva.presentation.Estadisticas.EstadisticasController;
import reserva.presentation.Estadisticas.EstadisticasView;

import javax.swing.*;

public class mainEstadisticas {

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            EstadisticasView view =
                    new EstadisticasView();

            new EstadisticasController(view);

            JFrame frame =
                    new JFrame("Estadisticas");

            frame.setContentPane(
                    view.getPanelPrincipal()
            );

            frame.setDefaultCloseOperation(
                    JFrame.EXIT_ON_CLOSE
            );

            frame.pack();

            frame.setLocationRelativeTo(null);

            frame.setVisible(true);
        });
    }
}