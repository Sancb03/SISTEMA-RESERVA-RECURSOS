package reserva.presentation.Alertas;

import reserva.AbstractTableModel;
import reserva.logic.Reserva;
import reserva.presentation.Iconos;

import javax.swing.Icon;
import java.util.List;

/** Columnas de la tabla de alertas: Actividad | Ver | Desc. (descartar). */
public class AlertasTableModel extends AbstractTableModel<Reserva> {

    public static final int ACTIVIDAD = 0;
    public static final int VER = 1;
    public static final int DESCARTAR = 2;

    public AlertasTableModel(int[] cols, List<Reserva> rows) {
        super(cols, rows);
    }

    @Override
    protected void initColNames() {
        colNames = new String[3];
        colNames[ACTIVIDAD] = "Actividad";
        colNames[VER] = "Ver";
        colNames[DESCARTAR] = "Desc.";
    }

    @Override
    protected Object getPropertyAt(Reserva r, int col) {
        switch (cols[col]) {
            case ACTIVIDAD:
                String quien = r.getFuncionario() != null ? r.getFuncionario().getNombre() : "?";
                return quien + " - " + r.getActividad();
            case VER:
                return icono("detalles", "Ver");
            case DESCARTAR:
                return icono("descartar", "X");
            default:
                return "";
        }
    }

    private Object icono(String nombre, String alternativa) {
        Icon icon = Iconos.get(nombre);
        return icon != null ? icon : alternativa;
    }

    @Override
    public Class<?> getColumnClass(int col) {
        if (cols[col] == VER && Iconos.get("detalles") != null) {
            return Icon.class;
        }
        if (cols[col] == DESCARTAR && Iconos.get("descartar") != null) {
            return Icon.class;
        }
        return String.class;
    }

    /** Refresca la tabla cuando cambió la lista (que es la misma que tiene el Model). */
    public void refrescar() {
        fireTableDataChanged();
    }
}
