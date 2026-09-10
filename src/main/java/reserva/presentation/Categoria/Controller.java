package reserva.presentation.Categoria;

import reserva.data.CategoriaDao;
import reserva.logic.Administrador;
import reserva.logic.Categoria;
import reserva.logic.Usuario;

import javax.swing.*;
import java.awt.Desktop;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;

class CategoriaController {
    private final CategoriasView view;
    private final CategoriaModel model;
    private final TablaModel tableModel;
    private final CategoriaDao dao;
    private final Usuario usuarioActual;

    public CategoriaController(CategoriasView view,  CategoriaModel model,
                               TablaModel tableModel, Usuario usuarioActual) {
        this.view = view;
        this.model = new CategoriaModel();
        this.tableModel = new TablaModel();
        this.dao = new CategoriaDao();
        this.usuarioActual = usuarioActual;
        inicializar();
    }

    public CategoriasView getView() {
        return view;
    }

    public CategoriaModel getModel() {
        return model;
    }

    public TablaModel getTableModel() {
        return tableModel;
    }

    private void inicializar() {
        if (!(usuarioActual instanceof Administrador)) {
            JOptionPane.showMessageDialog(view.getCategoriaPanel(),
                    "Solo un administrador puede acceder a esta pantalla.");
            habilitar(false);
            return;
        }

        view.getCategoriatable().setModel(tableModel);
        cargarListado();

        view.getBuscarButton().addActionListener(e -> buscar());
        view.getGuardarButton().addActionListener(e -> guardar());
        view.getBorrarButton().addActionListener(e -> borrar());
        view.getLimpiarButton().addActionListener(e -> limpiar());
        view.getImprimirButton().addActionListener(e -> generarPDF());

        view.getCategoriatable().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargarFilaSeleccionada();
            }
        });
    }

    private void generarPDF() {
        try {
            List<Categoria> categorias = dao.listar(usuarioActual);

            String nombreArchivo = "Categorias.pdf";

            PdfWriter writer = new PdfWriter(nombreArchivo);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph("LISTA DE CATEGORÍAS"));
            Table tabla = new Table(2);
            tabla.addCell(new Cell().add(new Paragraph("ID")));

            tabla.addCell(new Cell().add(new Paragraph("Descripción")));

            for (Categoria c : categorias) {
                tabla.addCell(new Cell().add(new Paragraph(c.getId())));

                tabla.addCell(new Cell().add(new Paragraph(c.getDescripcion())));
            }

            document.add(tabla);
            document.close();

            JOptionPane.showMessageDialog(view.getCategoriaPanel(), "PDF generado correctamente.");

            Desktop.getDesktop().open(new File(nombreArchivo));

        }
        catch (Exception ex) {
            JOptionPane.showMessageDialog(view.getCategoriaPanel(), "No se pudo generar el PDF: " + ex.getMessage());
        }
    }

    private void cargarFilaSeleccionada() {
        int fila = view.getCategoriatable().getSelectedRow();
        if (fila < 0) return;
        Object id = tableModel.getValueAt(fila, 0);
        Object descripcion = tableModel.getValueAt(fila, 1);
        view.getIdTField().setText(id == null ? "" : id.toString());
        view.getDescripcionTField().setText(descripcion == null ? "" : descripcion.toString());
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
            String texto = view.getDescripcionBusquedaTField().getText().trim();
            if (texto.isEmpty()) {
                cargarListado();
            } else {
                mostrarEnTabla(dao.buscarPorDescripcion(texto, usuarioActual));
            }
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(view.getCategoriaPanel(), ex.getMessage());
        }
    }

    private void guardar() {
        try {
            String id = view.getIdTField().getText().trim();
            String descripcion = view.getDescripcionTField().getText().trim();

            if (descripcion.isEmpty()) {
                JOptionPane.showMessageDialog(view.getCategoriaPanel(), "La descripción es obligatoria.");
                return;
            }

            boolean ok;
            if (id.isEmpty()) {
                Categoria c = new Categoria();
                c.setDescripcion(descripcion);
                ok = dao.guardar(c, usuarioActual);
                if (ok) {
                    JOptionPane.showMessageDialog(view.getCategoriaPanel(),
                            "Categoría creada con id " + c.getId() + ".");
                } else {
                    JOptionPane.showMessageDialog(view.getCategoriaPanel(), "No se pudo crear la categoría.");
                }
            } else {
                Categoria c = new Categoria();
                c.setId(id);
                c.setDescripcion(descripcion);
                ok = dao.actualizar(c, usuarioActual);
                if (ok) {
                    JOptionPane.showMessageDialog(view.getCategoriaPanel(), "Categoría modificada.");
                } else {
                    JOptionPane.showMessageDialog(view.getCategoriaPanel(), "No se pudo modificar la categoría.");
                }
            }

            if (ok) {
                limpiar();
                cargarListado();
            }
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(view.getCategoriaPanel(), ex.getMessage());
        }
    }

    private void borrar() {
        try {
            String id = view.getIdTField().getText().trim();
            if (id.isEmpty()) {
                JOptionPane.showMessageDialog(view.getCategoriaPanel(), "Busque o seleccione una categoría para borrar.");
                return;
            }
            int confirmar = JOptionPane.showConfirmDialog(view.getCategoriaPanel(),
                    "¿Eliminar la categoría " + id + "?");
            if (confirmar == JOptionPane.YES_OPTION) {
                if (dao.eliminar(id, usuarioActual)) {
                    limpiar();
                    cargarListado();
                } else {
                    JOptionPane.showMessageDialog(view.getCategoriaPanel(), "No se pudo eliminar la categoría.");
                }
            }
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(view.getCategoriaPanel(), ex.getMessage());
        }
    }

    private void limpiar() {
        view.getDescripcionBusquedaTField().setText("");
        view.getIdTField().setText("");
        view.getDescripcionTField().setText("");
    }

    private void cargarListado() {
        try {
            mostrarEnTabla(dao.listar(usuarioActual));
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(view.getCategoriaPanel(), ex.getMessage());
        }
    }

    private void mostrarEnTabla(List<Categoria> lista) {
        List<Object[]> filas = new ArrayList<>();
        for (Categoria c : lista) {
            filas.add(new Object[]{c.getId(), c.getDescripcion()});
        }
        tableModel.setFilas(filas);
    }
}
