package reserva.presentation;

import reserva.logic.Reserva;

/** Quien quiera enterarse de las reservas que hacen OTROS usuarios implementa esta interfaz. */
public interface SocketListener {
    void deliver_reserva(Reserva reserva);
}
