package reserva.red;

import reserva.logic.Reserva;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class RegistroAlertas {

    private record Suscriptor(Socket socket, ObjectOutputStream salida, int usuarioId) {
    }

    private final List<Suscriptor> suscriptores = new CopyOnWriteArrayList<>();

    public void agregar(Socket socket, ObjectOutputStream salida, int usuarioId) {
        suscriptores.add(new Suscriptor(socket, salida, usuarioId));
    }

    public void quitar(ObjectOutputStream salida) {
        suscriptores.removeIf(s -> s.salida() == salida);
    }

    public int cantidad() {
        return suscriptores.size();
    }

    public void difundirNuevaReserva(Reserva reserva) {
        Notificacion notificacion = new Notificacion(Notificacion.NUEVA_RESERVA, reserva);
        int autor = reserva.getFuncionario() != null ? reserva.getFuncionario().getId() : -1;

        for (Suscriptor s : suscriptores) {
            if (s.usuarioId() == autor) {
                continue;
            }
            try {
                synchronized (s.salida()) {
                    s.salida().writeObject(notificacion);
                    s.salida().flush();
                    s.salida().reset(); // que el stream no se "acuerde" de los objetos ya enviados
                }
            } catch (IOException ex) {
                // ese Frontend ya no está: se saca de la lista y se cierra su conexión
                suscriptores.remove(s);
                try {
                    s.socket().close();
                } catch (IOException ignored) {
                }
            }
        }
    }
}
