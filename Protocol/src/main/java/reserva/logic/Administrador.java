package reserva.logic;

public class Administrador extends Usuario {
    private static final long serialVersionUID = 1L;


    public Administrador() {
        super();
    }

    @Override
    public String getRol() {
        return "ADMINISTRADOR";
    }
}
