package reserva.logic;

public class Administrador extends Usuario {

    public Administrador() {
        super();
    }

    @Override
    public String getRol() {
        return "ADMINISTRADOR";
    }
}
