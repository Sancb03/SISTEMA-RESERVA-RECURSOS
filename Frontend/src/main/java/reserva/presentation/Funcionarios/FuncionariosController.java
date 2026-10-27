package reserva.presentation.Funcionarios;

import reserva.GeneradorPDF;
import reserva.logic.Service;
import reserva.logic.ServiceProxy;
import reserva.logic.Funcionario;
import reserva.logic.Usuario;
import reserva.presentation.Iconos;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class FuncionariosController {

    private final FuncionariosView view;
    private final FuncionariosModel model;
    private final Service service;
    private final Usuario usuarioActual;

    public FuncionariosController(
            FuncionariosView view,
            FuncionariosModel model,
            Usuario usuarioActual) {

        this.view = view;
        this.model = model;
        this.service = ServiceProxy.instance();
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
                        service.buscarFuncionarioPorId(
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
                        service.buscarFuncionariosPorNombre(
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

    public void guardar(String idTexto, String nombre, String telefono) {
        idTexto = idTexto == null ? "" : idTexto.trim();
        nombre = nombre == null ? "" : nombre.trim();
        telefono = telefono == null ? "" : telefono.trim();

        String error = null;
        if (idTexto.isEmpty()) {
            error = "El id es obligatorio.";
        } else if (!idTexto.matches("\\d{1,9}") || Integer.parseInt(idTexto) <= 0) {
            error = "El id debe ser un número entero positivo (solo dígitos).";
        } else if (nombre.isEmpty()) {
            error = "El nombre es obligatorio.";
        } else if (nombre.length() > 80) {
            error = "El nombre no puede pasar de 80 caracteres.";
        } else if (telefono.isEmpty()) {
            error = "El teléfono es obligatorio.";
        } else if (!telefono.matches("[0-9+\\-() ]+") || !telefono.matches(".*\\d.*")) {
            error = "El teléfono solo puede tener números (y los símbolos + - ( ) ), no letras.";
        } else if (telefono.length() > 20) {
            error = "El teléfono no puede pasar de 20 caracteres.";
        }

        if (error != null) {
            JOptionPane.showMessageDialog(view.getPanel1(), error,
                    "Dato inválido", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Funcionario f = new Funcionario();
        f.setId(Integer.parseInt(idTexto));
        f.setNombre(nombre);
        f.setTelefono(telefono);
        guardar(f);
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
                    service.buscarFuncionarioPorId(
                            f.getId(),
                            usuarioActual
                    );

            Funcionario enPantalla = model.getCurrent();
            boolean esEdicion = existente != null && enPantalla != null && enPantalla.getId() == f.getId();
            if (existente != null && !esEdicion) {

                JOptionPane.showMessageDialog(
                        view.getPanel1(),
                        "Ya existe un funcionario con el id " + f.getId() +
                                ". Búsquelo para modificarlo o use otro id.",
                        "Id repetido",
                        JOptionPane.WARNING_MESSAGE
                );

                return;
            }

            boolean ok;

            if (existente == null) {

                ok = service.guardarFuncionario(
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
                            "Ese funcionario ya existe."
                    );
                }

            } else {

                ok = service.actualizarFuncionario(
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

                if (service.eliminarFuncionario(
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
                    service.listarFuncionarios(
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