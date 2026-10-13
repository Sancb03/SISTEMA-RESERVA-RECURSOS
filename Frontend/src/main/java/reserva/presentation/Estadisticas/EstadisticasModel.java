package reserva.presentation.Estadisticas;

import reserva.AbstractModel;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

public class EstadisticasModel extends AbstractModel {

    private List<Object[]> recursos;
    private List<Object[]> actividades;

    public static final String RECURSOS = "recursos";
    public static final String ACTIVIDADES = "actividades";

    public EstadisticasModel() {
        recursos = new ArrayList<>();
        actividades = new ArrayList<>();
    }

    public List<Object[]> getRecursos() {
        return recursos;
    }

    public void setRecursos(List<Object[]> recursos) {
        this.recursos = recursos;
        firePropertyChange(RECURSOS);
    }

    public List<Object[]> getActividades() {
        return actividades;
    }

    public void setActividades(List<Object[]> actividades) {
        this.actividades = actividades;
        firePropertyChange(ACTIVIDADES);
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        super.addPropertyChangeListener(listener);

        firePropertyChange(RECURSOS);
        firePropertyChange(ACTIVIDADES);
    }
}