package reserva.logic;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Mismas firmas que hoy tiene reserva.data.Data (Proyecto 1), para que
 * los Controllers casi no cambien: donde antes decían
 *     Data.instance().listarCategorias(usuarioActual)
 * ahora dicen
 *     Service.instance().listarCategorias(usuarioActual)
 *
 * Dos clases van a implementar esta interfaz:
 *   - reserva.logic.ServiceImpl  (en el Backend): la lógica real,
 *     contra MySQL (reemplaza lo que hacía Data.java contra el XML).
 *   - reserva.logic.ServiceProxy (en el Frontend): el Proxy que pide
 *     la rúbrica. No tiene lógica de negocio: arma una Peticion, la
 *     manda por el socket, espera la Respuesta y la devuelve (o
 *     relanza la excepción) como si la llamada hubiera sido local.
 */
public interface Service {

    // ===================== USUARIOS (usado por Login) =====================
    boolean guardarUsuario(Usuario usuario);
    boolean actualizarUsuario(Usuario usuario);
    boolean eliminarUsuario(int id);
    Usuario buscarUsuarioPorId(int id);
    Usuario buscarUsuarioPorIdentificacion(String identificacion);
    List<Usuario> listarUsuarios();

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
}
