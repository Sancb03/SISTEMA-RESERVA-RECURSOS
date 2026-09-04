package reserva.logic;

public class Funcionario extends Usuario {
    private String telefono;

    public Funcionario() {
        super();
    }

    public String getTelefono() {

        return telefono;
    }

    public void setTelefono(String telefono) {

        this.telefono = telefono;
    }

    @Override
    public String getRol() {
        return "FUNCIONARIO";
    }
}
