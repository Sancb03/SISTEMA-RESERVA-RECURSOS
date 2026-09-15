package reserva.presentation.Recursos;

import reserva.logic.Recurso;
import reserva.AbstractTableModel;

import java.util.List;

public class TablaModel extends AbstractTableModel<Recurso> {

    public TablaModel(int[] cols, List<Recurso> rows) {
        super(cols, rows);
    }

    @Override
    protected Object getPropertyAt(Recurso r, int col) {

        switch (cols[col]) {

            case ID:
                return r.getId();

            case CATEGORIA:
                return r.getCategoria() != null
                        ? r.getCategoria().getDescripcion()
                        : "";

            case DESCRIPCION:
                return r.getDescripcion();

            default:
                return "";
        }
    }

    @Override
    protected void initColNames() {

        colNames = new String[3];

        colNames[ID] = "Id";
        colNames[CATEGORIA] = "Categoría";
        colNames[DESCRIPCION] = "Descripción";
    }

    public static final int ID = 0;
    public static final int CATEGORIA = 1;
    public static final int DESCRIPCION = 2;
}