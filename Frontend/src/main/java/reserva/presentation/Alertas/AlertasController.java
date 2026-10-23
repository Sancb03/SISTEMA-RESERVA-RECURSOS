package reserva.presentation.Alertas;

import reserva.logic.Reserva;
import reserva.logic.ServiceProxy;
import reserva.logic.Usuario;
import reserva.red.AlertaListener;

import javax.swing.SwingUtilities;
import java.io.IOException;

public class AlertasController implements AlertaListener {

    private final AlertasModel model;

    public AlertasController(AlertasModel model) {
        this.model = model;
    }

    public AlertasModel getModel() {
        return model;
    }

    public void iniciar(Usuario usuario) throws IOException {
        ServiceProxy.instance().iniciarAlertas(usuario, this);
    }

    public void detener() {
        ServiceProxy.instance().detenerAlertas();
    }

    @Override
    public void nuevaReserva(Reserva reserva) {
        SwingUtilities.invokeLater(() -> model.agregar(reserva));
    }

    public void descartar(Reserva reserva) {
        model.descartar(reserva);
    }
}
