package reserva.presentation.Funcionarios;

import reserva.GeneradorPDF;
import reserva.presentation.Iconos;

import reserva.data.Data;
import reserva.logic.Administrador;
import reserva.logic.Funcionario;
import reserva.logic.Usuario;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

import java.awt.Desktop;
import java.io.File;

class FuncionariosController {
    private final FuncionariosView view;
    private final FuncionariosModel model;
    private final TablaModel tableModel;
    private final Data data;
    private final Usuario usuarioActual;

    public FuncionariosController(FuncionariosView view,FuncionariosModel model, TablaModel tableModel, Usuario usuarioActual)
    {
        this.view = view;
        this.model = model;
        this.tableModel = tableModel;
        this.data = Data.instance();
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
    }

    private void generarPDF() {
        GeneradorPDF.generarDesdeTabla(view.getListadotable(), "Lista de Funcionarios", "Funcionarios.pdf");
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
                Funcionario f = data.buscarFuncionarioPorId(Integer.parseInt(idTexto), usuarioActual);
                if (f != null) {
                    mostrarEnFormulario(f);
                } else {
                    JOptionPane.showMessageDialog(view.getPanel1(), "No existe un funcionario con ese id.");
                    tableModel.setFilas(new ArrayList<>());
                }
            } else if (!nombreTexto.isEmpty()) {
                mostrarEnTabla(data.buscarFuncionariosPorNombre(nombreTexto, usuarioActual));
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


            Funcionario existente = data.buscarFuncionarioPorId(id, usuarioActual);

            Funcionario f = new Funcionario();
            f.setId(model.getId());
            f.setNombre(model.getNombre());
            f.setTelefono(model.getTelefono());

            boolean ok;

            if (existente == null) {
                ok = data.guardarFuncionario(f, usuarioActual);
                if (ok) {
                    JOptionPane.showMessageDialog(view.getPanel1(),
                            "Funcionario creado. Su clave inicial es igual al id (" + id + ").");
                } else {
                    JOptionPane.showMessageDialog(view.getPanel1(),
                            "No se pudo crear el funcionario (¿el id ya pertenece a otro usuario?).");
                }
            } else {
                ok = data.actualizarFuncionario(f, usuarioActual);
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
                if (data.eliminarFuncionario(id, usuarioActual)) {
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
            mostrarEnTabla(data.listarFuncionarios(usuarioActual));
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
