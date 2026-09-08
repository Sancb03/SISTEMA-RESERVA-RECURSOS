package reserva.presentation.Calendario;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import java.time.DayOfWeek;
import java.time.format.DateTimeFormatter;
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

    public CalendarioController(CalendarioRecursosView recursosView, CalendarioActividadesView actividadesView, CalendarioModel model, TablaModel tableModel) {
        this.recursosView = recursosView;
        this.actividadesView = actividadesView;
        this.model = model;
        this.tableModel = tableModel;
    }

    public CalendarioController(CalendarioRecursosView recursosView, CalendarioActividadesView actividadesView) {

        this.actividadesTableModel = new TablaModel();
        this.recursosView = recursosView;
        this.actividadesView = actividadesView;
        this.model = new CalendarioModel();
        this.tableModel = new TablaModel();

        recursosView.getCalendarioRectable().setModel(tableModel);
        actividadesView.getActividadesSemtable().setModel(actividadesTableModel);

        //metodos del calendario de recursos
        cargarHoras();

        configurarEventos();

        //metodos del calendario de actividades
        cargarCalendarioActividades();

    }

    private void configurarEventos() {
        recursosView.getCargarButton().addActionListener(e -> {cargarCalendarioRecursos();});

        recursosView.getImprimirButton().addActionListener(e -> {generarPDFRecursos();});

        actividadesView.getCargarButton().addActionListener(e -> cargarCalendarioActividades());

        actividadesView.getImprimirButton().addActionListener(e-> generarPDFActividades());
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

        Object categoria = recursosView.getDescripcion_cBox().getSelectedItem();

        if(fecha == null){
            JOptionPane.showMessageDialog(null, "Seleccione una fecha");
            return;
        }

        if(categoria == null){
            JOptionPane.showMessageDialog(null, "Seleccione una categoria");
            return;
        }

        JOptionPane.showMessageDialog(null, "Fecha: " + fecha + "\nCategoria: " + categoria);
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

        List<Object[]> filas = new ArrayList<>();

        for(int hora = 7; hora <= 20; hora++){
            Object[] fila = new Object[8];

            fila[0] = String.format("%02d:00", hora);

            filas.add(fila);
        }
        actividadesTableModel.setFilas(filas);
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
