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

        cargarRecursos(inicio, fin, true);
    }

    private void cargarRecursos(LocalDate inicio, LocalDate fin, boolean avisarSinDatos) {

        if (!rangoValido(inicio, fin)) {
            return;
        }

        rangoRecInicio = inicio;
        rangoRecFin = fin;

        try {
            cargarCategoriasRecursos(inicio, fin);
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(
                    null,
                    ex.getMessage(),
                    "Estadísticas",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        if (EstadisticasCalculo.sinDatos(model.getRecursos())) {
            graficoRecursos = null;
            vaciarPanel(view.getGraficoRecPanel());

            if (avisarSinDatos) {
                JOptionPane.showMessageDialog(
                        null,
                        "No hay reservas activas entre " + formatear(inicio) + " y " + formatear(fin) + ".",
                        "Sin datos",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }
            return;
        }

        generarGraficoRecursos();
    }

    /** Fechas vacías o fin antes del inicio: avisa y no se calcula nada. */
    private boolean rangoValido(LocalDate inicio, LocalDate fin) {

        if (inicio == null || fin == null) {
            JOptionPane.showMessageDialog(
                    null,
                    "Seleccione la fecha de inicio y la fecha de fin."
            );
            return false;
        }

        if (inicio.isAfter(fin)) {
            JOptionPane.showMessageDialog(
                    null,
                    "La fecha de inicio no puede ser posterior a la fecha de fin."
            );
            return false;
        }

        return true;
    }

    private void vaciarPanel(JPanel panel) {
        panel.removeAll();
        panel.revalidate();
        panel.repaint();
    }

    /** Vuelve a calcular con el último rango usado: al regresar a la pestaña las reservas pudieron cambiar. */
    public void refrescar() {

        if (rangoRecInicio != null) {
            cargarRecursos(rangoRecInicio, rangoRecFin, false);
        }

        if (rangoActInicio != null) {
            cargarActividades(rangoActInicio, rangoActFin, false);
        }
    }

    private void cargarCategoriasRecursos(LocalDate inicio, LocalDate fin) {

        List<Reserva> reservas = Service.instance().listarReservas();

        model.setRecursos(EstadisticasCalculo.recursosPorCategoria(reservas, inicio, fin));
    }

    public void cargarEstadisticasActividades(
            LocalDate inicio,
            LocalDate fin) {

        cargarActividades(inicio, fin, true);
    }

    private void cargarActividades(LocalDate inicio, LocalDate fin, boolean avisarSinDatos) {

        if (!rangoValido(inicio, fin)) {
            return;
        }

        rangoActInicio = inicio;
        rangoActFin = fin;

        try {
            cargarSemanas(inicio, fin);
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(
                    null,
                    ex.getMessage(),
                    "Estadísticas",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        if (EstadisticasCalculo.sinDatos(model.getActividades())) {
            graficoActividades = null;
            vaciarPanel(view.getGraficoActPanel());

            if (avisarSinDatos) {
                JOptionPane.showMessageDialog(
                        null,
                        "No hay reservas activas entre " + formatear(inicio) + " y " + formatear(fin) + ".",
                        "Sin datos",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }
            return;
        }

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
                    "No hay estadísticas para exportar. Cargue primero un rango que tenga reservas."
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
                    "No hay estadísticas para exportar. Cargue primero un rango que tenga reservas."
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