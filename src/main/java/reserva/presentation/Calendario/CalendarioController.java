package reserva.presentation.Calendario;

import reserva.GeneradorPDF;
import reserva.data.Data;
import reserva.logic.Categoria;
import reserva.logic.Usuario;

import javax.swing.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class CalendarioController {

    private final CalendarioRecursosView recursosView;
    private final CalendarioActividadesView actividadesView;
    private final CalendarioModel model;
    private final Usuario usuarioActual;

    public CalendarioController(
            CalendarioRecursosView recursosView,
            CalendarioActividadesView actividadesView,
            CalendarioModel model,
            Usuario usuarioActual) {

        this.recursosView = recursosView;
        this.actividadesView = actividadesView;
        this.model = model;
        this.usuarioActual = usuarioActual;

        recursosView.setController(this);
        recursosView.setModel(model);

        actividadesView.setController(this);
        actividadesView.setModel(model);

        cargarHorasRecursos();
        cargarCategoriasRecursos();
    }

    public CalendarioController(
            CalendarioRecursosView recursosView,
            CalendarioActividadesView actividadesView, Usuario usuarioActual) {

        this(
                recursosView,
                actividadesView,
                new CalendarioModel(),
                usuarioActual
        );
    }
    public void cargarCategoriasRecursos() {

        try {
            List<Categoria> categorias =
                    Data.instance().listarCategorias(usuarioActual);

            recursosView.cargarCategoriasEnCombo(categorias);

        } catch (RuntimeException ex) {

            JOptionPane.showMessageDialog(
                    null,
                    ex.getMessage()
            );
        }
    }

    private void cargarHorasRecursos() {

        List<String> columnas = new ArrayList<>();
        columnas.add("Hora");

        List<Object[]> filas = new ArrayList<>();

        for (int hora = 7; hora <= 20; hora++) {

            Object[] fila = new Object[1];

            fila[0] = String.format("%02d:00", hora);

            filas.add(fila);
        }

        model.setColumnasRecursos(columnas);
        model.setFilasRecursos(filas);
    }

    public void cargarCalendarioRecursos(
            LocalDate fecha,
            Object categoria) {

        if (fecha == null) {

            JOptionPane.showMessageDialog(
                    null,
                    "Seleccione una fecha"
            );

            return;
        }

        if (categoria == null) {

            JOptionPane.showMessageDialog(
                    null,
                    "Seleccione una categoria"
            );

            return;
        }

        JOptionPane.showMessageDialog(
                null,
                "Fecha: " + fecha +
                        "\nCategoria: " + categoria
        );
    }

    public void cargarCalendarioActividades(LocalDate fecha) {

        if (fecha == null) {

            JOptionPane.showMessageDialog(
                    null,
                    "Seleccione una fecha"
            );

            return;
        }

        DateTimeFormatter formato =
                DateTimeFormatter.ofPattern("dd/MM");

        LocalDate semana =
                fecha.with(DayOfWeek.MONDAY);

        List<String> columnas = new ArrayList<>();

        columnas.add("Hora");

        columnas.add(
                "Lunes " +
                        semana.format(formato)
        );

        columnas.add(
                "Martes " +
                        semana.plusDays(1).format(formato)
        );

        columnas.add(
                "Miercoles " +
                        semana.plusDays(2).format(formato)
        );

        columnas.add(
                "Jueves " +
                        semana.plusDays(3).format(formato)
        );

        columnas.add(
                "Viernes " +
                        semana.plusDays(4).format(formato)
        );

        columnas.add(
                "Sabado " +
                        semana.plusDays(5).format(formato)
        );

        columnas.add(
                "Domingo " +
                        semana.plusDays(6).format(formato)
        );

        List<Object[]> filas = new ArrayList<>();

        for (int hora = 7; hora <= 20; hora++) {

            Object[] fila = new Object[8];

            fila[0] =
                    String.format("%02d:00", hora);

            filas.add(fila);
        }

        model.setColumnasActividades(columnas);
        model.setFilasActividades(filas);
    }

    public void generarPDFRecursos() {

        GeneradorPDF.generarDesdeTabla(
                recursosView.getCalendarioRectable(),
                "Calendario de Recursos",
                "CalendarioRecursos.pdf"
        );
    }

    public void generarPDFActividades() {

        GeneradorPDF.generarDesdeTabla(
                actividadesView.getActividadesSemtable(),
                "Calendario de Actividades",
                "CalendarioActividades.pdf"
        );
    }

    public CalendarioRecursosView getRecursosView() {
        return recursosView;
    }

    public CalendarioActividadesView getActividadesView() {
        return actividadesView;
    }

    public CalendarioModel getModel() {
        return model;
    }
}