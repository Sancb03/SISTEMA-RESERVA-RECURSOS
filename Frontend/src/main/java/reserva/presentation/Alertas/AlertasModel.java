package reserva.presentation.Alertas;

import reserva.AbstractModel;
import reserva.logic.Reserva;

import java.util.ArrayList;
import java.util.List;


public class AlertasModel extends AbstractModel {

    public static final String ALERTAS = "alertas";

    private final List<Reserva> alertas = new ArrayList<>();

    public List<Reserva> getAlertas() {
        return alertas;
    }

    public void agregar(Reserva reserva) {
        alertas.add(0, reserva); // la más reciente arriba
        firePropertyChange(ALERTAS);
    }

    public void descartar(Reserva reserva) {
        if (alertas.remove(reserva)) {
            firePropertyChange(ALERTAS);
        }
    }

    public void limpiar() {
        alertas.clear();
        firePropertyChange(ALERTAS);
    }
}
