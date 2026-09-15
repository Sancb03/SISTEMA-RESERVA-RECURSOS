package reserva.presentation.Reservas;

import reserva.logic.Categoria;
import reserva.logic.Reserva;
import reserva.AbstractModel;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

public class ReservasModel extends AbstractModel {

    private List<Categoria> categorias;
    private List<Reserva> misReservas;

    public static final String CATEGORIAS = "categorias";
    public static final String RESERVAS = "reservas";

    public ReservasModel() {
        categorias = new ArrayList<>();
        misReservas = new ArrayList<>();
    }

    public List<Categoria> getCategorias() {
        return categorias;
    }

    public void setCategorias(List<Categoria> categorias) {

        this.categorias =
                categorias != null
                        ? categorias
                        : new ArrayList<>();

        firePropertyChange(CATEGORIAS);
    }

    public List<Reserva> getMisReservas() {
        return misReservas;
    }

    public void setMisReservas(List<Reserva> misReservas) {

        this.misReservas =
                misReservas != null
                        ? misReservas
                        : new ArrayList<>();

        firePropertyChange(RESERVAS);
    }

    @Override
    public void addPropertyChangeListener(
            PropertyChangeListener listener) {

        super.addPropertyChangeListener(listener);

        firePropertyChange(CATEGORIAS);
        firePropertyChange(RESERVAS);
    }
}