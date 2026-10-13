package reserva.red;

import reserva.logic.Reserva;

/**
 * Quien quiera enterarse de las reservas que hacen OTROS usuarios (el panel de
 * Alertas) implementa esta interfaz y se registra con
 * ServiceProxy.instance().iniciarAlertas(...).
 *
 * OJO: nuevaReserva() se llama desde el hilo que escucha el socket, NO desde
 * el hilo de Swing. Quien la implemente debe pasar a Swing con
 * SwingUtilities.invokeLater antes de tocar cualquier componente
 * (igual que hacen Refresher y FsMonitor en el ejemplo del profe).
 */
public interface AlertaListener {

    void nuevaReserva(Reserva reserva);
}
