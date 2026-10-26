package reserva.presentation.Estadisticas;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import java.awt.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

import reserva.logic.*;
import reserva.GeneradorPDF;
import reserva.presentation.Iconos;

public class EstadisticasController {

    private final EstadisticasView view;
    private final EstadisticasModel model;

    // ultimos graficos y rangos mostrados, para poder meterlos en el PDF
    private JFreeChart graficoRecursos;
    private JFreeChart graficoActividades;
    private LocalDate rangoRecInicio;
    private LocalDate rangoRecFin;
    private LocalDate rangoActInicio;
    private LocalDate rangoActFin;

    public EstadisticasController(EstadisticasView view) {
        this(
                view,
                new EstadisticasModel()
        );
    }

    public EstadisticasController(
            EstadisticasView view,
            EstadisticasModel model) {

        this.view = view;
        this.model = model;

        view.setController(this);
        view.setModel(model);

        view.getPdfRecButton().setIcon(Iconos.get("pdf"));
        view.getPdfActButton().setIcon(Iconos.get("pdf"));
    }

    public void cargarEstadisticasRecursos(
            LocalDate inicio,
            LocalDate fin) {

        if (inicio == null || fin == null) {
            JOptionPane.showMessageDialog(
                    null,
                    "Seleccione las fechas"
            );
            return;
        }

        if (inicio.isAfter(fin)) {
            JOptionPane.showMessageDialog(
                    null,
                    "La fecha de inicio no puede ser despues a la del fin"
            );
            return;
        }

        cargarCategoriasRecursos(inicio, fin);
        rangoRecInicio = inicio;
        rangoRecFin = fin;

        generarGraficoRecursos();
    }

    private void cargarCategoriasRecursos(LocalDate inicio, LocalDate fin) {

        List<Reserva> reservas = Service.instance().listarReservas();

        model.setRecursos(EstadisticasCalculo.recursosPorCategoria(reservas, inicio, fin));
    }

    public void cargarEstadisticasActividades(
            LocalDate inicio,
            LocalDate fin) {

        if (inicio == null || fin == null) {
            JOptionPane.showMessageDialog(
                    null,
                    "Seleccione las fechas"
            );
            return;
        }

        if (inicio.isAfter(fin)) {
            JOptionPane.showMessageDialog(
                    null,
                    "La fecha de inicio no puede ser despues a la del fin"
            );
            return;
        }

        cargarSemanas(inicio, fin);
        rangoActInicio = inicio;
        rangoActFin = fin;

        generarGraficoActividades();
    }

    private void cargarSemanas(LocalDate inicio, LocalDate fin) {

        List<Reserva> reservas = Service.instance().listarReservas();

        model.setActividades(EstadisticasCalculo.actividadesPorSemana(reservas, inicio, fin));
    }

    private void generarGraficoRecursos() {

        DefaultCategoryDataset dataset =
                new DefaultCategoryDataset();

        for (Object[] fila : model.getRecursos()) {

            Object categoria = fila[0];
            Object cantidad = fila[1];

            if (categoria != null && cantidad != null) {

                int valor =
                        Integer.parseInt(
                                cantidad.toString()
                        );

                dataset.addValue(
                        valor,
                        "Recursos",
                        categoria.toString()
                );
            }
        }

        JFreeChart chart =
                ChartFactory.createBarChart(
                        "Recursos Reservados por Categoria",
                        "Categoria",
                        "Cantidad",
                        dataset
                );

        graficoRecursos = chart;

        ChartPanel chartPanel = new ChartPanel(chart);

        JPanel panel = view.getGraficoRecPanel();

        panel.removeAll();

        panel.setLayout(
                new BorderLayout()
        );

        panel.add(
                chartPanel,
                BorderLayout.CENTER
        );

        panel.revalidate();
        panel.repaint();
    }

    private void generarGraficoActividades() {

        DefaultCategoryDataset dataset =
                new DefaultCategoryDataset();

        for (Object[] fila : model.getActividades()) {

            Object semana = fila[0];
            Object cantidad = fila[1];

            if (semana != null && cantidad != null) {

                int valor =
                        Integer.parseInt(
                                cantidad.toString()
                        );

                dataset.addValue(
                        valor,
                        "Actividades",
                        semana.toString()
                );
            }
        }

        JFreeChart chart =
                ChartFactory.createBarChart(
                        "Actividades por semana",
                        "Semana",
                        "Cantidad",
                        dataset
                );

        graficoActividades = chart;

        ChartPanel chartPanel = new ChartPanel(chart);

        JPanel panel =
                view.getGraficoActPanel();

        panel.removeAll();

        panel.setLayout(
                new BorderLayout()
        );

        panel.add(
                chartPanel,
                BorderLayout.CENTER
        );

        panel.revalidate();
        panel.repaint();
    }

    public void generarPDFRecursos() {

        if (graficoRecursos == null || rangoRecInicio == null) {
            JOptionPane.showMessageDialog(
                    null,
                    "Primero cargue las estadisticas de recursos"
            );
            return;
        }

        GeneradorPDF.generarDesdeTablaConGrafico(
                view.getEstadisticasRectable(),
                graficoRecursos.createBufferedImage(600, 350),
                "Recursos reservados del " + formatear(rangoRecInicio) + " al " + formatear(rangoRecFin),
                "EstadisticasRecursos.pdf"
        );
    }

    public void generarPDFActividades() {

        if (graficoActividades == null || rangoActInicio == null) {
            JOptionPane.showMessageDialog(
                    null,
                    "Primero cargue las estadisticas de actividades"
            );
            return;
        }

        GeneradorPDF.generarDesdeTablaConGrafico(
                view.getEstadisticasActtable(),
                graficoActividades.createBufferedImage(600, 350),
                "Actividades por semana del " + formatear(rangoActInicio) + " al " + formatear(rangoActFin),
                "EstadisticasActividades.pdf"
        );
    }

    private String formatear(LocalDate fecha) {
        return fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    public EstadisticasView getView() {
        return view;
    }

    public EstadisticasModel getModel() {
        return model;
    }
}