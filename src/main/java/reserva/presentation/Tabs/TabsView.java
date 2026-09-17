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


    private JPanel Tabs_panel;
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

    public TabsView(Usuario usuarioActual) {
        setTitle("Sistema de Reserva de Recursos"
                + (usuarioActual != null ? " - " + usuarioActual.getNombre() + " (" + usuarioActual.getRol() + ")" : ""));
        setContentPane(Tabs_panel);
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
        RecursosView recursosView = null;

        if (esAdmin) {

            FuncionariosTab.setLayout(new BorderLayout());
            FuncionariosTab.add(new FuncionariosView(usuarioActual).getPanel1(), BorderLayout.CENTER);

            CategoriasTab.setLayout(new BorderLayout());
            CategoriasTab.add(new CategoriasView(usuarioActual).getPanelPrincipal(), BorderLayout.CENTER);

            recursosView = new RecursosView(usuarioActual);
            RecursosTab.setLayout(new BorderLayout());
            RecursosTab.add(recursosView.getPanel1(), BorderLayout.CENTER);

            tabsPrincipal.remove(ReservasTab);
        } else {

            tabsPrincipal.remove(FuncionariosTab);
            tabsPrincipal.remove(CategoriasTab);
            tabsPrincipal.remove(RecursosTab);

            ReservasTab.setLayout(new BorderLayout());
            ReservasTab.add(new ReservasView(), BorderLayout.CENTER);
        }


        CalendarioRecursosView calRecursosView = new CalendarioRecursosView();
        CalendarioActividadesView calActividadesView = new CalendarioActividadesView();
        CalendarioController calendarioController =
                new CalendarioController(calRecursosView, calActividadesView, usuarioActual);

        CalendarioRecTab.setLayout(new BorderLayout());
        CalendarioRecTab.add(calRecursosView.getPanel(), BorderLayout.CENTER);

        CalendarioActTab.setLayout(new BorderLayout());
        CalendarioActTab.add(calActividadesView.getPanelPrincipal(), BorderLayout.CENTER);

        EstadisticasView estadisticasView = new EstadisticasView();
        new EstadisticasController(estadisticasView);

        EstadisticasTab.setLayout(new BorderLayout());
        EstadisticasTab.add(estadisticasView.getPanelPrincipal(), BorderLayout.CENTER);

        RecursosView recursosViewFinal = recursosView;
        tabsPrincipal.addChangeListener(e -> {
            Component seleccionada = tabsPrincipal.getSelectedComponent();

            if (seleccionada == RecursosTab && recursosViewFinal != null) {
                recursosViewFinal.getController().cargarCategorias();
            } else if (seleccionada == CalendarioTab) {
                calendarioController.cargarCategoriasRecursos();
            }
        });

    }

    private void ponerIcono(JTabbedPane tabs, JPanel pestaña, String nombreIcono) {
        int indice = tabs.indexOfComponent(pestaña);
        if (indice >= 0) {
            tabs.setIconAt(indice, Iconos.get(nombreIcono));
        }
    }

}
