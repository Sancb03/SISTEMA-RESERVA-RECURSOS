package reserva.presentation.Reservas;

import reserva.logic.Categoria;
import reserva.logic.Reserva;

import java.util.ArrayList;
import java.util.List;

class ReservasModel {

    private List<Categoria> categorias = new ArrayList<>();
    private List<Reserva> misReservas = new ArrayList<>();

    public ReservasModel() {
    }

    public List<Categoria> getCategorias() {
        return categorias;
    }

    public void setCategorias(List<Categoria> categorias) {
        this.categorias = (categorias != null) ? categorias : new ArrayList<>();
    }

    public List<Reserva> getMisReservas() {
        return misReservas;
    }

    public void setMisReservas(List<Reserva> misReservas) {
        this.misReservas = (misReservas != null) ? misReservas : new ArrayList<>();
    }
}
