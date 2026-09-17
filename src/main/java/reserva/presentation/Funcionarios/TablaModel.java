package reserva.presentation.Funcionarios;

import reserva.logic.Funcionario;
import reserva.AbstractTableModel;

import java.util.List;

public class TablaModel extends AbstractTableModel<Funcionario> {

    public TablaModel(int[] cols, List<Funcionario> rows) {
        super(cols, rows);
    }

    @Override
    protected Object getPropertyAt(Funcionario f, int col) {

        switch (cols[col]) {

            case ID:
                return f.getId();

            case NOMBRE:
                return f.getNombre();

            case TELEFONO:
                return f.getTelefono();

            default:
                return "";
        }
    }

    @Override
    protected void initColNames() {

        colNames = new String[3];

        colNames[ID] = "ID";
        colNames[NOMBRE] = "Nombre";
        colNames[TELEFONO] = "Teléfono";
    }

    public static final int ID = 0;
    public static final int NOMBRE = 1;
    public static final int TELEFONO = 2;
}