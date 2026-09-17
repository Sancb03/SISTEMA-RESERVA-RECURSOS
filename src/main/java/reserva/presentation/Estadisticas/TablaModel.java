package reserva.presentation.Estadisticas;

import javax.swing.table.AbstractTableModel;
import java.util.List;

public class TablaModel extends AbstractTableModel {

    private final String[] columnas;
    private final List<Object[]> filas;

    public TablaModel(String[] columnas, List<Object[]> filas) {
        this.columnas = columnas;
        this.filas = filas;
    }

    @Override
    public int getRowCount() {
        return filas.size();
    }

    @Override
    public int getColumnCount() {
        return columnas.length;
    }

    @Override
    public String getColumnName(int columna) {
        return columnas[columna];
    }

    @Override
    public Object getValueAt(int fila, int columna) {
        return filas.get(fila)[columna];
    }
}