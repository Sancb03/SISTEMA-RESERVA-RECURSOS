package reserva.presentation.Login.CambiarClave;

class CambiarClaveModel {
    private String identificacion;
    private String claveActual;
    private String nuevaClave;
    private String confirmacionClave;

    CambiarClaveModel() {
    }

    String getIdentificacion() {
        return identificacion;
    }

    void setIdentificacion(String identificacion) {
        this.identificacion = identificacion;
    }

    String getClaveActual() {
        return claveActual;
    }

    void setClaveActual(String claveActual) {
        this.claveActual = claveActual;
    }

    String getNuevaClave() {
        return nuevaClave;
    }

    void setNuevaClave(String nuevaClave) {
        this.nuevaClave = nuevaClave;
    }

    String getConfirmacionClave() {
        return confirmacionClave;
    }

    void setConfirmacionClave(String confirmacionClave) {
        this.confirmacionClave = confirmacionClave;
    }
}
