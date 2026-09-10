package reserva.presentation.Calendario;

import reserva.data.Data;
import reserva.logic.Categoria;
import reserva.logic.Recurso;
import reserva.logic.Reserva;
import reserva.logic.Usuario;
import reserva.presentation.Iconos;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import java.awt.Component;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;

import java.awt.Desktop;
import java.io.File;


public class CalendarioController {
    private final CalendarioRecursosView recursosView;
    private final CalendarioActividadesView actividadesView;
    private final CalendarioModel model;
    private final TablaModel tableModel;
    private TablaModel actividadesTableModel;

    private final Usuario usuarioActual;

    public CalendarioController(CalendarioRecursosView recursosView, CalendarioActividadesView actividadesView,
                                CalendarioModel model, TablaModel tableModel, Usuario usuarioActual) {
        this.recursosView = recursosView;
        this.actividadesView = actividadesView;
        this.model = model;
        this.tableModel = tableModel;
        this.usuarioActual = usuarioActual;
    }

    public CalendarioController(CalendarioRecursosView recursosView, CalendarioActividadesView actividadesView,
                                Usuario usuarioActual) {

        this.actividadesTableModel = new TablaModel();
        this.recursosView = recursosView;
        this.actividadesView = actividadesView;
        this.model = new CalendarioModel();
        this.tableModel = new TablaModel();
        this.usuarioActual = usuarioActual;

        recursosView.getCalendarioRectable().setModel(tableModel);
        actividadesView.getActividadesSemtable().setModel(actividadesTableModel);

        cargarCategoriasEnCombo();
        configurarEventos();

        cargarHoras();
        cargarCalendarioActividades();
    }

    private void configurarEventos() {
        recursosView.getCargarButton().setIcon(Iconos.get("date"));
        recursosView.getImprimirButton().setIcon(Iconos.get("pdf"));
        actividadesView.getCargarButton().setIcon(Iconos.get("date"));
        actividadesView.getImprimirButton().setIcon(Iconos.get("pdf"));

        recursosView.getCargarButton().addActionListener(e -> {cargarCalendarioRecursos();});

        recursosView.getImprimirButton().addActionListener(e -> {generarPDFRecursos();});

        actividadesView.getCargarButton().addActionListener(e -> cargarCalendarioActividades());

        actividadesView.getImprimirButton().addActionListener(e-> generarPDFActividades());
    }

