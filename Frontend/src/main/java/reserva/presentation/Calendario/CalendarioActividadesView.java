package reserva.presentation.Calendario;

import com.github.lgooddatepicker.components.DatePicker;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class CalendarioActividadesView implements PropertyChangeListener {

    private JButton FechaRefbutton;
    private JButton cargarButton;
    private JButton imprimirButton;
    private JTable ActividadesSemtable;
    private JPanel SemanaPanel;
    private JLabel FechaRefLabel;
    private JScrollPane ActividadesSemScroll;
    private DatePicker datePickerA;
    private JPanel panelPrincipal;

    private CalendarioController controller;
    private CalendarioModel model;

    public CalendarioActividadesView() {

        cargarButton.addActionListener(e -> {
            controller.cargarCalendarioActividades(
                    datePickerA.getDate()
            );
        });

        imprimirButton.addActionListener(e -> {
            controller.generarPDFActividades();
        });
    }

    public void setController(CalendarioController controller) {
        this.controller = controller;
    }

    public void setModel(CalendarioModel model) {
        this.model = model;
        model.addPropertyChangeListener(this);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {

        switch (evt.getPropertyName()) {

            case CalendarioModel.ACTIVIDADES:

                ActividadesSemtable.setModel(
                        new TableModel(
                                model.getColumnasActividades(),
                                model.getFilasActividades()
                        )
                );

                break;
        }

        panelPrincipal.revalidate();
        panelPrincipal.repaint();
    }

    public JPanel getPanelPrincipal() {
        return panelPrincipal;
    }

    public DatePicker getDatePickerA() {
        return datePickerA;
    }

    public JButton getFechaRef_button() {
        return FechaRefbutton;
    }

    public JButton getCargarButton() {
        return cargarButton;
    }

    public JButton getImprimirButton() {
        return imprimirButton;
    }

    public JTable getActividadesSemtable() {
        return ActividadesSemtable;
    }
}