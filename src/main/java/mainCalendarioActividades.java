import reserva.presentation.Calendario.CalendarioActividadesView;
import reserva.presentation.Calendario.CalendarioRecursosView;
import reserva.presentation.Calendario.CalendarioController;

import javax.swing.*;

public class mainCalendarioActividades {

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            CalendarioRecursosView recursosView =
                    new CalendarioRecursosView();

            CalendarioActividadesView actividadesView =
                    new CalendarioActividadesView();

            new CalendarioController(
                    recursosView,
                    actividadesView
            );

            JFrame frame =
                    new JFrame("Calendario de Actividades");

            frame.setContentPane(
                    actividadesView.getPanelPrincipal()
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