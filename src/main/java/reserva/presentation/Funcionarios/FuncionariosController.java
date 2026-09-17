package reserva.presentation.Funcionarios;

import reserva.GeneradorPDF;
import reserva.data.Data;
import reserva.logic.Funcionario;
import reserva.logic.Usuario;
import reserva.presentation.Iconos;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class FuncionariosController {

    private final FuncionariosView view;
    private final FuncionariosModel model;
    private final Data data;
    private final Usuario usuarioActual;

    public FuncionariosController(
            FuncionariosView view,
            FuncionariosModel model,
            Usuario usuarioActual) {

        this.view = view;
        this.model = model;
        this.data = Data.instance();
        this.usuarioActual = usuarioActual;

        view.setController(this);
        view.setModel(model);

        configurarIconos();
        cargarListado();
    }

    private void configurarIconos() {
        view.getBuscarButton().setIcon(Iconos.get("search"));
        view.getGuardarButton().setIcon(Iconos.get("save"));
        view.getBorrarButton().setIcon(Iconos.get("delete"));
        view.getLimpiarButton().setIcon(Iconos.get("clear"));
        view.getImprimirButton().setIcon(Iconos.get("pdf"));
    }

    public void buscar() {
        try {

            String idTexto =
                    view.getIdTField().getText().trim();

            String nombreTexto =
                    view.getNombreTField().getText().trim();

            if (!idTexto.isEmpty()) {

                Funcionario f =
                        data.buscarFuncionarioPorId(
                                Integer.parseInt(idTexto),
                                usuarioActual
                        );

                if (f != null) {

                    model.setCurrent(f);

                    List<Funcionario> lista =
                            new ArrayList<>();

                    lista.add(f);

                    model.setListado(lista);

                } else {

                    JOptionPane.showMessageDialog(
                            view.getPanel1(),
                            "No existe un funcionario con ese id."
                    );

                    model.setListado(
                            new ArrayList<>()
                    );
                }

            } else if (!nombreTexto.isEmpty()) {

                model.setListado(
                        data.buscarFuncionariosPorNombre(
                                nombreTexto,
                                usuarioActual
                        )
                );

            } else {

                cargarListado();
            }

        } catch (NumberFormatException ex) {

            JOptionPane.showMessageDialog(
                    view.getPanel1(),
                    "El id debe ser numérico."
            );

        } catch (RuntimeException ex) {

            JOptionPane.showMessageDialog(
                    view.getPanel1(),
                    ex.getMessage()
            );
        }
    }

    public void guardar(Funcionario f) {
        try {

            if (f.getNombre() == null ||
                    f.getNombre().isBlank()) {

                JOptionPane.showMessageDialog(
                        view.getPanel1(),
                        "El nombre es obligatorio."
                );

                return;
            }

            Funcionario existente =
                    data.buscarFuncionarioPorId(
                            f.getId(),
                            usuarioActual
                    );

            boolean ok;

            if (existente == null) {

                ok = data.guardarFuncionario(
                        f,
                        usuarioActual
                );

                if (ok) {

                    JOptionPane.showMessageDialog(
                            view.getPanel1(),
                            "Funcionario creado. Su clave inicial es igual al id (" +
                                    f.getId() + ")."
                    );

                } else {

                    JOptionPane.showMessageDialog(
                            view.getPanel1(),
                            "No se pudo crear el funcionario."
                    );
                }

            } else {

                ok = data.actualizarFuncionario(
                        f,
                        usuarioActual
                );

                if (ok) {

                    JOptionPane.showMessageDialog(
                            view.getPanel1(),
                            "Funcionario modificado."
                    );

                } else {

                    JOptionPane.showMessageDialog(
                            view.getPanel1(),
                            "No se pudo modificar el funcionario."
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

    public void borrar(String idTexto) {
        try {

            if (idTexto == null ||
                    idTexto.isBlank()) {

                JOptionPane.showMessageDialog(
                        view.getPanel1(),
                        "Ingrese o busque el id del funcionario a borrar."
                );

                return;
            }

            int id =
                    Integer.parseInt(idTexto);

            int confirmar =
                    JOptionPane.showConfirmDialog(
                            view.getPanel1(),
                            "¿Eliminar al funcionario " +
                                    id + "?"
                    );

            if (confirmar ==
                    JOptionPane.YES_OPTION) {

                if (data.eliminarFuncionario(
                        id,
                        usuarioActual)) {

                    limpiar();
                    cargarListado();

                } else {

                    JOptionPane.showMessageDialog(
                            view.getPanel1(),
                            "No se pudo eliminar el funcionario."
                    );
                }
            }

        } catch (NumberFormatException ex) {

            JOptionPane.showMessageDialog(
                    view.getPanel1(),
                    "El id debe ser numérico."
            );

        } catch (RuntimeException ex) {

            JOptionPane.showMessageDialog(
                    view.getPanel1(),
                    ex.getMessage()
            );
        }
    }

    public void limpiar() {

        view.getIdTField().setText("");
        view.getNombreTField().setText("");

        model.setCurrent(
                new Funcionario()
        );
    }

    private void cargarListado() {
        try {

            model.setListado(
                    data.listarFuncionarios(
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
                "Lista de Funcionarios",
                "Funcionarios.pdf"
        );
    }

    public FuncionariosView getView() {
        return view;
    }

    public FuncionariosModel getModel() {
        return model;
    }
}