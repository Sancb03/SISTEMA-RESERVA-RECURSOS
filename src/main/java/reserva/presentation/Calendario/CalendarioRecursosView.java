package reserva.presentation.Calendario;

import com.github.lgooddatepicker.components.DatePicker;

import javax.swing.*;

public class CalendarioRecursosView {
    private JTextField Fecha_tField;
    private JButton Fechabutton;
    private JComboBox Descripcion_cBox;
    private JButton cargarButton;
    private JButton imprimirButton;
    private JTable CalendarioRectable;
    private JPanel FiltosPanel;
    private JLabel FechaLabel;
    private JLabel DescripcionLable;
    private JScrollPane CalendarioRecScroll;
    private DatePicker datePicker;
    private JPanel panelPrincipal;

    public JPanel getPanel() { return panelPrincipal; }

    public JTextField getFecha_tField() {
        return Fecha_tField;
    }

    public JButton getFechabutton() { return Fechabutton; }

    public JComboBox getDescripcion_cBox() {
        return Descripcion_cBox;
    }

    public JButton getCargarButton() {
        return cargarButton;
    }

    public JTable getCalendarioRectable() { return CalendarioRectable; }

    public JButton getImprimirButton() { return imprimirButton; }

    public DatePicker getDatePicker() { return datePicker; }

}
