package reserva.presentation.Estadisticas;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

class TablaModel extends AbstractTableModel {
    private static final String[] COLUMNAS = {"Indicador", "Valor", "Periodo"};
    private final List<Object[]> filas = new ArrayList<>();

    public TablaModel() {
    }

    public List<Object[]> getFilas() {
        return filas;
    }

    public void setFilas(List<Object[]> filas) {
        this.filas.clear();
        if (filas != null) {
            this.filas.addAll(filas);
        }
        fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
        return filas.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMNAS.length;
    }

    @Override
    public String getColumnName(int column) {
        return COLUMNAS[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex < 0 || rowIndex >= filas.size()) {
            return null;
        }
        return filas.get(rowIndex)[columnIndex];
    }
}
