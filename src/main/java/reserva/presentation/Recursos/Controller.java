package reserva.presentation.Recursos;

import reserva.GeneradorPDF;
import reserva.data.Data;
import reserva.logic.Administrador;
import reserva.logic.Categoria;
import reserva.logic.Recurso;
import reserva.logic.Usuario;
import reserva.presentation.Iconos;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

class RecursosController {
    private final RecursosView view;
    private final RecursosModel model;
    private final TablaModel tableModel;
    private final Usuario usuarioActual;

    private List<Categoria> categorias = new ArrayList<>();

    public RecursosController(RecursosView view, RecursosModel model, TablaModel tableModel, Usuario usuarioActual) {
        this.view = view;
        this.model = new RecursosModel();
        this.tableModel = new TablaModel();
        this.usuarioActual = usuarioActual;
        inicializar();
    }

    public RecursosView getView() {
        return view;
    }

    public RecursosModel getModel() {
        return model;
    }

    public TablaModel getTableModel() {
        return tableModel;
    }

    private void inicializar() {
        if (!(usuarioActual instanceof Administrador)) {
            JOptionPane.showMessageDialog(view.getPanel1(),
                    "Solo un administrador puede acceder a esta pantalla.");
            habilitar(false);
            return;
        }

        view.getListadotable().setModel(tableModel);
        cargarCategorias();
        cargarListado();

        view.getBuscarButton().setIcon(Iconos.get("search"));
        view.getGuardarButton().setIcon(Iconos.get("save"));
        view.getBorrarButton().setIcon(Iconos.get("delete"));
        view.getLimpiarButton().setIcon(Iconos.get("clear"));
        view.getImprimirButton().setIcon(Iconos.get("pdf"));

        view.getBuscarButton().addActionListener(e -> buscar());
        view.getGuardarButton().addActionListener(e -> guardar());
        view.getBorrarButton().addActionListener(e -> borrar());
        view.getLimpiarButton().addActionListener(e -> limpiar());
        view.getImprimirButton().addActionListener(e -> generarPDF());

        view.getListadotable().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargarFilaSeleccionada();
            }
        });
    }

    private void cargarCategorias() {
        try {
            categorias = Data.instance().listarCategorias(usuarioActual);
        } catch (RuntimeException ex) {
            categorias = new ArrayList<>();
            JOptionPane.showMessageDialog(view.getPanel1(), ex.getMessage());
        }
        view.cargarCategoriasEnCombos(categorias);
    }

    private Categoria buscarCategoriaPorId(String id) {
        if (id == null) {
            return null;
        }
        for (Categoria c : categorias) {
            if (c.getId().equals(id)) {
                return c;
            }
        }
        return null;
    }

    private void generarPDF() {
        GeneradorPDF.generarDesdeTabla(view.getListadotable(), "Lista de Recursos", "Recursos.pdf");
    }

    private void cargarFilaSeleccionada() {
        int fila = view.getListadotable().getSelectedRow();
        if (fila < 0) return;
        Object id = tableModel.getValueAt(fila, 0);
        view.getIdRecTField().setText(id == null ? "" : id.toString());
        view.getIdRecTField().setEnabled(false);

        Recurso recurso = Data.instance().buscarRecursoPorId(String.valueOf(id), usuarioActual);
        if (recurso != null) {
            view.seleccionarCategoriaRec(recurso.getCategoria());
            view.getDescripcionRecTField().setText(recurso.getDescripcion());
        }
    }

    private void habilitar(boolean habilitado) {
        view.getBuscarButton().setEnabled(habilitado);
        view.getGuardarButton().setEnabled(habilitado);
        view.getBorrarButton().setEnabled(habilitado);
        view.getLimpiarButton().setEnabled(habilitado);
        view.getImprimirButton().setEnabled(habilitado);
    }

    private void buscar() {
        try {
            Categoria filtro = view.getCategoriaSeleccionadaBusqueda();
            String texto = view.getDescripcionTField().getText().trim().toLowerCase();

            List<Recurso> resultado = new ArrayList<>();
            for (Recurso r : Data.instance().listarRecursos(usuarioActual)) {
                boolean coincideCategoria = (filtro == null)
                        || (r.getCategoria() != null && r.getCategoria().getId().equals(filtro.getId()));
                boolean coincideTexto = texto.isEmpty()
                        || (r.getDescripcion() != null && r.getDescripcion().toLowerCase().contains(texto));
                if (coincideCategoria && coincideTexto) {
                    resultado.add(r);
                }
            }
            mostrarEnTabla(resultado);
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(view.getPanel1(), ex.getMessage());
        }
    }

    private void guardar() {
        try {
            String id = view.getIdRecTField().getText().trim();
            Categoria categoria = view.getCategoriaSeleccionadaRec();
            String descripcion = view.getDescripcionRecTField().getText().trim();

            if (id.isEmpty() || categoria == null || descripcion.isEmpty()) {
                JOptionPane.showMessageDialog(view.getPanel1(),
                        "El id, la categoría y la descripción son obligatorios.");
                return;
            }

            Recurso existente = Data.instance().buscarRecursoPorId(id, usuarioActual);

            Recurso r = new Recurso();
            r.setId(id);
            r.setCategoria(categoria);
            r.setDescripcion(descripcion);

            boolean ok;
            if (existente == null) {
                ok = Data.instance().guardarRecurso(r, usuarioActual);
                if (ok) {
                    JOptionPane.showMessageDialog(view.getPanel1(), "Recurso creado.");
                } else {
                    JOptionPane.showMessageDialog(view.getPanel1(),
                            "No se pudo crear el recurso (¿el id ya existe?).");
                }
            } else {
                ok = Data.instance().actualizarRecurso(r, usuarioActual);
                if (ok) {
                    JOptionPane.showMessageDialog(view.getPanel1(), "Recurso modificado.");
                } else {
                    JOptionPane.showMessageDialog(view.getPanel1(), "No se pudo modificar el recurso.");
                }
            }

            if (ok) {
                limpiar();
                cargarListado();
            }
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(view.getPanel1(), ex.getMessage());
        }
    }

    private void borrar() {
        try {
            String id = view.getIdRecTField().getText().trim();
            if (id.isEmpty()) {
                JOptionPane.showMessageDialog(view.getPanel1(), "Busque o seleccione un recurso para borrar.");
                return;
            }
            int confirmar = JOptionPane.showConfirmDialog(view.getPanel1(), "¿Eliminar el recurso " + id + "?");
            if (confirmar == JOptionPane.YES_OPTION) {
                if (Data.instance().eliminarRecurso(id, usuarioActual)) {
                    limpiar();
                    cargarListado();
                } else {
                    JOptionPane.showMessageDialog(view.getPanel1(), "No se pudo eliminar el recurso.");
                }
            }
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(view.getPanel1(), ex.getMessage());
        }
    }

    private void limpiar() {
        view.getDescripcionTField().setText("");
        view.getIdRecTField().setText("");
        view.getIdRecTField().setEnabled(true);
        view.getDescripcionRecTField().setText("");
        view.limpiarCombos();
    }

    private void cargarListado() {
        try {
            mostrarEnTabla(Data.instance().listarRecursos(usuarioActual));
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(view.getPanel1(), ex.getMessage());
        }
    }

    private void mostrarEnTabla(List<Recurso> lista) {
        List<Object[]> filas = new ArrayList<>();
        for (Recurso r : lista) {
            filas.add(new Object[]{
                    r.getId(),
                    r.getCategoria() != null ? r.getCategoria().getDescripcion() : "",
                    r.getDescripcion()
            });
        }
        tableModel.setFilas(filas);
    }
}
