package reserva.presentation.Calendario;

import javax.swing.table.AbstractTableModel;
import java.util.List;

public class TableModel extends AbstractTableModel {

    private final List<String> columnas;
    private final List<Object[]> filas;

    public TableModel(List<String> columnas, List<Object[]> filas) {
        this.columnas = columnas;
        this.filas = filas;
    }

    @Override
    public int getRowCount() {
        return filas.size();
    }

    @Override
    public int getColumnCount() {
        return columnas.size();
    }

    @Override
    public String getColumnName(int columna) {
        return columnas.get(columna);
    }

    @Override
    public Object getValueAt(int fila, int columna) {
        return filas.get(fila)[columna];
    }
}