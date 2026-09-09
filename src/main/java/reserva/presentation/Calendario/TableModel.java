package reserva.presentation.Calendario;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

class TablaModel extends AbstractTableModel {
    private List<String> columnas;
    private List<Object[]> filas;

    public TablaModel() {
         columnas = new ArrayList<>();
         filas = new ArrayList<>();
    }

    public List<Object[]> getFilas() {
        return filas;
    }

    public void setFilas(List<Object[]> filasNuevas) {
        filas.clear();
        if (filasNuevas != null) {
            filas.addAll(filasNuevas);
        }
        fireTableDataChanged();
    }

    public void setColumnas(List<String> nuevasColumnas) {
        columnas.clear();

        if(nuevasColumnas != null){
            columnas.addAll(nuevasColumnas);
        }
        fireTableStructureChanged();
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
    public String getColumnName(int column) {
        return columnas.get(column);
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex < 0 || rowIndex >= filas.size()) {
            return null;
        }
        return filas.get(rowIndex)[columnIndex];
    }
}
