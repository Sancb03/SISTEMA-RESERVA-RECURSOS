package reserva.presentation.Alertas;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reserva.logic.Funcionario;
import reserva.logic.Recurso;
import reserva.logic.Reserva;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Alertas: Model, Controller y TableModel")
class AlertasTest {

    private AlertasModel model;
    private AlertasController controller;

    private static Reserva reserva(String actividad, String funcionario) {
        Funcionario f = new Funcionario();
        f.setNombre(funcionario);
        Recurso r = new Recurso();
        r.setDescripcion("Sala 1");
        Reserva res = new Reserva();
        res.setActividad(actividad);
        res.setFuncionario(f);
        res.setFecha(LocalDate.of(2027, 5, 20));
        res.setHoraInicio(LocalTime.of(8, 0));
        res.setHoraFin(LocalTime.of(10, 0));
        res.setRecursos(new ArrayList<>(List.of(r)));
        return res;
    }

    @BeforeEach
    void setUp() {
        model = new AlertasModel();
        controller = new AlertasController(model);
    }

    @Test
    @DisplayName("Una alerta nueva queda de primera y avisa a la vista")
    void agregar() {
        List<String> avisos = new ArrayList<>();
        model.addPropertyChangeListener(e -> avisos.add(e.getPropertyName()));

        model.agregar(reserva("Primera", "Juan"));
        model.agregar(reserva("Segunda", "Maria"));

        assertEquals(2, model.getAlertas().size());
        assertEquals("Segunda", model.getAlertas().get(0).getActividad());
        assertEquals(List.of(AlertasModel.ALERTAS, AlertasModel.ALERTAS), avisos);
    }

    @Test
    @DisplayName("Descartar quita solo esa alerta")
    void descartar() {
        Reserva a = reserva("A", "Juan");
        Reserva b = reserva("B", "Juan");
        model.agregar(a);
        model.agregar(b);

        controller.descartar(a);

        assertEquals(List.of(b), model.getAlertas());
    }

    @Test
    @DisplayName("Descartar una alerta que ya no está no hace nada ni avisa")
    void descartarInexistente() {
        List<String> avisos = new ArrayList<>();
        model.addPropertyChangeListener(e -> avisos.add(e.getPropertyName()));

        controller.descartar(reserva("X", "Juan"));

        assertTrue(avisos.isEmpty());
    }

    @Test
    @DisplayName("El listener (hilo del socket) pasa por el hilo de Swing antes de tocar el Model")
    void nuevaReservaEntraPorElHiloDeSwing() throws InvocationTargetException, InterruptedException {
        List<Boolean> enHiloSwing = new ArrayList<>();
        model.addPropertyChangeListener(e -> enHiloSwing.add(SwingUtilities.isEventDispatchThread()));

        controller.nuevaReserva(reserva("Desde el socket", "Juan"));
        SwingUtilities.invokeAndWait(() -> { }); // espera a que se procese lo que quedó en la cola de Swing

        assertEquals(1, model.getAlertas().size());
        assertEquals(List.of(true), enHiloSwing);
    }

    @Test
    @DisplayName("La tabla muestra 'Funcionario - actividad' y tiene las columnas Actividad, Ver y Desc.")
    void tabla() {
        model.agregar(reserva("Reunion clientes", "Juan Perez"));
        AlertasTableModel tabla = new AlertasTableModel(
                new int[]{AlertasTableModel.ACTIVIDAD, AlertasTableModel.VER, AlertasTableModel.DESCARTAR},
                model.getAlertas());

        assertEquals(3, tabla.getColumnCount());
        assertEquals("Actividad", tabla.getColumnName(0));
        assertEquals("Ver", tabla.getColumnName(1));
        assertEquals("Desc.", tabla.getColumnName(2));
        assertEquals("Juan Perez - Reunion clientes", tabla.getValueAt(0, 0));
        assertNotNull(tabla.getValueAt(0, 1));
        assertNotNull(tabla.getValueAt(0, 2));
    }
}
