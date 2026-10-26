package reserva.logic;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Contrato del servicio de la aplicación (módulo Protocol). Lo implementan:
 *   - reserva.logic.Service del Backend: la lógica real contra MySQL.
 *   - reserva.logic.Service del Frontend: el PROXY, que manda cada llamada por
 *     el socket al Service remoto y devuelve el resultado (o relanza el error)
 *     como si la llamada hubiera sido local.
 * Las firmas son las mismas que tenía Data en el Proyecto 1.
 */
public interface IService {

    // ===================== USUARIOS (usado por Login) =====================
    boolean guardarUsuario(Usuario usuario);
    boolean actualizarUsuario(Usuario usuario);
    boolean eliminarUsuario(int id);
    Usuario buscarUsuarioPorId(int id);
    Usuario buscarUsuarioPorIdentificacion(String identificacion);
    List<Usuario> listarUsuarios();
    Usuario autenticar(String identificacion, String clave);
    boolean cambiarClave(String identificacion, String claveActual, String claveNueva);

    // ===================== FUNCIONARIOS =====================
    boolean guardarFuncionario(Funcionario funcionario, Usuario usuarioActual);
    boolean actualizarFuncionario(Funcionario funcionario, Usuario usuarioActual);
    boolean eliminarFuncionario(int id, Usuario usuarioActual);
    Funcionario buscarFuncionarioPorId(int id, Usuario usuarioActual);
    List<Funcionario> buscarFuncionariosPorNombre(String nombre, Usuario usuarioActual);
    List<Funcionario> listarFuncionarios(Usuario usuarioActual);

    // ===================== CATEGORIAS =====================
    boolean guardarCategoria(Categoria categoria, Usuario usuarioActual);
    boolean actualizarCategoria(Categoria categoria, Usuario usuarioActual);
    boolean eliminarCategoria(String id, Usuario usuarioActual);
    Categoria buscarCategoriaPorId(String id, Usuario usuarioActual);
    List<Categoria> buscarCategoriasPorDescripcion(String texto, Usuario usuarioActual);
    List<Categoria> listarCategorias(Usuario usuarioActual);

    // ===================== RECURSOS =====================
    boolean guardarRecurso(Recurso recurso, Usuario usuarioActual);
    boolean actualizarRecurso(Recurso recurso, Usuario usuarioActual);
    boolean eliminarRecurso(String id, Usuario usuarioActual);
    Recurso buscarRecursoPorId(String id, Usuario usuarioActual);
    List<Recurso> buscarRecursosPorCategoria(Categoria categoria, Usuario usuarioActual);
    List<Recurso> listarRecursos(Usuario usuarioActual);

    // ===================== RESERVAS =====================
    boolean guardarReserva(Reserva reserva);
    boolean actualizarReserva(Reserva reserva);
    boolean eliminarReserva(String id);
    Reserva buscarReservaPorId(String id);
    List<Reserva> listarReservas();
    List<Reserva> listarReservasPorFuncionario(int funcionarioId);
    boolean existeSolape(Recurso recurso, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin, String excluirReservaId);

    void stop();
}
