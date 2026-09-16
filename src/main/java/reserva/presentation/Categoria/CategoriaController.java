package reserva.presentation.Categoria;

import reserva.GeneradorPDF;
import reserva.data.Data;
import reserva.logic.Administrador;
import reserva.logic.Categoria;
import reserva.logic.Usuario;
import reserva.presentation.Iconos;

import javax.swing.*;
import java.util.ArrayList;

public class CategoriaController {

    private final CategoriasView view;
    private final CategoriaModel model;
    private final Data data;
    private final Usuario usuarioActual;

    public CategoriaController(
            CategoriasView view,
            CategoriaModel model,
            Usuario usuarioActual) {

        this.view = view;
        this.model = model;
        this.data = Data.instance();
        this.usuarioActual = usuarioActual;

        view.setController(this);
        view.setModel(model);

        inicializar();
    }

    private void inicializar() {

        if (!(usuarioActual instanceof Administrador)) {

            JOptionPane.showMessageDialog(
                    view.getCategoriaPanel(),
                    "Solo un administrador puede acceder a esta pantalla."
            );

            habilitar(false);
            return;
        }

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

        cargarListado();
    }

    public void buscar() {

        try {

            String texto =
                    view.getDescripcionBusquedaTField()
                            .getText()
                            .trim();

            if (texto.isEmpty()) {

                cargarListado();

            } else {

                model.setListado(
                        data.buscarCategoriasPorDescripcion(
                                texto,
                                usuarioActual
                        )
                );
            }

        } catch (RuntimeException ex) {

            JOptionPane.showMessageDialog(
                    view.getCategoriaPanel(),
                    ex.getMessage()
            );
        }
    }

    public void guardar(Categoria c) {

        try {

            if (c.getDescripcion() == null ||
                    c.getDescripcion().isBlank()) {

                JOptionPane.showMessageDialog(
                        view.getCategoriaPanel(),
                        "La descripción es obligatoria."
                );

                return;
            }

            boolean ok;

            if (c.getId() == null ||
                    c.getId().isBlank()) {

                ok = data.guardarCategoria(
                        c,
                        usuarioActual
                );

                if (ok) {

                    JOptionPane.showMessageDialog(
                            view.getCategoriaPanel(),
                            "Categoría creada con id " +
                                    c.getId() + "."
                    );

                } else {

                    JOptionPane.showMessageDialog(
                            view.getCategoriaPanel(),
                            "No se pudo crear la categoría."
                    );
                }

            } else {

                ok = data.actualizarCategoria(
                        c,
                        usuarioActual
                );

                if (ok) {

                    JOptionPane.showMessageDialog(
                            view.getCategoriaPanel(),
                            "Categoría modificada."
                    );

                } else {

                    JOptionPane.showMessageDialog(
                            view.getCategoriaPanel(),
                            "No se encontró una categoría con ese id para modificar."
                    );
                }
            }

            if (ok) {
                limpiar();
                cargarListado();
            }

        } catch (RuntimeException ex) {

            JOptionPane.showMessageDialog(
                    view.getCategoriaPanel(),
                    ex.getMessage()
            );
        }
    }

    public void borrar() {

        try {

            Categoria actual =
                    model.getCurrent();

            if (actual == null ||
                    actual.getId() == null ||
                    actual.getId().isBlank()) {

                JOptionPane.showMessageDialog(
                        view.getCategoriaPanel(),
                        "Busque o seleccione una categoría para borrar."
                );

                return;
            }

            int confirmar =
                    JOptionPane.showConfirmDialog(
                            view.getCategoriaPanel(),
                            "¿Eliminar la categoría " +
                                    actual.getId() + "?"
                    );

            if (confirmar == JOptionPane.YES_OPTION) {

                if (data.eliminarCategoria(
                        actual.getId(),
                        usuarioActual)) {

                    limpiar();
                    cargarListado();

                } else {

                    JOptionPane.showMessageDialog(
                            view.getCategoriaPanel(),
                            "No se pudo eliminar la categoría."
                    );
                }
            }

        } catch (RuntimeException ex) {

            JOptionPane.showMessageDialog(
                    view.getCategoriaPanel(),
                    ex.getMessage()
            );
        }
    }

    public void seleccionarCategoria(int fila) {

        if (fila >= 0 &&
                fila < model.getListado().size()) {

            model.setCurrent(
                    model.getListado().get(fila)
            );
        }
    }

    public void limpiar() {

        view.getDescripcionBusquedaTField()
                .setText("");

        model.setCurrent(
                new Categoria()
        );
    }

    private void cargarListado() {

        try {

            model.setListado(
                    data.listarCategorias(
                            usuarioActual
                    )
            );

        } catch (RuntimeException ex) {

            JOptionPane.showMessageDialog(
                    view.getCategoriaPanel(),
                    ex.getMessage()
            );
        }
    }

    public void generarPDF() {

        GeneradorPDF.generarDesdeTabla(
                view.getCategoriatable(),
                "Lista de Categorias",
                "Categorias.pdf"
        );
    }

    private void habilitar(boolean habilitado) {

        view.getBuscarButton().setEnabled(habilitado);
        view.getGuardarButton().setEnabled(habilitado);
        view.getBorrarButton().setEnabled(habilitado);
        view.getLimpiarButton().setEnabled(habilitado);
        view.getImprimirButton().setEnabled(habilitado);
    }

    public CategoriasView getView() {
        return view;
    }

    public CategoriaModel getModel() {
        return model;
    }
}