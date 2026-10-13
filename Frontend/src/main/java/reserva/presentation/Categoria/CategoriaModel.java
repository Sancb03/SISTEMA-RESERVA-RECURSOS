package reserva.presentation.Categoria;

import reserva.logic.Categoria;
import reserva.AbstractModel;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

public class CategoriaModel extends AbstractModel {

    private Categoria current;
    private List<Categoria> listado;

    public static final String CURRENT = "current";
    public static final String LIST = "list";

    public CategoriaModel() {
        current = new Categoria();
        listado = new ArrayList<>();
    }

    public Categoria getCurrent() {
        return current;
    }

    public void setCurrent(Categoria current) {
        this.current = current;
        firePropertyChange(CURRENT);
    }

    public List<Categoria> getListado() {
        return listado;
    }

    public void setListado(List<Categoria> listado) {
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