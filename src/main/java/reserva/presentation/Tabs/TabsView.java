package reserva.presentation.Tabs;

import reserva.logic.Administrador;
import reserva.logic.Usuario;
import reserva.presentation.Calendario.CalendarioActividadesView;
import reserva.presentation.Calendario.CalendarioController;
import reserva.presentation.Calendario.CalendarioRecursosView;
import reserva.presentation.Categoria.CategoriasView;
import reserva.presentation.Estadisticas.EstadisticasController;
import reserva.presentation.Estadisticas.EstadisticasView;
import reserva.presentation.Funcionarios.FuncionariosView;
import reserva.presentation.Recursos.RecursosView;
import reserva.presentation.Reservas.ReservasView;
import reserva.presentation.Iconos;

import javax.swing.*;
import java.awt.*;

public class TabsView extends JFrame {

    // Componentes creados por el .form (IntelliJ GUI Designer)
    private JPanel panel1;
    private JTabbedPane tabsPrincipal;
    private JTabbedPane CalendarioSubTabs;
    private JPanel FuncionariosTab;
    private JPanel CategoriasTab;
    private JPanel RecursosTab;
    private JPanel ReservasTab;
    private JPanel CalendarioTab;
    private JPanel EstadisticasTab;
    private JPanel CalendarioRecTab;
    private JPanel CalendarioActTab;

    /**
     * @param usuarioActual el usuario que acaba de iniciar sesión (viene de LoginView).
     *                       Según sea Administrador o Funcionario se muestran unas
     *                       pestañas u otras, tal como lo pide el enunciado.
     */
    public TabsView(Usuario usuarioActual) {
        setTitle("Sistema de Reserva de Recursos"
                + (usuarioActual != null ? " - " + usuarioActual.getNombre() + " (" + usuarioActual.getRol() + ")" : ""));
        setContentPane(panel1);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 700);
        setLocationRelativeTo(null);

        if (Iconos.get("icon") != null) {
            setIconImage(Iconos.get("icon").getImage());
        }
        ponerIcono(tabsPrincipal, FuncionariosTab, "funcionarios");
        ponerIcono(tabsPrincipal, CategoriasTab, "categorias");
        ponerIcono(tabsPrincipal, RecursosTab, "recursos");
        ponerIcono(tabsPrincipal, ReservasTab, "reservas");
        ponerIcono(tabsPrincipal, CalendarioTab, "calendarizacion");
        ponerIcono(tabsPrincipal, EstadisticasTab, "statistics");
        ponerIcono(CalendarioSubTabs, CalendarioRecTab, "recursos");
        ponerIcono(CalendarioSubTabs, CalendarioActTab, "actividades");

        boolean esAdmin = usuarioActual instanceof Administrador;

        if (esAdmin) {
            // Funcionarios, Categorías y Recursos: solo administrador (funcionalidades 3, 4 y 5)
            FuncionariosTab.setLayout(new BorderLayout());
            FuncionariosTab.add(new FuncionariosView(usuarioActual).getPanel1(), BorderLayout.CENTER);

            CategoriasTab.setLayout(new BorderLayout());
            CategoriasTab.add(new CategoriasView(usuarioActual).getCategoriaPanel(), BorderLayout.CENTER);

            RecursosTab.setLayout(new BorderLayout());
            RecursosTab.add(new RecursosView(usuarioActual).getPanel1(), BorderLayout.CENTER);

            // Reservas: solo funcionario (funcionalidad 2) -> se le quita al administrador
            tabsPrincipal.remove(ReservasTab);
        } else {
            // El funcionario no gestiona Funcionarios/Categorías/Recursos
            tabsPrincipal.remove(FuncionariosTab);
            tabsPrincipal.remove(CategoriasTab);
            tabsPrincipal.remove(RecursosTab);

            ReservasTab.setLayout(new BorderLayout());
            ReservasTab.add(new ReservasView(), BorderLayout.CENTER);
        }

        // Calendarización (funcionalidades 6 y 7) y Estadísticas (8): para ambos roles
        CalendarioRecursosView calRecursosView = new CalendarioRecursosView();
        CalendarioActividadesView calActividadesView = new CalendarioActividadesView();
        new CalendarioController(calRecursosView, calActividadesView, usuarioActual);

        CalendarioRecTab.setLayout(new BorderLayout());
        CalendarioRecTab.add(calRecursosView.getPanel(), BorderLayout.CENTER);

        CalendarioActTab.setLayout(new BorderLayout());
        CalendarioActTab.add(calActividadesView.getPanelPrincipal(), BorderLayout.CENTER);

        EstadisticasView estadisticasView = new EstadisticasView();
        new EstadisticasController(estadisticasView);

        EstadisticasTab.setLayout(new BorderLayout());
        EstadisticasTab.add(estadisticasView.getPanelPrincipal(), BorderLayout.CENTER);
    }

    private void ponerIcono(JTabbedPane tabs, JPanel pestaña, String nombreIcono) {
        int indice = tabs.indexOfComponent(pestaña);
        if (indice >= 0) {
            tabs.setIconAt(indice, Iconos.get(nombreIcono));
        }
    }

}
