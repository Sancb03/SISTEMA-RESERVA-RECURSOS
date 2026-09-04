package reserva.presentation.Funcionarios;

import reserva.data.FuncionarioDao;
import reserva.logic.Administrador;
import reserva.logic.Funcionario;
import reserva.logic.Usuario;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

class FuncionariosController {
    private final FuncionariosView view;
    private final FuncionariosModel model;
    private final TablaModel tableModel;
    private final FuncionarioDao dao;
    private final Usuario usuarioActual;

    public FuncionariosController(FuncionariosView view, Usuario usuarioActual) {
        this.view = view;
        this.model = new FuncionariosModel();
        this.tableModel = new TablaModel();
        this.dao = new FuncionarioDao();
        this.usuarioActual = usuarioActual;
        inicializar();
    }

    public FuncionariosController(FuncionariosView view, FuncionariosModel model, TablaModel tableModel) {
        this.view = view;
        this.model = model;
        this.tableModel = tableModel;
        this.dao = new FuncionarioDao();
        this.usuarioActual = null;
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
        if (!(usuarioActual instanceof Administrador)) {
            JOptionPane.showMessageDialog(view.getPanel1(),
                    "Solo un administrador puede acceder a esta pantalla.");
            habilitar(false);
            return;
        }

        view.getListadotable().setModel(tableModel);
        cargarListado();

        view.getBuscarButton().addActionListener(e -> buscar());
        view.getGuardarButton().addActionListener(e -> guardar());
        view.getBorrarButton().addActionListener(e -> borrar());
        view.getLimpiarButton().addActionListener(e -> limpiar());
        view.getImprimirButton().addActionListener(e -> cargarListado());
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
            String idTexto = view.getIdTField().getText().trim();
            String nombreTexto = view.getNombreTField().getText().trim();

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
            Funcionario existente = dao.buscarPorId(id, usuarioActual);

            Funcionario f = new Funcionario();
            f.setId(id);
            f.setNombre(nombre);
            f.setTelefono(telefono);

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