    @SuppressWarnings("unchecked")
    private void cargarCategoriasEnCombo() {
        try {
            List<Categoria> categorias = Data.instance().listarCategorias(usuarioActual);
            for (Categoria c : categorias) {
                recursosView.getDescripcion_cBox().addItem(c);
            }
            recursosView.getDescripcion_cBox().setRenderer(new DefaultListCellRenderer() {
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
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(null, "No se pudieron cargar las categorías: " + ex.getMessage());
        }
    }

    //Metodos que se utilizan en la calendarizacion de recursos
    private void cargarHoras() {
        List<String> columnas = new ArrayList<>();

        columnas.add("Hora");

        tableModel.setColumnas(columnas);

        List<Object[]> filas = new ArrayList<>();

        for(int hora = 7; hora <= 20; hora++){
            Object[] fila = new Object[1];

            fila[0] = String.format("%02d:00", hora);

            filas.add(fila);
        }
        tableModel.setFilas(filas);
    }


    private void cargarCalendarioRecursos() {
        LocalDate fecha = recursosView.getDatePicker().getDate();

        Object seleccionado = recursosView.getDescripcion_cBox().getSelectedItem();

        if(fecha == null){
            JOptionPane.showMessageDialog(null, "Seleccione una fecha");
            return;
        }

        if(!(seleccionado instanceof Categoria categoria)){
            JOptionPane.showMessageDialog(null, "Seleccione una categoria");
            return;
        }

        List<Recurso> recursos = Data.instance().buscarRecursosPorCategoria(categoria, usuarioActual);
        List<Reserva> todasLasReservas = Data.instance().listarReservas();

        List<String> columnas = new ArrayList<>();
        columnas.add("Hora");
        for (Recurso r : recursos) {
            columnas.add(r.getId());
        }
        tableModel.setColumnas(columnas);

        List<Object[]> filas = new ArrayList<>();
        for (int hora = 7; hora <= 20; hora++) {
            Object[] fila = new Object[1 + recursos.size()];
            fila[0] = String.format("%02d:00", hora);
            LocalTime horaInicioCelda = LocalTime.of(hora, 0);
            LocalTime horaFinCelda = horaInicioCelda.plusHours(1);

            for (int i = 0; i < recursos.size(); i++) {
                fila[i + 1] = textoOcupacion(todasLasReservas, recursos.get(i), fecha, horaInicioCelda, horaFinCelda);
            }
            filas.add(fila);
        }
        tableModel.setFilas(filas);
    }

    /** Si hay una reserva activa de ese recurso que cubre ese rango de hora, arma el texto a mostrar en la celda. */
    private String textoOcupacion(List<Reserva> reservas, Recurso recurso, LocalDate fecha,
                                  LocalTime horaInicioCelda, LocalTime horaFinCelda) {
        for (Reserva r : reservas) {
            if (!Reserva.ACTIVA.equals(r.getEstado())) continue;
            if (!fecha.equals(r.getFecha())) continue;
            boolean tieneEseRecurso = r.getRecursos().stream().anyMatch(x -> x.getId().equals(recurso.getId()));
            if (!tieneEseRecurso) continue;

            boolean seCruza = horaInicioCelda.isBefore(r.getHoraFin()) && r.getHoraInicio().isBefore(horaFinCelda);
            if (seCruza) {
                String funcionario = (r.getFuncionario() != null) ? r.getFuncionario().getNombre() : "";
                return r.getActividad() + " - " + funcionario;
            }
        }
        return "";
    }

    private void generarPDFRecursos() {
        String destino = "CalendarioRecursos.pdf";

        try{
            PdfWriter writer = new PdfWriter(destino);
            PdfDocument pdf = new PdfDocument(writer);
            Document documento = new Document(pdf);

            documento.add(new Paragraph("\t\t\tCalendario de Recursos"));

            int cantColumnas = recursosView.getCalendarioRectable().getColumnCount();

            Table tablaPDF = new Table(cantColumnas);

            //Encabezados
            for(int columnas =0; columnas < cantColumnas; columnas++){
                String nombreColumna = recursosView.getCalendarioRectable().getColumnName(columnas);

                tablaPDF.addHeaderCell(new Cell().add(new Paragraph(nombreColumna)));
            }

            //filas
            int cantFilas = recursosView.getCalendarioRectable().getRowCount();

            for(int fila =0; fila < cantFilas; fila++){
                for(int columna =0; columna < cantColumnas; columna++){
                    Object valor = recursosView.getCalendarioRectable().getValueAt(fila, columna);

                    String texto;

                    if(valor == null){
                        texto = "";
                    }else{
                        texto = valor.toString();
                    }

                    tablaPDF.addCell(new Cell().add(new Paragraph(texto)));
                }
            }
            documento.add(tablaPDF);
            documento.close();

            abrirPDF(destino);

        }catch(Exception e){
            JOptionPane.showMessageDialog(null, "No se genero el PDF: " + e.getMessage());
        }
    }

    public CalendarioRecursosView getRecursosView() {
        return recursosView;
    }


    //Metodos que se utilizan en la calendarizacion de Actividades
    public void cargarCalendarioActividades(){
        LocalDate fecha = actividadesView.getDatePickerA().getDate();
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM");

        if(fecha == null){
            JOptionPane.showMessageDialog(null, "Seleccione una fecha");
            return;
        }

        LocalDate semana = fecha.with(DayOfWeek.MONDAY);

        List<String> columnas = new ArrayList<>();

        columnas.add("Hora");
        columnas.add("Lunes " + semana.format(formato));
        columnas.add("Martes " + semana.plusDays(1).format(formato));
        columnas.add("Miercoles " + semana.plusDays(2).format(formato));
        columnas.add("Jueves " + semana.plusDays(3).format(formato));
        columnas.add("Viernes " + semana.plusDays(4).format(formato));
        columnas.add("Sabado " + semana.plusDays(5).format(formato));
        columnas.add("Domingo " + semana.plusDays(6).format(formato));

        actividadesTableModel.setColumnas(columnas);

        List<Reserva> todasLasReservas = Data.instance().listarReservas();

        List<Object[]> filas = new ArrayList<>();

        for(int hora = 7; hora <= 20; hora++){
            Object[] fila = new Object[8];

            fila[0] = String.format("%02d:00", hora);

            LocalTime horaInicioCelda = LocalTime.of(hora, 0);
            LocalTime horaFinCelda = horaInicioCelda.plusHours(1);

            for (int dia = 0; dia < 7; dia++) {
                LocalDate fechaColumna = semana.plusDays(dia);
                fila[dia + 1] = textoActividad(todasLasReservas, fechaColumna, horaInicioCelda, horaFinCelda);
            }

            filas.add(fila);
        }
        actividadesTableModel.setFilas(filas);
    }

    /** Si hay una reserva activa (de cualquier recurso) en esa fecha y hora, arma el texto a mostrar en la celda. */
    private String textoActividad(List<Reserva> reservas, LocalDate fecha, LocalTime horaInicioCelda, LocalTime horaFinCelda) {
        for (Reserva r : reservas) {
            if (!Reserva.ACTIVA.equals(r.getEstado())) continue;
            if (!fecha.equals(r.getFecha())) continue;

            boolean seCruza = horaInicioCelda.isBefore(r.getHoraFin()) && r.getHoraInicio().isBefore(horaFinCelda);
            if (seCruza) {
                String funcionario = (r.getFuncionario() != null) ? r.getFuncionario().getNombre() : "";
                return r.getActividad() + " (" + funcionario + ")";
            }
        }
        return "";
    }

    private void generarPDFActividades(){

        String destino = "CalendarioActividades.pdf";

        try{

            PdfWriter writer = new PdfWriter(destino);
            PdfDocument pdf = new PdfDocument(writer);
            Document documento = new Document(pdf);

            documento.add(new Paragraph("Calendario de Actividades"));

            int cantColumnas = actividadesView.getActividadesSemtable().getColumnCount();

            Table tablaPDF = new Table(cantColumnas);

            //Encabezado del documento
            for(int columna =0; columna < cantColumnas; columna++){
                String nombreColumna = actividadesView.getActividadesSemtable().getColumnName(columna);

                tablaPDF.addHeaderCell(new Cell().add(new Paragraph(nombreColumna)));
            }

            //Filas
            int cantFilas = actividadesView.getActividadesSemtable().getRowCount();

            for(int fila =0; fila < cantFilas; fila++){
                for(int columna =0; columna < cantColumnas; columna++){
                    Object valor = actividadesView.getActividadesSemtable().getValueAt(fila, columna);

                    String texto;


                    if(valor == null){
                        texto = "";
                    }else{
                        texto = valor.toString();
                    }

                    tablaPDF.addCell(new Cell().add(new Paragraph(texto)));
                }
            }

            documento.add(tablaPDF);
            documento.close();

            abrirPDF(destino);
        }catch(Exception e){
            JOptionPane.showMessageDialog(null, "No se genero el PDF: " + e.getMessage());
        }
    }

    private void abrirPDF(String ruta){
        try{
            File archivo = new File(ruta);

            if(archivo.exists()){
                if(Desktop.isDesktopSupported()){
                    Desktop.getDesktop().open(archivo);
                }else{
                    JOptionPane.showMessageDialog(null, "No se puede abrir el PDF automaticamente");
                }
            }else{
                JOptionPane.showMessageDialog(null, "No existe el archivo PDF");
            }
        }catch(Exception e){
            JOptionPane.showMessageDialog(null, "No se puede abrir el PDF:  " + e.getMessage());
        }
    }

    public CalendarioActividadesView getActividadesView() {
        return actividadesView;
    }

    public CalendarioModel getModel() {
        return model;
    }

    public TablaModel getTableModel() {
        return tableModel;
    }


}