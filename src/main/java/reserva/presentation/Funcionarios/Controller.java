package reserva.presentation.Funcionarios;

import reserva.data.FuncionarioDao;
import reserva.logic.Administrador;
import reserva.logic.Funcionario;
import reserva.logic.Usuario;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;

import java.awt.Desktop;
import java.io.File;

class FuncionariosController {
    private final FuncionariosView view;
    private final FuncionariosModel model;
    private final TablaModel tableModel;
    private final FuncionarioDao dao;
    private final Usuario usuarioActual;

    public FuncionariosController(FuncionariosView view,FuncionariosModel model, TablaModel tableModel, Usuario usuarioActual)
    {
        this.view = view;
        this.model = model;
        this.tableModel = tableModel;
        this.dao = new FuncionarioDao();
        this.usuarioActual = usuarioActual;

        inicializar();
    }

    public FuncionariosView getView() {
        return view;
    }

    public FuncionariosModel getModel() {
        return model;
    }

    public TablaModel getTableModel() {
        return tableModel;
    }

    private void inicializar() {

        view.getListadotable().setModel(tableModel);

        cargarListado();

        view.getBuscarButton().addActionListener(e -> buscar());
        view.getGuardarButton().addActionListener(e -> guardar());
        view.getBorrarButton().addActionListener(e -> borrar());
        view.getLimpiarButton().addActionListener(e -> limpiar());
        view.getImprimirButton().addActionListener(e -> generarPDF());
    }

    private void generarPDF() {

        try {

            List<Funcionario> funcionarios = dao.listar(usuarioActual);

            String nombreArchivo = "Funcionarios.pdf";

            PdfWriter writer = new PdfWriter(nombreArchivo);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph("LISTA DE FUNCIONARIOS"));

            Table tabla = new Table(3);

            tabla.addCell(new Cell().add(new Paragraph("ID")));
            tabla.addCell(new Cell().add(new Paragraph("Nombre")));
            tabla.addCell(new Cell().add(new Paragraph("Teléfono")));

            for (Funcionario f : funcionarios) {

                tabla.addCell(new Cell().add(
                        new Paragraph(String.valueOf(f.getId()))
                ));

                tabla.addCell(new Cell().add(
                        new Paragraph(f.getNombre())
                ));

                tabla.addCell(new Cell().add(
                        new Paragraph(f.getTelefono())
                ));
            }

            document.add(tabla);

            document.close();

            JOptionPane.showMessageDialog(
                    view.getPanel1(),
                    "PDF generado correctamente."
            );

            Desktop.getDesktop().open(new File(nombreArchivo));

        } catch (Exception ex) {

            JOptionPane.showMessageDialog(
                    view.getPanel1(),
                    "No se pudo generar el PDF: " + ex.getMessage()
            );
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
            String idTexto = view.getIdTField().getText();
            String nombreTexto = view.getNombreTField().getText();

            if (!idTexto.isEmpty()) {
                Funcionario f = dao.buscarPorId(Integer.parseInt(idTexto), usuarioActual);
                if (f != null) {
                    mostrarEnFormulario(f);
                } else {
                    JOptionPane.showMessageDialog(view.getPanel1(), "No existe un funcionario con ese id.");
                    tableModel.setFilas(new ArrayList<>());
                }
            } else if (!nombreTexto.isEmpty()) {
                mostrarEnTabla(dao.buscarPorNombre(nombreTexto, usuarioActual));
            } else {
                cargarListado();
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(view.getPanel1(), "El id debe ser numérico.");
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(view.getPanel1(), ex.getMessage());
        }
    }

    private void guardar() {
        try {
            String idTexto = view.getIdFunTField().getText().trim();
            String nombre = view.getNombreFunTField().getText().trim();
            String telefono = view.getTelefonoFunTField().getText().trim();

            if (idTexto.isEmpty() || nombre.isEmpty()) {
                JOptionPane.showMessageDialog(view.getPanel1(), "El id y el nombre son obligatorios.");
                return;
            }

            int id = Integer.parseInt(idTexto);

            model.setId(id);
            model.setNombre(nombre);
            model.setTelefono(telefono);


            Funcionario existente = dao.buscarPorId(id, usuarioActual);

            Funcionario f = new Funcionario();
            f.setId(model.getId());
            f.setNombre(model.getNombre());
            f.setTelefono(model.getTelefono());

            boolean ok;

            if (existente == null) {
                ok = dao.guardar(f, usuarioActual);
                if (ok) {
                    JOptionPane.showMessageDialog(view.getPanel1(),
                            "Funcionario creado. Su clave inicial es igual al id (" + id + ").");
                } else {
                    JOptionPane.showMessageDialog(view.getPanel1(),
                            "No se pudo crear el funcionario (¿el id ya pertenece a otro usuario?).");
                }
            } else {
                ok = dao.actualizar(f, usuarioActual);
                if (ok) {
                    JOptionPane.showMessageDialog(view.getPanel1(), "Funcionario modificado.");
                } else {
                    JOptionPane.showMessageDialog(view.getPanel1(), "No se pudo modificar el funcionario.");
                }
            }

            if (ok) {
                limpiar();
                cargarListado();
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(view.getPanel1(), "El id debe ser numérico.");
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(view.getPanel1(), ex.getMessage());
        }
    }

    private void borrar() {
        try {
            String idTexto = view.getIdFunTField().getText().trim();
            if (idTexto.isEmpty()) {
                JOptionPane.showMessageDialog(view.getPanel1(), "Ingrese o busque el id del funcionario a borrar.");
                return;
            }
            int id = Integer.parseInt(idTexto);
            int confirmar = JOptionPane.showConfirmDialog(view.getPanel1(),
                    "¿Eliminar al funcionario " + id + "?");
            if (confirmar == JOptionPane.YES_OPTION) {
                if (dao.eliminar(id, usuarioActual)) {
                    limpiar();
                    cargarListado();
                } else {
                    JOptionPane.showMessageDialog(view.getPanel1(), "No se pudo eliminar el funcionario.");
                }
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(view.getPanel1(), "El id debe ser numérico.");
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(view.getPanel1(), ex.getMessage());
        }
    }

    private void limpiar() {
        view.getIdTField().setText("");
        view.getNombreTField().setText("");
        view.getIdFunTField().setText("");
        view.getNombreFunTField().setText("");
        view.getTelefonoFunTField().setText("");
    }

    private void cargarListado() {
        try {
            mostrarEnTabla(dao.listar(usuarioActual));
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(view.getPanel1(), ex.getMessage());
        }
    }

    private void mostrarEnFormulario(Funcionario f) {
        view.getIdFunTField().setText(String.valueOf(f.getId()));
        view.getNombreFunTField().setText(f.getNombre());
        view.getTelefonoFunTField().setText(f.getTelefono());
        List<Funcionario> unaFila = new ArrayList<>();
        unaFila.add(f);
        mostrarEnTabla(unaFila);
    }

    private void mostrarEnTabla(List<Funcionario> lista) {
        List<Object[]> filas = new ArrayList<>();
        for (Funcionario f : lista) {
            filas.add(new Object[]{f.getId(), f.getNombre(), f.getTelefono()});
        }
        tableModel.setFilas(filas);
    }
}
