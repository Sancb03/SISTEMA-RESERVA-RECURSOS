package reserva.presentation.Calendario;

import reserva.AbstractModel;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

public class CalendarioModel extends AbstractModel {

    private List<String> columnasRecursos;
    private List<Object[]> filasRecursos;

    private List<String> columnasActividades;
    private List<Object[]> filasActividades;

    public static final String RECURSOS = "recursos";
    public static final String ACTIVIDADES = "actividades";

    public CalendarioModel() {
        columnasRecursos = new ArrayList<>();
        filasRecursos = new ArrayList<>();

        columnasActividades = new ArrayList<>();
        filasActividades = new ArrayList<>();
    }

    public List<String> getColumnasRecursos() {
        return columnasRecursos;
    }

    public void setColumnasRecursos(List<String> columnasRecursos) {
        this.columnasRecursos = columnasRecursos;
        firePropertyChange(RECURSOS);
    }

    public List<Object[]> getFilasRecursos() {
        return filasRecursos;
    }

    public void setFilasRecursos(List<Object[]> filasRecursos) {
        this.filasRecursos = filasRecursos;
        firePropertyChange(RECURSOS);
    }

    public List<String> getColumnasActividades() {
        return columnasActividades;
    }

    public void setColumnasActividades(List<String> columnasActividades) {
        this.columnasActividades = columnasActividades;
        firePropertyChange(ACTIVIDADES);
    }

    public List<Object[]> getFilasActividades() {
        return filasActividades;
    }

    public void setFilasActividades(List<Object[]> filasActividades) {
        this.filasActividades = filasActividades;
        firePropertyChange(ACTIVIDADES);
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        super.addPropertyChangeListener(listener);

        firePropertyChange(RECURSOS);
        firePropertyChange(ACTIVIDADES);
    }
}