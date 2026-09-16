package reserva.presentation.Estadisticas;

import com.github.lgooddatepicker.components.DatePicker;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class EstadisticasView implements PropertyChangeListener {

    private JTable EstadisticasRectable;
    private JTable EstadisticasActtable;
    private JPanel RecursosPanel;
    private JPanel FechasRecPanel;
    private JButton cargarRecButton;
    private JPanel EstadisticasRecPanel;
    private JPanel GraficoRecPanel;
    private JScrollPane EstadisticasRecScroll;
    private JPanel ActividadesPanel;
    private JPanel EstadisticasActPanel;
    private JPanel GraficoActPanel;
    private JPanel FechasActPanel;
    private JButton CargarActButton;
    private JScrollPane EstadisticasActScroll;
    private DatePicker DPRecursosInicio;
    private DatePicker DPRecursosFin;
    private DatePicker DPActividadesInicio;
    private DatePicker DPActividadesFin;
    private JPanel Estadisticas_panel;

    private EstadisticasController controller;
    private EstadisticasModel model;

    public EstadisticasView() {

        cargarRecButton.addActionListener(e -> {

            controller.cargarEstadisticasRecursos(
                    DPRecursosInicio.getDate(),
                    DPRecursosFin.getDate()
            );
        });

        CargarActButton.addActionListener(e -> {

            controller.cargarEstadisticasActividades(
                    DPActividadesInicio.getDate(),
                    DPActividadesFin.getDate()
            );
        });
    }

    public void setController(EstadisticasController controller) {
        this.controller = controller;
    }

    public void setModel(EstadisticasModel model) {
        this.model = model;
        model.addPropertyChangeListener(this);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {

        switch (evt.getPropertyName()) {

            case EstadisticasModel.RECURSOS:

                EstadisticasRectable.setModel(
                        new TablaModel(
                                new String[]{"Categoria", "Cantidad"},
                                model.getRecursos()
                        )
                );

                break;

            case EstadisticasModel.ACTIVIDADES:

                EstadisticasActtable.setModel(
                        new TablaModel(
                                new String[]{"Semana", "Cantidad"},
                                model.getActividades()
                        )
                );

                break;
        }

        Estadisticas_panel.revalidate();
        Estadisticas_panel.repaint();
    }

    public JPanel getPanelPrincipal() {
        return Estadisticas_panel;
    }

    public DatePicker getDPActividadesInicio() {
        return DPActividadesInicio;
    }

    public DatePicker getDPActividadesFin() {
        return DPActividadesFin;
    }

    public DatePicker getDPRecursosInicio() {
        return DPRecursosInicio;
    }

    public DatePicker getDPRecursosFin() {
        return DPRecursosFin;
    }

    public JButton getCargarRecButton() {
        return cargarRecButton;
    }

    public JButton getCargarActButton() {
        return CargarActButton;
    }

    public JTable getEstadisticasRectable() {
        return EstadisticasRectable;
    }

    public JTable getEstadisticasActtable() {
        return EstadisticasActtable;
    }

    public JPanel getGraficoRecPanel() {
        return GraficoRecPanel;
    }

    public JPanel getGraficoActPanel() {
        return GraficoActPanel;
    }
}