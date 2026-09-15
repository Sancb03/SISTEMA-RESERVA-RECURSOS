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

public class RecursosController {

    private final RecursosView view;
    private final RecursosModel model;
    private final Data data;
    private final Usuario usuarioActual;

    private List<Categoria> categorias;

    public RecursosController(
            RecursosView view,
            RecursosModel model,
            Usuario usuarioActual) {

        this.view = view;
        this.model = model;
        this.usuarioActual = usuarioActual;
        this.data = Data.instance();

        this.categorias = new ArrayList<>();

        view.setController(this);
        view.setModel(model);

        inicializar();
    }

    private void inicializar() {

        if (!(usuarioActual instanceof Administrador)) {

            JOptionPane.showMessageDialog(
                    view.getPanel1(),
                    "Solo un administrador puede acceder a esta pantalla."
            );

            habilitar(false);
            return;
        }

        configurarIconos();

        cargarCategorias();
        cargarListado();
    }

    private void configurarIconos() {

        view.getBuscarButton().setIcon(
                Iconos.get("search")
        );

        view.getGuardarButton().setIcon(
                Iconos.get("save")
        );

        view.getBorrarButton().setIcon(
                Iconos.get("delete")
        );

        view.getLimpiarButton().setIcon(
                Iconos.get("clear")
        );

        view.getImprimirButton().setIcon(
                Iconos.get("pdf")
        );
    }

    public void cargarCategorias() {

        try {

            categorias =
                    data.listarCategorias(
                            usuarioActual
                    );

            view.cargarCategoriasEnCombos(
                    categorias
            );

        } catch (RuntimeException ex) {

            categorias = new ArrayList<>();

            JOptionPane.showMessageDialog(
                    view.getPanel1(),
                    ex.getMessage()
            );
        }
    }

    public void buscar() {

        try {

            Categoria filtro =
                    view.getCategoriaSeleccionadaBusqueda();

            String texto =
                    view.getDescripcionTField()
                            .getText()
                            .trim()
                            .toLowerCase();

            List<Recurso> resultado =
                    new ArrayList<>();

            for (Recurso r :
                    data.listarRecursos(usuarioActual)) {

                boolean coincideCategoria =
                        filtro == null
                                ||
                                (
                                        r.getCategoria() != null
                                                &&
                                                r.getCategoria()
                                                        .getId()
                                                        .equals(
                                                                filtro.getId()
                                                        )
                                );

                boolean coincideTexto =
                        texto.isEmpty()
                                ||
                                (
                                        r.getDescripcion() != null
                                                &&
                                                r.getDescripcion()
                                                        .toLowerCase()
                                                        .contains(texto)
                                );

                if (coincideCategoria &&
                        coincideTexto) {

                    resultado.add(r);
                }
            }

            model.setListado(resultado);

        } catch (RuntimeException ex) {

            JOptionPane.showMessageDialog(
                    view.getPanel1(),
                    ex.getMessage()
            );
        }
    }

    public void guardar(Recurso r) {

        try {

            if (r.getId() == null ||
                    r.getId().isBlank() ||
                    r.getCategoria() == null ||
                    r.getDescripcion() == null ||
                    r.getDescripcion().isBlank()) {

                JOptionPane.showMessageDialog(
                        view.getPanel1(),
                        "El id, la categoría y la descripción son obligatorios."
                );

                return;
            }

            Recurso existente =
                    data.buscarRecursoPorId(
                            r.getId(),
                            usuarioActual
                    );

            boolean ok;

            if (existente == null) {

                ok = data.guardarRecurso(
                        r,
                        usuarioActual
                );

                if (ok) {

                    JOptionPane.showMessageDialog(
                            view.getPanel1(),
                            "Recurso creado."
                    );

                } else {

                    JOptionPane.showMessageDialog(
                            view.getPanel1(),
                            "No se pudo crear el recurso."
                    );
                }

            } else {

                ok = data.actualizarRecurso(
                        r,
                        usuarioActual
                );

                if (ok) {

                    JOptionPane.showMessageDialog(
                            view.getPanel1(),
                            "Recurso modificado."
                    );

                } else {

                    JOptionPane.showMessageDialog(
                            view.getPanel1(),
                            "No se pudo modificar el recurso."
                    );
                }
            }

            if (ok) {

                limpiar();
                cargarListado();
            }

        } catch (RuntimeException ex) {

            JOptionPane.showMessageDialog(
                    view.getPanel1(),
                    ex.getMessage()
            );
        }
    }

    public void borrar() {

        try {

            Recurso actual =
                    model.getCurrent();

            if (actual == null ||
                    actual.getId() == null ||
                    actual.getId().isBlank()) {

                JOptionPane.showMessageDialog(
                        view.getPanel1(),
                        "Busque o seleccione un recurso para borrar."
                );

                return;
            }

            int confirmar =
                    JOptionPane.showConfirmDialog(
                            view.getPanel1(),
                            "¿Eliminar el recurso " +
                                    actual.getId() +
                                    "?"
                    );

            if (confirmar ==
                    JOptionPane.YES_OPTION) {

                if (data.eliminarRecurso(
                        actual.getId(),
                        usuarioActual)) {

                    limpiar();
                    cargarListado();

                } else {

                    JOptionPane.showMessageDialog(
                            view.getPanel1(),
                            "No se pudo eliminar el recurso."
                    );
                }
            }

        } catch (RuntimeException ex) {

            JOptionPane.showMessageDialog(
                    view.getPanel1(),
                    ex.getMessage()
            );
        }
    }

    public void seleccionarRecurso(int fila) {

        if (fila >= 0 &&
                fila < model.getListado().size()) {

            Recurso seleccionado =
                    model.getListado().get(fila);

            model.setCurrent(seleccionado);
        }
    }

    public void limpiar() {

        view.getDescripcionTField()
                .setText("");

        view.limpiarCombos();

        model.setCurrent(
                new Recurso()
        );
    }

    private void cargarListado() {

        try {

            model.setListado(
                    data.listarRecursos(
                            usuarioActual
                    )
            );

        } catch (RuntimeException ex) {

            JOptionPane.showMessageDialog(
                    view.getPanel1(),
                    ex.getMessage()
            );
        }
    }

    public void generarPDF() {

        GeneradorPDF.generarDesdeTabla(
                view.getListadotable(),
                "Lista de Recursos",
                "Recursos.pdf"
        );
    }

    private void habilitar(boolean habilitado) {

        view.getBuscarButton()
                .setEnabled(habilitado);

        view.getGuardarButton()
                .setEnabled(habilitado);

        view.getBorrarButton()
                .setEnabled(habilitado);

        view.getLimpiarButton()
                .setEnabled(habilitado);

        view.getImprimirButton()
                .setEnabled(habilitado);
    }

    public RecursosView getView() {
        return view;
    }

    public RecursosModel getModel() {
        return model;
    }
}