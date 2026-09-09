import reserva.presentation.Calendario.CalendarioActividadesView;
import reserva.presentation.Calendario.CalendarioRecursosView;
import reserva.presentation.Calendario.CalendarioController;

import javax.swing.*;

public class mainCalendarioRecursos {

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            // Crear las vistas
            CalendarioRecursosView recursosView =
                    new CalendarioRecursosView();

            CalendarioActividadesView actividadesView =
                    new CalendarioActividadesView();

            // Crear el controller
            new CalendarioController(
                    recursosView,
                    actividadesView
            );

            // Ventana para probar Calendario de Recursos
            JFrame frame =
                    new JFrame("Calendario de Recursos");

            frame.setContentPane(
                    recursosView.getPanel()
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