package reserva.presentation.Login;

import reserva.logic.Usuario;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

class LoginModel {

    public static final String USUARIO_AUTENTICADO = "usuarioAutenticado";

    private final PropertyChangeSupport support = new PropertyChangeSupport(this);
    private Usuario usuarioAutenticado;

    LoginModel() {
    }

    Usuario getUsuarioAutenticado() {
        return usuarioAutenticado;
    }

    void setUsuarioAutenticado(Usuario usuarioAutenticado) {
        this.usuarioAutenticado = usuarioAutenticado;
        support.firePropertyChange(USUARIO_AUTENTICADO, null, usuarioAutenticado);
    }

    void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }
}