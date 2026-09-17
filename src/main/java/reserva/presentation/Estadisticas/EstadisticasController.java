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

import reserva.data.*;
import reserva.logic.*;

public class EstadisticasController {

    private final EstadisticasView view;
    private final EstadisticasModel model;

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

        generarGraficoRecursos();
    }

    private void cargarCategoriasRecursos(LocalDate inicio, LocalDate fin) {

        List<Reserva> reservas = Data.instance().listarReservas();

        Map<String, Integer> cantidades = new LinkedHashMap<>();

        for(Reserva reserva : reservas) {
            if(reserva.getFecha() == null){
                continue;
            }

            if(reserva.getFecha().isBefore(inicio) || reserva.getFecha().isAfter(fin)){
                continue;
            }

            if(!Reserva.ACTIVA.equals(reserva.getEstado())){
                continue;
            }

            for(Recurso recurso : reserva.getRecursos()){
                if(recurso.getCategoria() == null){
                    continue;
                }

                String categoria = recurso.getCategoria().getDescripcion();

                cantidades.put(categoria, cantidades.getOrDefault(categoria, 0) + 1);
            }
        }

        List<Object[]> filas = new ArrayList<>();

        for(Map.Entry<String, Integer> dato : cantidades.entrySet()) {
            filas.add(new Object[]{dato.getKey(), dato.getValue()});
        }

        model.setRecursos(filas);
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

        generarGraficoActividades();
    }

    private void cargarSemanas(LocalDate inicio, LocalDate fin) {

        List<Reserva> reservas = Data.instance().listarReservas();

        List<Object[]> filas = new ArrayList<>();

        DateTimeFormatter formato =
                DateTimeFormatter.ofPattern("dd/MM/yyyy");

        LocalDate lunes =
                inicio.with(DayOfWeek.MONDAY);

        while (!lunes.isAfter(fin)) {

            LocalDate domingo =
                    lunes.plusDays(6);

            int cantidad = 0;

            for(Reserva reserva : reservas) {
                if(reserva.getFecha() == null){
                    continue;
                }

                if(!Reserva.ACTIVA.equals(reserva.getEstado())){
                    continue;
                }

                LocalDate fechaReserva = reserva.getFecha();

                boolean dentroDelRangoSleccionado = !fechaReserva.isBefore(inicio) && !fechaReserva.isAfter(fin);

                boolean dentroDeLaSemana = !fechaReserva.isBefore(lunes) && !fechaReserva.isAfter(domingo);

                if(dentroDelRangoSleccionado && dentroDeLaSemana) {
                    cantidad++;
                }
            }

            String semana = lunes.format(formato) + " - " + domingo.format(formato);

            filas.add( new Object[]{ semana, cantidad } );

            lunes = lunes.plusWeeks(1);
        }

        model.setActividades(filas);
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

    public EstadisticasView getView() {
        return view;
    }

    public EstadisticasModel getModel() {
        return model;
    }
}