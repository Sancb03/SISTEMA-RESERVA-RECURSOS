package reserva.presentation.Calendario;

import com.github.lgooddatepicker.components.DatePicker;

import javax.swing.*;

public class CalendarioActividadesView {
    private JButton FechaRefbutton;
    private JButton cargarButton;
    private JButton imprimirButton;
    private JTable ActividadesSemtable;
    private JPanel SemanaPanel;
    private JLabel FechaRefLabel;
    private JScrollPane ActividadesSemScroll;
    private DatePicker datePickerA;
    private JPanel panelPrincipal;

    public JPanel getPanelPrincipal(){ return panelPrincipal; }

    public DatePicker getDatePickerA(){ return datePickerA; }

    public JButton getFechaRef_button() { return FechaRefbutton; }

    public JButton getCargarButton() { return cargarButton; }

    public JButton getImprimirButton() { return imprimirButton; }

    public JTable getActividadesSemtable() { return ActividadesSemtable; }
}
