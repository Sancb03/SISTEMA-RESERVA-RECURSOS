package reserva.presentation.Reservas;

import reserva.logic.Categoria;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ReservasView extends JPanel {

    // Componentes creados por el .form (IntelliJ GUI Designer)
    private JPanel mainPanel;
    private JTextField Frase_tField;
    private JTextField Actividad_tField;
    private JTextField Fecha_tField;
    private JButton Fechabutton;
    private JComboBox HoraInicio_cBox;
    private JComboBox HoraFin_cBox;
    private JButton extraerButton;
    private JButton reservarButton;
    private JButton cancelarReservaSeleccionadaButton;
    private JButton limpiaButton;
    private JList Categorialist;
    private JButton imprimirButton;
    private JTable MiReservatable;
    private JPanel ReservasPanel;
    private JLabel FraseLabel;
    private JLabel ActividadLabel;
    private JLabel FechaLabel;
    private JLabel CategoriasLabel;
    private JLabel HoraInicioLabel;
    private JLabel HoraFinLabel;
    private JPanel MiReservasPanel;
    private JScrollPane MiReservaScrolll;
    private JPanel CategoriaPanel;
    private JScrollPane CategoriaScroll;

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private ReservasController controller;
    private ReservasModel model;
    private TablaModel tableModel;
    private DefaultListModel<Categoria> categoriasListModel;

    public ReservasView() {
        setLayout(new BorderLayout());
        add(mainPanel, BorderLayout.CENTER);

        controller = new ReservasController(this);
        model = controller.getModel();
        tableModel = controller.getTableModel();

        configurarHoras();
        configurarListaCategorias();
        MiReservatable.setModel(tableModel);

        Fechabutton.addActionListener(e -> elegirFecha());
        reservarButton.addActionListener(e -> onReservar());
        cancelarReservaSeleccionadaButton.addActionListener(e -> onCancelar());
        limpiaButton.addActionListener(e -> limpiar());
        imprimirButton.addActionListener(e -> onImprimir());
        extraerButton.addActionListener(e -> onExtraerConIA());

        controller.cargarCategorias();
        refrescarListaCategorias();

        try {
            controller.cargarMisReservas();
        } catch (Exception ex) {
            // Puede pasar si se abre esta vista sin haber iniciado sesión primero
            // (por ejemplo, mientras se prueba este módulo por separado).
            System.err.println("No se pudieron cargar las reservas: " + ex.getMessage());
        }
    }

    private void configurarHoras() {
        DefaultComboBoxModel<String> horas = new DefaultComboBoxModel<>();
        for (int h = 6; h <= 22; h++) {
            horas.addElement(String.format("%02d:00", h));
        }
        HoraInicio_cBox.setModel(horas);
        HoraFin_cBox.setModel(new DefaultComboBoxModel<>(copiarModelo(horas)));
    }

    private String[] copiarModelo(DefaultComboBoxModel<String> modelo) {
        String[] valores = new String[modelo.getSize()];
        for (int i = 0; i < modelo.getSize(); i++) {
            valores[i] = modelo.getElementAt(i);
        }
        return valores;
    }

    private void configurarListaCategorias() {
        categoriasListModel = new DefaultListModel<>();
        Categorialist.setModel(categoriasListModel);
        Categorialist.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        Categorialist.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Categoria categoria) {
                    setText(categoria.getNombre());
                }
                return this;
            }
        });
    }

    private void refrescarListaCategorias() {
        categoriasListModel.clear();
        for (Categoria categoria : model.getCategorias()) {
            categoriasListModel.addElement(categoria);
        }
    }

    private void elegirFecha() {
        LocalDate actual = parsearFecha(Fecha_tField.getText());
        Date fechaInicial = Date.from((actual != null ? actual : LocalDate.now())
                .atStartOfDay(ZoneId.systemDefault()).toInstant());

        JSpinner spinner = new JSpinner(new SpinnerDateModel(fechaInicial, null, null, java.util.Calendar.DAY_OF_MONTH));
        spinner.setEditor(new JSpinner.DateEditor(spinner, "dd/MM/yyyy"));

        int opcion = JOptionPane.showConfirmDialog(this, spinner, "Seleccionar fecha",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (opcion == JOptionPane.OK_OPTION) {
            Date seleccionada = (Date) spinner.getValue();
            LocalDate fecha = seleccionada.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            Fecha_tField.setText(fecha.format(FORMATO_FECHA));
        }
    }

    private LocalDate parsearFecha(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(texto.trim(), FORMATO_FECHA);
        } catch (Exception ex) {
            return null;
        }
    }

    private void onReservar() {
        try {
            LocalDate fecha = parsearFecha(Fecha_tField.getText());
            if (fecha == null) {
                throw new Exception("La fecha no es válida. Use el formato dd/MM/yyyy o el botón \"...\".");
            }
            LocalTime horaInicio = LocalTime.parse(String.valueOf(HoraInicio_cBox.getSelectedItem()));
            LocalTime horaFin = LocalTime.parse(String.valueOf(HoraFin_cBox.getSelectedItem()));

            List<Categoria> seleccionadas = new ArrayList<>();
            for (Object valor : Categorialist.getSelectedValuesList()) {
                seleccionadas.add((Categoria) valor);
            }

            controller.reservar(Actividad_tField.getText(), fecha, horaInicio, horaFin, seleccionadas);

            JOptionPane.showMessageDialog(this, "Reserva registrada con éxito.");
            limpiar();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "No se pudo reservar", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onCancelar() {
        int fila = MiReservatable.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione primero una reserva de la tabla.");
            return;
        }
        int id = (int) tableModel.getValueAt(fila, 0);

        int confirmar = JOptionPane.showConfirmDialog(this, "¿Cancelar la reserva seleccionada?",
                "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirmar != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            controller.cancelar(id);
            JOptionPane.showMessageDialog(this, "Reserva cancelada.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "No se pudo cancelar", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onExtraerConIA() {
        // TODO (equipo): aquí se debe invocar el servicio de IA que el profesor
        // va a entregar más adelante, usando el texto de Frase_tField para
        // llenar Actividad_tField, Fecha_tField, las horas y las categorías.
        JOptionPane.showMessageDialog(this,
                "La extracción con IA todavía no está disponible (se habilitará cuando el profesor entregue el API).",
                "Pendiente", JOptionPane.INFORMATION_MESSAGE);
    }

    private void onImprimir() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("mis_reservas.pdf"));
        int opcion = chooser.showSaveDialog(this);
        if (opcion != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File destino = chooser.getSelectedFile();

        List<String[]> filas = new ArrayList<>();
        for (Object[] fila : tableModel.getFilas()) {
            String[] textos = new String[fila.length];
            for (int i = 0; i < fila.length; i++) {
                textos[i] = String.valueOf(fila[i]);
            }
            filas.add(textos);
        }

        try {
            reserva.presentation.PdfReportGenerator.generar(destino, "Mis Reservas", TablaModel.COLUMNAS, filas);
            JOptionPane.showMessageDialog(this, "Reporte guardado en:\n" + destino.getAbsolutePath());
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "No se pudo generar el PDF: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void limpiar() {
        Frase_tField.setText("");
        Actividad_tField.setText("");
        Fecha_tField.setText("");
        HoraInicio_cBox.setSelectedIndex(0);
        HoraFin_cBox.setSelectedIndex(0);
        Categorialist.clearSelection();
    }
}

