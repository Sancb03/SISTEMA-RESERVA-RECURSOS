package reserva.presentation.Estadisticas;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.DayOfWeek;

public class EstadisticasController {
    private final EstadisticasView view;
    private final EstadisticasModel model;
    private final TablaModel tableModel;

    public EstadisticasController(EstadisticasView view) {
        this.view = view;
        this.model = new EstadisticasModel();
        this.tableModel = new TablaModel();

        configurarTablas();
        configurarEventos();
    }

    public EstadisticasController(EstadisticasView view, EstadisticasModel model, TablaModel tableModel) {
        this.view = view;
        this.model = model;
        this.tableModel = tableModel;

        configurarTablas();
        configurarEventos();
    }

    public EstadisticasView getView() {
        return view;
    }

    public EstadisticasModel getModel() {
        return model;
    }

    public TablaModel getTableModel() {
        return tableModel;
    }

    private void configurarEventos(){
        view.getCargarRecButton().addActionListener(e -> cargarEstadisticasRecursos());

        view.getCargarActButton().addActionListener(e -> cargarEstadisticasActividades());
    }

    private void configurarTablas(){
        DefaultTableModel modelRecursos = new DefaultTableModel(new Object[]{"Categoria", "Cantidad"}, 0);

        view.getEstadisticasRectable().setModel(modelRecursos);

        DefaultTableModel modelActividades = new DefaultTableModel(new Object[]{"Semana", "Cantidad"}, 0);

        view.getEstadisticasActtable().setModel(modelActividades);
    }

    private void cargarEstadisticasActividades(){
        LocalDate inicio = view.getDPActividadesInicio().getDate();
        LocalDate fin = view.getDPActividadesFin().getDate();

        if(inicio == null || fin == null){
            JOptionPane.showMessageDialog(null, "Seleccione las fechas");
            return;
        }

        if(inicio.isAfter(fin)){
            JOptionPane.showMessageDialog(null, "La fecha de inicio no puede ser despues a la del fin");
            return;
        }

        cargarSemanas(inicio, fin);
        generarGraficoActividades();
    }

    private void cargarSemanas(LocalDate inicio, LocalDate fin){
        DefaultTableModel model = (DefaultTableModel) view.getEstadisticasActtable().getModel();

        model.setRowCount(0);

        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        LocalDate lunes = inicio.with(DayOfWeek.MONDAY);

        while(!lunes.isAfter(fin)){
            LocalDate domingo = lunes.plusDays(6);

            String semana = lunes.format(formato) + " - " + domingo.format(formato);

            model.addRow(new Object[]{semana, 0});

            lunes = lunes.plusWeeks(1);
        }

    }

    private void generarGraficoActividades(){
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        JTable tabla = view.getEstadisticasActtable();

        for(int fila = 0; fila < tabla.getRowCount(); fila++){
            Object semana = tabla.getValueAt(fila, 0);

            Object cantidad = tabla.getValueAt(fila, 1);

            if(semana != null && cantidad != null){
                int valor = Integer.parseInt(cantidad.toString());

                dataset.addValue(valor, "Actividades", semana.toString());
            }
        }

        JFreeChart chart = ChartFactory.createBarChart("Actividades por semana", "Semana", "Cantidad", dataset);

        ChartPanel chartPanel = new ChartPanel(chart);

        JPanel panel = view.getGraficoActPanel();

        panel.removeAll();

        panel.setLayout(new BorderLayout());

        panel.add(chartPanel, BorderLayout.CENTER);

        panel.revalidate();
        panel.repaint();
    }

    private void cargarEstadisticasRecursos(){
        LocalDate inicio = view.getDPRecursosInicio().getDate();

        LocalDate fin = view.getDPRecursosFin().getDate();

        if(inicio == null || fin == null){
            JOptionPane.showMessageDialog(null, "Seleccione las fechas");
            return;
        }

        if(inicio.isAfter(fin)){
            JOptionPane.showMessageDialog(null, "La fecha de inicio no puede despues a la del fin");
            return;
        }

        cargarCategoriasRecursos();
        generarGraficoRecursos();
    }

    private void cargarCategoriasRecursos(){
        DefaultTableModel model = (DefaultTableModel) view.getEstadisticasRectable().getModel();

        model.setRowCount(0);

        //DATOS TEMPORALES SOLO PARA PROBAR LAS TABLAS

        model.addRow(new Object[]{"Salas", 4});
        model.addRow(new Object[]{"Computadoras", 7});

        model.addRow(new Object[]{"Proyectores", 3});
    }

    private void generarGraficoRecursos(){
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        JTable tabla = view.getEstadisticasRectable();

        for(int fila =0; fila < tabla.getRowCount(); fila++){
            Object categoria = tabla.getValueAt(fila, 0);

            Object cantidad = tabla.getValueAt(fila, 1);

            if(categoria != null && cantidad != null){
                int valor = Integer.parseInt(cantidad.toString());

                dataset.addValue(valor, "Recursos", categoria.toString());
            }
        }

        JFreeChart chart =ChartFactory.createBarChart("Recursos Reservados por Categoria", "Categoria", "Cantidad", dataset);

        ChartPanel chartPanel = new ChartPanel(chart);

        JPanel panel = view.getGraficoRecPanel();

        panel.removeAll();
        panel.setLayout(new BorderLayout());
        panel.add(chartPanel, BorderLayout.CENTER);
        panel.revalidate();
        panel.repaint();
    }
}
