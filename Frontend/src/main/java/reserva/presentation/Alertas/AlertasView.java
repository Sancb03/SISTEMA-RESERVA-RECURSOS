package reserva.presentation.Alertas;

import reserva.logic.Recurso;
import reserva.logic.Reserva;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

/**
 * Panel de Alertas: va a la derecha de la pantalla principal, independiente de
 * la pestaña que esté abierta. "Ver" abre el detalle de la reserva y "Desc."
 * pide confirmación y quita la alerta de esta pantalla (solo local).
 */
public class AlertasView implements PropertyChangeListener {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

    private final AlertasController controller;
    private final AlertasModel model;
    private final JPanel panel = new JPanel(new BorderLayout());
    private final JTable tabla = new JTable();
    private AlertasTableModel tableModel;

    public AlertasView(AlertasController controller) {
        this.controller = controller;
        this.model = controller.getModel();
        model.addPropertyChangeListener(this);

        panel.setBorder(BorderFactory.createTitledBorder("Alertas"));
        panel.setPreferredSize(new Dimension(300, 0));
        panel.add(new JScrollPane(tabla), BorderLayout.CENTER);

        tabla.setRowHeight(28);
        tabla.setFillsViewportHeight(true);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.getTableHeader().setReorderingAllowed(false);
        cargarTabla();

        tabla.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int fila = tabla.rowAtPoint(e.getPoint());
                int columna = tabla.columnAtPoint(e.getPoint());
                if (fila < 0 || columna < 0) {
                    return;
                }
                Reserva reserva = tableModel.getRowAt(tabla.convertRowIndexToModel(fila));
                if (columna == AlertasTableModel.VER) {
                    mostrarDetalle(reserva);
                } else if (columna == AlertasTableModel.DESCARTAR) {
                    confirmarDescarte(reserva);
                }
            }
        });
    }

    public JPanel getPanel() {
        return panel;
    }

    private void cargarTabla() {
        tableModel = new AlertasTableModel(
                new int[]{AlertasTableModel.ACTIVIDAD, AlertasTableModel.VER, AlertasTableModel.DESCARTAR},
                model.getAlertas());
        tabla.setModel(tableModel);
        tabla.getColumnModel().getColumn(AlertasTableModel.VER).setMaxWidth(45);
        tabla.getColumnModel().getColumn(AlertasTableModel.DESCARTAR).setMaxWidth(45);

        DefaultTableCellRenderer centrado = new DefaultTableCellRenderer();
        centrado.setHorizontalAlignment(SwingConstants.CENTER);
        tabla.getColumnModel().getColumn(AlertasTableModel.VER).setCellRenderer(centrado);
        tabla.getColumnModel().getColumn(AlertasTableModel.DESCARTAR).setCellRenderer(centrado);
    }

    private void confirmarDescarte(Reserva reserva) {
        int opcion = JOptionPane.showConfirmDialog(panel,
                "¿Desea descartar la alerta \"" + reserva.getActividad() + "\"?",
                "Descartar alerta", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (opcion == JOptionPane.YES_OPTION) {
            controller.descartar(reserva);
        }
    }

    private void mostrarDetalle(Reserva r) {
        JOptionPane.showMessageDialog(panel, construirDetalle(r), "Detalle de la reserva",
                JOptionPane.INFORMATION_MESSAGE);
    }

    /** El contenido del popup: Encargado, Fecha, Desde, Hasta y Recursos. */
    static JPanel construirDetalle(Reserva r) {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(3, 6, 3, 6);
        c.anchor = GridBagConstraints.NORTHWEST;

        String recursos = r.getRecursos() == null ? "" : r.getRecursos().stream()
                .map(Recurso::getDescripcion).collect(Collectors.joining(", "));
        String[][] filas = {
                {"Actividad:", r.getActividad()},
                {"Encargado:", r.getFuncionario() != null ? r.getFuncionario().getNombre() : ""},
                {"Fecha:", r.getFecha() != null ? r.getFecha().format(FECHA) : ""},
                {"Desde:", r.getHoraInicio() != null ? r.getHoraInicio().format(HORA) : ""},
                {"Hasta:", r.getHoraFin() != null ? r.getHoraFin().format(HORA) : ""},
                {"Recursos:", recursos}
        };
        for (int i = 0; i < filas.length; i++) {
            c.gridy = i;
            c.gridx = 0;
            JLabel etiqueta = new JLabel(filas[i][0]);
            etiqueta.setFont(etiqueta.getFont().deriveFont(Font.BOLD));
            p.add(etiqueta, c);
            c.gridx = 1;
            p.add(new JLabel(filas[i][1]), c);
        }
        return p;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (AlertasModel.ALERTAS.equals(evt.getPropertyName())) {
            tableModel.refrescar();
        }
    }
}
