package reserva.presentation.Funcionarios;

import reserva.logic.Funcionario;
import reserva.AbstractModel;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

public class FuncionariosModel extends AbstractModel {

    private Funcionario current;
    private List<Funcionario> listado;

    public static final String CURRENT = "current";
    public static final String LIST = "list";

    public FuncionariosModel() {
        current = new Funcionario();
        listado = new ArrayList<>();
    }

    public Funcionario getCurrent() {
        return current;
    }

    public void setCurrent(Funcionario current) {
        this.current = current;
        firePropertyChange(CURRENT);
    }

    public List<Funcionario> getListado() {
        return listado;
    }

    public void setListado(List<Funcionario> listado) {
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