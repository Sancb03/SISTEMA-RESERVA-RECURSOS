package reserva;

import java.util.List;

public abstract class AbstractTableModel<E>
        extends javax.swing.table.AbstractTableModel
        implements javax.swing.table.TableModel {

    protected List<E> rows;
    protected int[] cols;
    protected String[] colNames;

    public AbstractTableModel(int[] cols, List<E> rows) {
        this.cols = cols;
        this.rows = rows;
        initColNames();
    }

    protected AbstractTableModel() {
    }

    @Override
    public int getColumnCount() {
        return cols.length;
    }

    public Class<?> getColumnClass(int col) {
        switch (cols[col]) {
            default:
                return super.getColumnClass(col);
        }
    }

    @Override
    public String getColumnName(int col) {
        return colNames[cols[col]];
    }

    @Override
    public int getRowCount() {
        return rows.size();
    }

    @Override
    public Object getValueAt(int row, int col) {
        return getPropertyAt(rows.get(row), col);
    }

    public E getRowAt(int row) {
        return rows.get(row);
    }

    protected abstract Object getPropertyAt(E e, int col);

    protected abstract void initColNames();
}