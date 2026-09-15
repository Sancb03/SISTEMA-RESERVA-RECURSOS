package reserva.presentation.Reservas;

import reserva.GeneradorPDF;
import reserva.logic.Categoria;
import reserva.logic.Recurso;
import reserva.presentation.Iconos;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
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
    private JScrollPane RecursosDisponiblesScroll;
    private JList RecursosDisponiblesList;

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private ReservasController controller;
    private ReservasModel model;
    private TablaModel tableModel;
    private DefaultListModel<Categoria> categoriasListModel;
    private DefaultListModel<Recurso> recursosDisponiblesListModel;

    public ReservasView() {
        setLayout(new BorderLayout());
        add(mainPanel, BorderLayout.CENTER);

        controller = new ReservasController(this);
        model = controller.getModel();
        tableModel = controller.getTableModel();

        configurarHoras();
        configurarListaCategorias();
        configurarListaRecursosDisponibles();
        MiReservatable.setModel(tableModel);

        Fechabutton.setIcon(Iconos.get("date"));
        extraerButton.setIcon(Iconos.get("ai"));
        reservarButton.setIcon(Iconos.get("checkDouble"));
        cancelarReservaSeleccionadaButton.setIcon(Iconos.get("cancel"));
        limpiaButton.setIcon(Iconos.get("clear"));
        imprimirButton.setIcon(Iconos.get("pdf"));

        Fechabutton.addActionListener(e -> { elegirFecha(); refrescarRecursosDisponibles(); });
        reservarButton.addActionListener(e -> onReservar());
        cancelarReservaSeleccionadaButton.addActionListener(e -> onCancelar());
        limpiaButton.addActionListener(e -> limpiar());
        imprimirButton.addActionListener(e -> onImprimir());
        extraerButton.addActionListener(e -> onExtraerConIA());

        Categorialist.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                refrescarRecursosDisponibles();
            }
        });
        HoraInicio_cBox.addItemListener(e -> refrescarRecursosDisponibles());
        HoraFin_cBox.addItemListener(e -> refrescarRecursosDisponibles());
        Fecha_tField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { refrescarRecursosDisponibles(); }
            public void removeUpdate(DocumentEvent e) { refrescarRecursosDisponibles(); }
            public void changedUpdate(DocumentEvent e) { refrescarRecursosDisponibles(); }
        });

        try {
            controller.cargarCategorias();
            controller.cargarMisReservas();
        } catch (Exception ex) {
            // Puede pasar si se abre esta vista sin haber iniciado sesión primero
            // (por ejemplo, mientras se prueba este módulo por separado).
            System.err.println("No se pudo cargar Reservas: " + ex.getMessage());
        }
        refrescarListaCategorias();
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
                    setText(categoria.getDescripcion());
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

    private void configurarListaRecursosDisponibles() {
        recursosDisponiblesListModel = new DefaultListModel<>();
        RecursosDisponiblesList.setModel(recursosDisponiblesListModel);
        RecursosDisponiblesList.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        RecursosDisponiblesList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Recurso recurso) {
                    String categoria = recurso.getCategoria() != null ? recurso.getCategoria().getDescripcion() : "";
                    setText(recurso.getId() + " - " + recurso.getDescripcion() + " (" + categoria + ")");
                }
                return this;
            }
        });
    }

    /**
     * Recalcula, con lo que hay ahora mismo en el formulario (categorías, fecha,
     * horas), qué recursos concretos están libres. Es opcional: si el funcionario
     * no escoge ninguno de aquí, se asigna igual el primero disponible de cada
     * categoría al reservar.
     */
    private void refrescarRecursosDisponibles() {
        recursosDisponiblesListModel.clear();

        LocalDate fecha = parsearFecha(Fecha_tField.getText());
        LocalTime horaInicio = parsearHoraCombo(HoraInicio_cBox);
        LocalTime horaFin = parsearHoraCombo(HoraFin_cBox);

        List<Categoria> seleccionadas = new ArrayList<>();
        for (Object valor : Categorialist.getSelectedValuesList()) {
            seleccionadas.add((Categoria) valor);
        }

        if (fecha == null || horaInicio == null || horaFin == null || !horaInicio.isBefore(horaFin)
                || seleccionadas.isEmpty()) {
            return;
        }

        try {
            List<Recurso> disponibles = controller.listarRecursosDisponibles(seleccionadas, fecha, horaInicio, horaFin);
            for (Recurso recurso : disponibles) {
                recursosDisponiblesListModel.addElement(recurso);
            }
        } catch (Exception ex) {
            // Sesión no lista todavía, etc. -- se deja la lista vacía sin molestar con un aviso.
        }
    }

    private LocalTime parsearHoraCombo(JComboBox combo) {
        Object valor = combo.getSelectedItem();
        if (valor == null) {
            return null;
        }
        try {
            return LocalTime.parse(String.valueOf(valor));
        } catch (Exception ex) {
            return null;
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

            List<Recurso> recursosElegidos = new ArrayList<>();
            for (Object valor : RecursosDisponiblesList.getSelectedValuesList()) {
                recursosElegidos.add((Recurso) valor);
            }

            controller.reservar(Actividad_tField.getText(), fecha, horaInicio, horaFin, seleccionadas, recursosElegidos);

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
        String id = (String) tableModel.getValueAt(fila, 0);

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
        String frase = Frase_tField.getText();
        try {
            ReservaExtraccion extraccion = controller.extraerConIA(frase);
            aplicarExtraccion(extraccion);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo extraer la información con IA: " + ex.getMessage()
                            + "\nPuede llenar el formulario a mano.",
                    "No se pudo extraer", JOptionPane.WARNING_MESSAGE);
        }
    }

    /** Llena el formulario con lo que devolvió la IA. El usuario puede revisar y corregir antes de reservar. */
    private void aplicarExtraccion(ReservaExtraccion extraccion) {
        if (extraccion == null) {
            JOptionPane.showMessageDialog(this, "La IA no devolvió ninguna información para esa frase.");
            return;
        }

        if (extraccion.getActividad() != null) {
            Actividad_tField.setText(extraccion.getActividad());
        }

        if (extraccion.getFecha() != null) {
            try {
                LocalDate fecha = LocalDate.parse(extraccion.getFecha());
                Fecha_tField.setText(fecha.format(FORMATO_FECHA));
            } catch (Exception ex) {
                // Si la IA devolvió una fecha en un formato raro, se deja que el usuario la escriba a mano.
            }
        }

        seleccionarHora(HoraInicio_cBox, extraccion.getHoraInicio());
        seleccionarHora(HoraFin_cBox, extraccion.getHoraFinal());

        if (extraccion.getCategoriasRecurso() != null) {
            seleccionarCategorias(extraccion.getCategoriasRecurso());
        }

        refrescarRecursosDisponibles();
    }

    private void seleccionarHora(JComboBox<String> combo, String hora) {
        if (hora == null) {
            return;
        }
        for (int i = 0; i < combo.getItemCount(); i++) {
            if (combo.getItemAt(i).equals(hora)) {
                combo.setSelectedIndex(i);
                return;
            }
        }
    }

    private void seleccionarCategorias(List<String> descripciones) {
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < categoriasListModel.getSize(); i++) {
            Categoria categoria = categoriasListModel.getElementAt(i);
            if (descripciones.stream().anyMatch(d -> d != null && d.equalsIgnoreCase(categoria.getDescripcion()))) {
                indices.add(i);
            }
        }
        int[] indicesArray = indices.stream().mapToInt(Integer::intValue).toArray();
        Categorialist.setSelectedIndices(indicesArray);
    }

    private void onImprimir() {
        GeneradorPDF.generarDesdeTabla(MiReservatable, "Mis Reservas", "MisReservas.pdf");
    }

    private void limpiar() {
        Frase_tField.setText("");
        Actividad_tField.setText("");
        Fecha_tField.setText("");
        HoraInicio_cBox.setSelectedIndex(0);
        HoraFin_cBox.setSelectedIndex(0);
        Categorialist.clearSelection();
        recursosDisponiblesListModel.clear();
    }
}
