package reserva.presentation.Reservas;

import javax.swing.*;

public class ReservasView {
    private JTextField Frase_tField;
    private JTextField Actividad_tField;
    private JTextField Fecha_tField;
    private JButton Fechabutton;
    private JComboBox HoraInicio_cBox;
    private JComboBox HoraFin_cBox;
    private JButton extraerButton;
    private JButton reservarButton;
    private JButton cancelarReservaSeleccionadaButton;
    private JButton limpiaButton;
    private JList Categorialist;
    private JButton imprimirButton;
    private JTable MiReservatable;
    private JPanel ReservasPanel;
    private JLabel FraseLabel;
    private JLabel ActividadLabel;
    private JLabel FechaLabel;
    private JLabel CategoriasLabel;
    private JLabel HoraInicioLabel;
    private JLabel HoraFinLabel;
    private JPanel MiReservasPanel;
    private JScrollPane MiReservaScrolll;
    private JPanel CategoriaPanel;
    private JScrollPane CategoriaScroll;

    public JTextField getFraseTField() {
        return Frase_tField;
    }

    public JTextField getActividadTField() {
        return Actividad_tField;
    }

    public JTextField getFechaTField() {
        return Fecha_tField;
    }

    public JButton getFechaButton() {
        return Fechabutton;
    }

    public JComboBox getHoraInicioCBox() {
        return HoraInicio_cBox;
    }

    public JComboBox getHoraFinCBox() {
        return HoraFin_cBox;
    }

    public JButton getExtraerButton() {
        return extraerButton;
    }

    public JButton getReservarButton() {
        return reservarButton;
    }

    public JButton getCancelarReservaSeleccionadaButton() {
        return cancelarReservaSeleccionadaButton;
    }

    public JButton getLimpiaButton() {
        return limpiaButton;
    }

    public JList getCategoriaList() {
        return Categorialist;
    }

    public JButton getImprimirButton() {
        return imprimirButton;
    }

    public JTable getMiReservaTable() {
        return MiReservatable;
    }

    public JPanel getReservasPanel() {
        return ReservasPanel;
    }

    public JPanel getMiReservasPanel() {
        return MiReservasPanel;
    }

    public JPanel getCategoriaPanel() {
        return CategoriaPanel;
    }

    public JScrollPane getMiReservaScroll() {
        return MiReservaScrolll;
    }

    public JScrollPane getCategoriaScroll() {
        return CategoriaScroll;
    }
}
