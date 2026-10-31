package reserva.logic;

import reserva.data.CategoriaDao;
import reserva.data.RecursoDao;
import reserva.data.ReservaDao;
import reserva.data.UsuarioDao;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * El Service REAL (corre solo en el Backend), igual que el Service del ejemplo
 * Personas: un singleton que implementa IService usando los DAOs (MySQL).
 * Es lo que antes era reserva.data.Data: mismas reglas y mismos permisos.
 *
 * Los métodos son synchronized porque varios Frontend (cada uno con su Worker
 * e hilo en el Backend) usan esta misma instancia y la misma conexión a la base.
 */
public class Service implements IService {

    private static Service theInstance;

    public static IService instance() {
        if (theInstance == null) theInstance = new Service();
        return theInstance;
    }

    private final UsuarioDao usuarioDao;
    private final CategoriaDao categoriaDao;
    private final RecursoDao recursoDao;
    private final ReservaDao reservaDao;

    public Service() {
        this(new UsuarioDao(), new CategoriaDao(), new RecursoDao(), new ReservaDao());
    }

    public Service(UsuarioDao usuarioDao, CategoriaDao categoriaDao, RecursoDao recursoDao, ReservaDao reservaDao) {
        this.usuarioDao = usuarioDao;
        this.categoriaDao = categoriaDao;
        this.recursoDao = recursoDao;
        this.reservaDao = reservaDao;
    }

    @Override
    public void stop() {
        // nada que cerrar: la conexión a la base la cierra Application al apagar el Backend
    }

    private void validarAdmin(Usuario usuarioActual) {
        if (!(usuarioActual instanceof Administrador)) {
            throw new SecurityException("Solo un administrador puede realizar esta acción.");
        }
    }

    private void validarSesion(Usuario usuarioActual) {
        if (usuarioActual == null) {
            throw new SecurityException("Debe haber una sesión activa.");
        }
    }

    // ===================== USUARIOS (usado por Login) =====================

    @Override
    public synchronized boolean guardarUsuario(Usuario usuario) {
        return usuario != null && usuarioDao.create(usuario);
    }

    @Override
    public synchronized boolean actualizarUsuario(Usuario usuario) {
        return usuario != null && usuarioDao.update(usuario);
    }

    @Override
    public synchronized boolean eliminarUsuario(int id) {
        return usuarioDao.delete(id);
    }

    @Override
    public synchronized Usuario buscarUsuarioPorId(int id) {
        return usuarioDao.read(id);
    }

    @Override
    public synchronized Usuario buscarUsuarioPorIdentificacion(String identificacion) {
        return identificacion == null ? null : usuarioDao.readByIdentificacion(identificacion);
    }

    @Override
    public synchronized List<Usuario> listarUsuarios() {
        return usuarioDao.findAll();
    }

    /**
     * Revisa identificacion y clave. La comparacion se hace aqui en el Backend para que la
     * clave no tenga que viajar al Frontend. Devuelve el usuario SIN su clave.
     */
    @Override
    public synchronized Usuario autenticar(String identificacion, String clave) {
        if (identificacion == null || identificacion.isBlank()) {
            throw new IllegalArgumentException("Debe indicar su identificación.");
        }
        if (clave == null || clave.isBlank()) {
            throw new IllegalArgumentException("Debe indicar su clave.");
        }

        Usuario usuario = usuarioDao.readByIdentificacion(identificacion.trim());
        if (usuario == null) {
            throw new IllegalArgumentException("No existe un usuario con esa identificación.");
        }
        if (!clave.equals(usuario.getClave())) {
            throw new SecurityException("La clave es incorrecta.");
        }

        usuario.setClave(null);
        return usuario;
    }

    @Override
    public synchronized boolean cambiarClave(String identificacion, String claveActual, String claveNueva) {
        if (identificacion == null || identificacion.isBlank()) {
            throw new IllegalArgumentException("Primero escriba su identificación en la pantalla de ingreso.");
        }
        if (claveNueva == null || claveNueva.length() < 6) {
            throw new IllegalArgumentException("La nueva clave debe tener al menos 6 caracteres.");
        }

        Usuario usuario = usuarioDao.readByIdentificacion(identificacion.trim());
        if (usuario == null) {
            throw new IllegalArgumentException("No se encontró un usuario con esa identificación.");
        }
        if (claveActual == null || !claveActual.equals(usuario.getClave())) {
            throw new SecurityException("La clave actual no coincide.");
        }

        usuario.setClave(claveNueva);
        return usuarioDao.update(usuario);
    }

