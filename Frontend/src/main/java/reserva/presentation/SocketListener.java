package reserva.presentation;

import reserva.logic.Reserva;

/** Quien quiera enterarse de las reservas que hacen OTROS usuarios implementa esta interfaz. */
public interface SocketListener {
    void deliver_reserva(Reserva reserva);
    /** El Backend dejó de responder (se cayó o se cortó la red). Se llama en el hilo de Swing. */
    default void conexionPerdida() {
    }
}
