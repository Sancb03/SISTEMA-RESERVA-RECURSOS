package reserva.presentation.Recursos;

import reserva.logic.Recurso;
import reserva.AbstractModel;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

public class RecursosModel extends AbstractModel {

    private Recurso current;
    private List<Recurso> listado;

    public static final String CURRENT = "current";
    public static final String LIST = "list";

    public RecursosModel() {
        current = new Recurso();
        listado = new ArrayList<>();
    }

    public Recurso getCurrent() {
        return current;
    }

    public void setCurrent(Recurso current) {
        this.current = current;
        firePropertyChange(CURRENT);
    }

    public List<Recurso> getListado() {
        return listado;
    }

    public void setListado(List<Recurso> listado) {
        this.listado = listado;
        firePropertyChange(LIST);
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        super.addPropertyChangeListener(listener);

        firePropertyChange(CURRENT);
        firePropertyChange(LIST);
    }
}