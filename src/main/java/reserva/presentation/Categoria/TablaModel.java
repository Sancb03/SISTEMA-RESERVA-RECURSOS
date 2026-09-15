package reserva.presentation.Categoria;

import reserva.logic.Categoria;
import reserva.AbstractTableModel;

import java.util.List;

public class TablaModel extends AbstractTableModel<Categoria> {

    public TablaModel(int[] cols, List<Categoria> rows) {
        super(cols, rows);
    }

    @Override
    protected Object getPropertyAt(Categoria c, int col) {

        switch (cols[col]) {

            case ID:
                return c.getId();

            case DESCRIPCION:
                return c.getDescripcion();

            default:
                return "";
        }
    }

    @Override
    protected void initColNames() {

        colNames = new String[2];

        colNames[ID] = "ID";
        colNames[DESCRIPCION] = "Descripción";
    }

    public static final int ID = 0;
    public static final int DESCRIPCION = 1;
}