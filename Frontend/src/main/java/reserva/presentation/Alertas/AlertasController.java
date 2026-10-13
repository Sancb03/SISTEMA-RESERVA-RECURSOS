package reserva.presentation.Alertas;

import reserva.logic.Reserva;
import reserva.logic.ServiceProxy;
import reserva.logic.Usuario;
import reserva.red.AlertaListener;

import javax.swing.SwingUtilities;
import java.io.IOException;

/**
 * Conecta el canal de alertas del Proxy con el Model. El Proxy llama a
 * nuevaReserva() desde el hilo del socket; aquí se pasa al hilo de Swing con
 * invokeLater antes de tocar el Model (que a su vez refresca la tabla).
 */
public class AlertasController implements AlertaListener {

    private final AlertasModel model;

    public AlertasController(AlertasModel model) {
        this.model = model;
    }

    public AlertasModel getModel() {
        return model;
    }

    /** Empieza a recibir las reservas que hagan los demás usuarios. */
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
