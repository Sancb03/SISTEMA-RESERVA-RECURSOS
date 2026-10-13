package reserva.presentation.Calendario;

import com.github.lgooddatepicker.components.DatePicker;
import reserva.logic.Categoria;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

public class CalendarioRecursosView implements PropertyChangeListener {

    private JTextField Fecha_tField;
    private JButton Fechabutton;
    private JComboBox<Categoria> Descripcion_cBox;
    private JButton cargarButton;
    private JButton imprimirButton;
    private JTable CalendarioRectable;
    private JPanel FiltosPanel;
    private JLabel FechaLabel;
    private JLabel DescripcionLable;
    private JScrollPane CalendarioRecScroll;
    private DatePicker datePicker;
    private JPanel panelPrincipal;

    private CalendarioController controller;
    private CalendarioModel model;

    public CalendarioRecursosView() {

        cargarButton.addActionListener(e -> {

            Categoria categoria =
                    (Categoria) Descripcion_cBox.getSelectedItem();

            controller.cargarCalendarioRecursos(
                    datePicker.getDate(),
                    categoria
            );
        });

        imprimirButton.addActionListener(e -> {
            controller.generarPDFRecursos();
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

            case CalendarioModel.RECURSOS:

                CalendarioRectable.setModel(
                        new TableModel(
                                model.getColumnasRecursos(),
                                model.getFilasRecursos()
                        )
                );

                break;
        }

        panelPrincipal.revalidate();
        panelPrincipal.repaint();
    }

    public JPanel getPanel() {
        return panelPrincipal;
    }

    public JTextField getFecha_tField() {
        return Fecha_tField;
    }

    public JButton getFechabutton() {
        return Fechabutton;
    }

    public JComboBox<Categoria> getDescripcion_cBox() {
        return Descripcion_cBox;
    }

    public JButton getCargarButton() {
        return cargarButton;
    }

    public JTable getCalendarioRectable() {
        return CalendarioRectable;
    }

    public JButton getImprimirButton() {
        return imprimirButton;
    }

    public DatePicker getDatePicker() {
        return datePicker;
    }

    public void cargarCategoriasEnCombo(
            List<Categoria> categorias) {

        Descripcion_cBox.removeAllItems();

        for (Categoria categoria : categorias) {
            Descripcion_cBox.addItem(categoria);
        }
    }
}