package reserva.presentation.Reservas;

import reserva.logic.Recurso;
import reserva.logic.Reserva;
import reserva.AbstractTableModel;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class TablaModel extends AbstractTableModel<Reserva> {

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final DateTimeFormatter FORMATO_HORA =
            DateTimeFormatter.ofPattern("HH:mm");

    public TablaModel(int[] cols, List<Reserva> rows) {
        super(cols, rows);
    }

    @Override
    protected Object getPropertyAt(
            Reserva r,
            int col) {

        switch (cols[col]) {

            case ID:
                return r.getId();

            case ACTIVIDAD:
                return r.getActividad();

            case FECHA:
                return r.getFecha() != null
                        ? r.getFecha().format(FORMATO_FECHA)
                        : "";

            case HORARIO:

                if (r.getHoraInicio() == null ||
                        r.getHoraFin() == null) {

                    return "";
                }

                return r.getHoraInicio().format(FORMATO_HORA)
                        + " - "
                        + r.getHoraFin().format(FORMATO_HORA);

            case RECURSOS:
                return nombresRecursos(r.getRecursos());

            case ESTADO:
                return r.getEstado();

            default:
                return "";
        }
    }

    private String nombresRecursos(
            List<Recurso> recursos) {

        if (recursos == null) {
            return "";
        }

        StringBuilder texto =
                new StringBuilder();

        for (Recurso recurso : recursos) {

            if (texto.length() > 0) {
                texto.append(", ");
            }

            texto.append(recurso.getId());
        }

        return texto.toString();
    }

    @Override
    protected void initColNames() {

        colNames = new String[6];

        colNames[ID] = "Id";
        colNames[ACTIVIDAD] = "Actividad";
        colNames[FECHA] = "Fecha";
        colNames[HORARIO] = "Horario";
        colNames[RECURSOS] = "Recursos";
        colNames[ESTADO] = "Estado";
    }

    public static final int ID = 0;
    public static final int ACTIVIDAD = 1;
    public static final int FECHA = 2;
    public static final int HORARIO = 3;
    public static final int RECURSOS = 4;
    public static final int ESTADO = 5;
}