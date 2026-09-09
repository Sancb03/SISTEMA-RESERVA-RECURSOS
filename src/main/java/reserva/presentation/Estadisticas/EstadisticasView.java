package reserva.presentation.Estadisticas;

import com.github.lgooddatepicker.components.DatePicker;

import javax.swing.*;

public class EstadisticasView {
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
    private JPanel panelPrincipal;

    public JPanel getPanelPrincipal(){ return panelPrincipal; }

    public DatePicker getDPActividadesInicio(){ return DPActividadesInicio; }

    public DatePicker getDPActividadesFin(){ return DPActividadesFin; }

    public DatePicker getDPRecursosInicio(){ return DPRecursosInicio; }

    public DatePicker getDPRecursosFin(){ return DPRecursosFin; }

    public JButton getCargarRecButton(){ return cargarRecButton; }

    public JButton getCargarActButton(){ return CargarActButton; }

    public JTable getEstadisticasRectable(){ return EstadisticasRectable; }

    public JTable getEstadisticasActtable(){ return EstadisticasActtable; }

    public JPanel getGraficoRecPanel(){ return GraficoRecPanel; }

    public JPanel getGraficoActPanel(){ return GraficoActPanel; }
}