    // ===================== FUNCIONARIOS =====================

    @Override
    public synchronized boolean guardarFuncionario(Funcionario funcionario, Usuario usuarioActual) {
        validarAdmin(usuarioActual);
        if (funcionario == null) {
            return false;
        }
        if (funcionario.getId() <= 0) {
            throw new IllegalArgumentException("El id del funcionario debe ser mayor que cero.");
        }
        if (funcionario.getNombre() == null || funcionario.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre del funcionario es obligatorio.");
        }
        if (funcionario.getIdentificacion() == null || funcionario.getIdentificacion().isBlank()) {
            funcionario.setIdentificacion(String.valueOf(funcionario.getId()));
        }
        funcionario.setClave(String.valueOf(funcionario.getId()));
        return usuarioDao.create(funcionario);
    }

    @Override
    public synchronized boolean actualizarFuncionario(Funcionario funcionario, Usuario usuarioActual) {
        validarAdmin(usuarioActual);
        if (funcionario == null) {
            return false;
        }
        Usuario existente = usuarioDao.read(funcionario.getId());
        if (!(existente instanceof Funcionario)) {
            return false;
        }
        // La pantalla de Funcionarios solo manda id, nombre y teléfono: la
        // identificación y la clave que ya tenía NO se deben perder al editar.
        if (funcionario.getIdentificacion() == null || funcionario.getIdentificacion().isBlank()) {
            funcionario.setIdentificacion(existente.getIdentificacion());
        }
        if (funcionario.getClave() == null || funcionario.getClave().isBlank()) {
            funcionario.setClave(existente.getClave());
        }
        return usuarioDao.update(funcionario);
    }

    @Override
    public synchronized boolean eliminarFuncionario(int id, Usuario usuarioActual) {
        validarAdmin(usuarioActual);
        Usuario existente = usuarioDao.read(id);
        if (!(existente instanceof Funcionario)) {
            return false;
        }
        return usuarioDao.delete(id);
    }

    @Override
    public synchronized Funcionario buscarFuncionarioPorId(int id, Usuario usuarioActual) {
        validarAdmin(usuarioActual);
        Usuario u = usuarioDao.read(id);
        return (u instanceof Funcionario) ? (Funcionario) u : null;
    }

    @Override
    public synchronized List<Funcionario> buscarFuncionariosPorNombre(String nombre, Usuario usuarioActual) {
        validarAdmin(usuarioActual);
        if (nombre == null || nombre.isBlank()) {
            return new ArrayList<>();
        }
        return usuarioDao.findFuncionariosByNombre(nombre);
    }

    @Override
    public synchronized List<Funcionario> listarFuncionarios(Usuario usuarioActual) {
        validarAdmin(usuarioActual);
        return usuarioDao.findFuncionarios();
    }

    // ===================== CATEGORIAS =====================

    @Override
    public synchronized boolean guardarCategoria(Categoria categoria, Usuario usuarioActual) {
        validarAdmin(usuarioActual);
        if (categoria == null || categoria.getDescripcion() == null || categoria.getDescripcion().isBlank()) {
            return false;
        }
        categoriaDao.create(categoria); // el DAO le asigna el id CAT-xxxxxx
        return true;
    }

    @Override
    public synchronized boolean actualizarCategoria(Categoria categoria, Usuario usuarioActual) {
        validarAdmin(usuarioActual);
        if (categoria == null || categoria.getId() == null) {
            return false;
        }
        return categoriaDao.update(categoria);
    }

    @Override
    public synchronized boolean eliminarCategoria(String id, Usuario usuarioActual) {
        validarAdmin(usuarioActual);
        return categoriaDao.delete(id);
    }

    @Override
    public synchronized Categoria buscarCategoriaPorId(String id, Usuario usuarioActual) {
        validarAdmin(usuarioActual);
        return categoriaDao.read(id);
    }

    @Override
    public synchronized List<Categoria> buscarCategoriasPorDescripcion(String texto, Usuario usuarioActual) {
        validarAdmin(usuarioActual);
        if (texto == null || texto.isBlank()) {
            return new ArrayList<>();
        }
        return categoriaDao.findByDescripcion(texto);
    }

    /** Lectura abierta a cualquier usuario con sesión: Reservas y Calendarización la necesitan. */
    @Override
    public synchronized List<Categoria> listarCategorias(Usuario usuarioActual) {
        validarSesion(usuarioActual);
        return categoriaDao.findAll();
    }

