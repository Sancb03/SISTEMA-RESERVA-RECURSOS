package reserva.presentation.Alertas;

import reserva.logic.Reserva;
import reserva.logic.Service;
import reserva.presentation.SocketListener;
import reserva.presentation.SocketMonitor;

public class AlertasController implements SocketListener {

    private final AlertasModel model;

    private SocketMonitor monitor;

    public AlertasController(AlertasModel model) {
        this.model = model;
    }

    public AlertasModel getModel() {
        return model;
    }

    /** Abre la conexión asincrónica de esta sesión y empieza a escuchar las reservas de los demás. */
    public void iniciar() throws Exception {
        detener();
        monitor = new SocketMonitor(this, Service.instance().getSid());
        monitor.start();
    }

    public void detener() {
        if (monitor != null) {
            monitor.stop();
            monitor = null;
        }
    }

    /** El SocketMonitor ya llama a este método dentro del hilo de Swing (invokeLater). */
    @Override
    public void deliver_reserva(Reserva reserva) {
        model.agregar(reserva);
    }

    public void descartar(Reserva reserva) {
        model.descartar(reserva);
    }
}
