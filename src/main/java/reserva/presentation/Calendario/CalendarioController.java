package reserva.presentation.Calendario;

import reserva.GeneradorPDF;
import reserva.data.Data;
import reserva.logic.Categoria;
import reserva.logic.Usuario;
import reserva.logic.Recurso;
import reserva.logic.Reserva;

import javax.swing.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.LocalTime;
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
            List<Categoria> categorias = Data.instance().listarCategorias(usuarioActual);

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

    public void cargarCalendarioRecursos(LocalDate fecha, Categoria categoria) {

        if (fecha == null) {
            JOptionPane.showMessageDialog(null, "Seleccione una fecha");

            return;
        }

        if (categoria == null) {
            JOptionPane.showMessageDialog(null, "Seleccione una categoria");

            return;
        }

        try {

            List<Recurso> recursos = Data.instance().buscarRecursosPorCategoria(categoria, usuarioActual);

            List<Reserva> reservas = Data.instance().listarReservas();

            List<String> columnas = new ArrayList<>();

            columnas.add("Hora");

            for (Recurso recurso : recursos) {
                columnas.add(recurso.getId());
            }

            List<Object[]> filas = new ArrayList<>();

            for (int hora = 7; hora <= 20; hora++) {

                Object[] fila = new Object[recursos.size() + 1];

                fila[0] = String.format("%02d:00", hora);

                LocalTime inicio = LocalTime.of(hora, 0);

                LocalTime fin = inicio.plusHours(1);

                for (int i = 0; i < recursos.size(); i++) {

                    Recurso recurso = recursos.get(i);

                    Reserva reserva = buscarReservaRecurso(reservas, recurso, fecha, inicio, fin);

                    if (reserva == null) {

                        fila[i + 1] = "Disponible";

                    } else {

                        String funcionario = "";

                        if (reserva.getFuncionario() != null) {

                            funcionario = reserva.getFuncionario().getNombre();
                        }

                        fila[i + 1] = reserva.getActividad() + " - " + funcionario;
                    }
                }

                filas.add(fila);
            }

            model.setColumnasRecursos(columnas);
            model.setFilasRecursos(filas);

        } catch (RuntimeException ex) {

            JOptionPane.showMessageDialog(
                    null,
                    ex.getMessage()
            );
        }
    }

    public void cargarCalendarioActividades(LocalDate fecha) {

        if (fecha == null) {

            JOptionPane.showMessageDialog(null, "Seleccione una fecha");

            return;
        }

        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM");

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

        List<Reserva> reservas = Data.instance().listarReservas();

        List<Object[]> filas = new ArrayList<>();

        for (int hora = 7; hora <= 20; hora++) {

            Object[] fila = new Object[8];

            fila[0] = String.format("%02d:00", hora);

            LocalTime inicio = LocalTime.of(hora, 0);

            LocalTime fin = inicio.plusHours(1);

            for (int dia = 0; dia < 7; dia++) {

                LocalDate fechaDia = semana.plusDays(dia);

                fila[dia + 1] = buscarActividades(reservas, fechaDia, inicio, fin);
            }

            filas.add(fila);
        }

        model.setColumnasActividades(columnas);
        model.setFilasActividades(filas);
    }

    private Reserva buscarReservaRecurso(List<Reserva> reservas, Recurso recurso,
            LocalDate fecha, LocalTime inicio, LocalTime fin) {

        for (Reserva reserva : reservas) {

            if (!Reserva.ACTIVA.equals(reserva.getEstado())) {
                continue;
            }

            if (reserva.getFecha() == null || !reserva.getFecha().equals(fecha)) {
                continue;
            }

            if (reserva.getRecursos() == null) {
                continue;
            }

            boolean usaRecurso = false;

            for (Recurso r : reserva.getRecursos()) {

                if (r.getId().equals(recurso.getId())) {
                    usaRecurso = true;
                    break;
                }
            }

            if (!usaRecurso) {
                continue;
            }

            boolean coincideHorario = inicio.isBefore(reserva.getHoraFin())
                            && reserva.getHoraInicio().isBefore(fin);

            if (coincideHorario) {
                return reserva;
            }
        }

        return null;
    }

    private String buscarActividades(List<Reserva> reservas,
            LocalDate fecha, LocalTime inicio, LocalTime fin) {

        List<String> actividades = new ArrayList<>();

        for (Reserva reserva : reservas) {

            if (!Reserva.ACTIVA.equals(reserva.getEstado())) {
                continue;
            }

            if (reserva.getFecha() == null || !reserva.getFecha().equals(fecha)) {
                continue;
            }

            boolean coincideHorario = inicio.isBefore(reserva.getHoraFin()) &&
                            reserva.getHoraInicio().isBefore(fin);

            if (!coincideHorario) {
                continue;
            }

            String funcionario = "";

            if (reserva.getFuncionario() != null) {
                funcionario = reserva.getFuncionario().getNombre();
            }

            actividades.add(reserva.getActividad() + " - " + funcionario);
        }

        if (actividades.isEmpty()) {
            return "";
        }

        return String.join(" | ", actividades);
    }

    public void generarPDFRecursos() {

        LocalDate fecha = recursosView.getDatePicker().getDate();

        String titulo = "Calendario de Recursos";

        if(fecha != null) {
            DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            titulo += " - " + fecha.format(formato);
        }

        GeneradorPDF.generarDesdeTabla(
                recursosView.getCalendarioRectable(),
                titulo, "CalendarioRecursos.pdf"
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