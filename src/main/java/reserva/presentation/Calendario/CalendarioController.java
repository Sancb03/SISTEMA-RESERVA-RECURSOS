package reserva.presentation.Calendario;

import reserva.GeneradorPDF;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import java.time.DayOfWeek;
import java.time.format.DateTimeFormatter;

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

        recursosView.getImprimirButton().addActionListener(e -> GeneradorPDF.generarDesdeTabla
                (recursosView.getCalendarioRectable(), "Calendario de Recursos", "CalendarioRecursos.pdf"));

        actividadesView.getCargarButton().addActionListener(e -> cargarCalendarioActividades());

        actividadesView.getImprimirButton().addActionListener(e-> GeneradorPDF.generarDesdeTabla
                (actividadesView.getActividadesSemtable(), "Calendario de Actividades", "CalendarioActividades.pdf"));
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