    // ===================== RECURSOS =====================

    @Override
    public synchronized boolean guardarRecurso(Recurso recurso, Usuario usuarioActual) {
        validarAdmin(usuarioActual);
        if (recurso == null || recurso.getId() == null || recurso.getId().isBlank()
                || recurso.getCategoria() == null) {
            return false;
        }
        return recursoDao.create(recurso); // false si el id ya existe (en cualquier categoría)
    }

    @Override
    public synchronized boolean actualizarRecurso(Recurso recurso, Usuario usuarioActual) {
        validarAdmin(usuarioActual);
        if (recurso == null || recurso.getId() == null || recurso.getCategoria() == null) {
            return false;
        }
        return recursoDao.update(recurso);
    }

    @Override
    public synchronized boolean eliminarRecurso(String id, Usuario usuarioActual) {
        validarAdmin(usuarioActual);
        return recursoDao.delete(id);
    }

    @Override
    public synchronized Recurso buscarRecursoPorId(String id, Usuario usuarioActual) {
        validarAdmin(usuarioActual);
        return recursoDao.read(id);
    }

    @Override
    public synchronized List<Recurso> buscarRecursosPorCategoria(Categoria categoria, Usuario usuarioActual) {
        validarSesion(usuarioActual);
        if (categoria == null) {
            return new ArrayList<>();
        }
        return recursoDao.findByCategoria(categoria.getId());
    }

    @Override
    public synchronized List<Recurso> listarRecursos(Usuario usuarioActual) {
        validarSesion(usuarioActual);
        return recursoDao.findAll();
    }

    // ===================== RESERVAS =====================

    /**
     * Antes de guardar se vuelve a revisar, AQUÍ en el Backend, que ningún
     * recurso se cruce con otra reserva: dos Frontend pueden haber consultado
     * disponibilidad al mismo tiempo y reservar el mismo recurso. Gana el
     * primero en llegar; al segundo se le avisa con una excepción.
     */
    @Override
    public synchronized boolean guardarReserva(Reserva reserva) {
        if (reserva == null) {
            return false;
        }
        if (reserva.getActividad() == null || reserva.getActividad().isBlank()) {
            throw new IllegalArgumentException("La actividad de la reserva es obligatoria.");
        }
        if (reserva.getFecha() == null || reserva.getHoraInicio() == null || reserva.getHoraFin() == null) {
            throw new IllegalArgumentException("La fecha y las horas de la reserva son obligatorias.");
        }
        if (!reserva.getHoraFin().isAfter(reserva.getHoraInicio())) {
            throw new IllegalArgumentException("La hora final debe ser posterior a la hora inicial.");
        }
        if (reserva.getFuncionario() == null) {
            throw new IllegalArgumentException("La reserva debe tener un funcionario.");
        }
        if (reserva.getRecursos() == null || reserva.getRecursos().isEmpty()) {
            throw new IllegalArgumentException("La reserva debe tener al menos un recurso.");
        }
        for (Recurso recurso : reserva.getRecursos()) {
            if (reservaDao.existeSolape(recurso.getId(), reserva.getFecha(),
                    reserva.getHoraInicio(), reserva.getHoraFin(), null)) {
                throw new IllegalStateException("El recurso " + recurso.getId()
                        + " acaba de ser reservado por otro usuario en ese horario. Intente de nuevo.");
            }
        }
        reservaDao.create(reserva); // el DAO le asigna el id RES-xxxxxx
        return true;
    }

    @Override
    public synchronized boolean actualizarReserva(Reserva reserva) {
        if (reserva == null || reserva.getId() == null) {
            return false;
        }
        return reservaDao.update(reserva);
    }

    @Override
    public synchronized boolean eliminarReserva(String id) {
        return reservaDao.delete(id);
    }

    @Override
    public synchronized Reserva buscarReservaPorId(String id) {
        return reservaDao.read(id);
    }

    @Override
    public synchronized List<Reserva> listarReservas() {
        return reservaDao.findAll();
    }

    @Override
    public synchronized List<Reserva> listarReservasPorFuncionario(int funcionarioId) {
        return reservaDao.findByFuncionario(funcionarioId);
    }

    @Override
    public synchronized boolean existeSolape(Recurso recurso, LocalDate fecha, LocalTime horaInicio,
                                             LocalTime horaFin, String excluirReservaId) {
        return reservaDao.existeSolape(recurso.getId(), fecha, horaInicio, horaFin, excluirReservaId);
    }
}
