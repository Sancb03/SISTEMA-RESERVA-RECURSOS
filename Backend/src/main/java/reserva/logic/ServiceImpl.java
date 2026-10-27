package reserva.logic;

import reserva.data.CategoriaDao;
import reserva.data.RecursoDao;
import reserva.data.ReservaDao;
import reserva.data.UsuarioDao;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * La implementación REAL del Service (corre solo en el Backend).
 * Es lo que antes era reserva.data.Data: mismas reglas y mismos permisos,
 * pero en vez de listas en memoria + archivo XML, usa los DAOs (MySQL).
 *
 * Los métodos son synchronized porque varios Frontend (cada uno con su hilo
 * en el Backend) usan esta misma instancia y la misma conexión a la base.
 */
public class ServiceImpl implements Service {

    private final UsuarioDao usuarioDao;
    private final CategoriaDao categoriaDao;
    private final RecursoDao recursoDao;
    private final ReservaDao reservaDao;

    /** Quienes quieran enterarse de las reservas nuevas (el Servidor las reenvía como alertas). */
    private final List<Consumer<Reserva>> oyentesNuevaReserva = new CopyOnWriteArrayList<>();

    public ServiceImpl() {
        this(new UsuarioDao(), new CategoriaDao(), new RecursoDao(), new ReservaDao());
    }

    public ServiceImpl(UsuarioDao usuarioDao, CategoriaDao categoriaDao, RecursoDao recursoDao, ReservaDao reservaDao) {
        this.usuarioDao = usuarioDao;
        this.categoriaDao = categoriaDao;
        this.recursoDao = recursoDao;
        this.reservaDao = reservaDao;
    }

    public void agregarOyenteNuevaReserva(Consumer<Reserva> oyente) {
        oyentesNuevaReserva.add(oyente);
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
        if (usuario == null) {
            return false;
        }
        if (usuario.getClave() == null || usuario.getClave().isBlank()) {
            // el Frontend ya no conoce la clave: si no manda una nueva, se conserva la que hay
            Usuario existente = usuarioDao.read(usuario.getId());
            if (existente == null) {
                return false;
            }
            usuario.setClave(existente.getClave());
        }
        return usuarioDao.update(usuario);
    }

    @Override
    public synchronized boolean eliminarUsuario(int id) {
        return usuarioDao.delete(id);
    }

    @Override
    public synchronized Usuario buscarUsuarioPorId(int id) {
        return sinClave(usuarioDao.read(id));
    }

    @Override
    public synchronized Usuario buscarUsuarioPorIdentificacion(String identificacion) {
        return identificacion == null ? null : sinClave(usuarioDao.readByIdentificacion(identificacion));
    }

    @Override
    public synchronized List<Usuario> listarUsuarios() {
        List<Usuario> resultado = new ArrayList<>();
        for (Usuario u : usuarioDao.findAll()) {
            resultado.add(sinClave(u));
        }
        return resultado;
    }

    @Override
    public synchronized Usuario login(String identificacion, String clave) {
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
            throw new IllegalArgumentException("La clave es incorrecta.");
        }
        return sinClave(usuario);
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
            throw new IllegalArgumentException("La clave actual no coincide.");
        }
        usuario.setClave(claveNueva);
        return usuarioDao.update(usuario);
    }

    /** Copia del usuario SIN clave: es lo único que sale del Backend hacia el Frontend. */
    private static <T extends Usuario> T sinClave(T original) {
        if (original == null) {
            return null;
        }
        Usuario copia;
        if (original instanceof Funcionario f) {
            Funcionario nuevo = new Funcionario();
            nuevo.setTelefono(f.getTelefono());
            copia = nuevo;
        } else {
            copia = new Administrador();
        }
        copia.setId(original.getId());
        copia.setIdentificacion(original.getIdentificacion());
        copia.setNombre(original.getNombre());
        copia.setClave(null);
        @SuppressWarnings("unchecked")
        T resultado = (T) copia;
        return resultado;
    }
    // ===================== FUNCIONARIOS =====================

    @Override
    public synchronized boolean guardarFuncionario(Funcionario funcionario, Usuario usuarioActual) {
        validarAdmin(usuarioActual);
        if (funcionario == null) {
            return false;
        }
        if (funcionario.getIdentificacion() == null || funcionario.getIdentificacion().isBlank()) {
            funcionario.setIdentificacion(String.valueOf(funcionario.getId()));
        }
        funcionario.setClave(String.valueOf(funcionario.getId())); // clave inicial = id
        boolean creado = usuarioDao.create(funcionario);
        funcionario.setClave(null); // la copia que vuelve al Frontend no lleva la clave
        return creado;
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
        return (u instanceof Funcionario f) ? sinClave(f) : null;
    }

    @Override
    public synchronized List<Funcionario> buscarFuncionariosPorNombre(String nombre, Usuario usuarioActual) {
        validarAdmin(usuarioActual);
        if (nombre == null || nombre.isBlank()) {
            return new ArrayList<>();
        }
        List<Funcionario> resultado = new ArrayList<>();
        for (Funcionario f : usuarioDao.findFuncionariosByNombre(nombre)) {
            resultado.add(sinClave(f));
        }
        return resultado;
    }

    @Override
    public synchronized List<Funcionario> listarFuncionarios(Usuario usuarioActual) {
        validarAdmin(usuarioActual);
        List<Funcionario> resultado = new ArrayList<>();
        for (Funcionario f : usuarioDao.findFuncionarios()) {
            resultado.add(sinClave(f));
        }
        return resultado;
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
        for (Recurso recurso : reserva.getRecursos()) {
            if (reservaDao.existeSolape(recurso.getId(), reserva.getFecha(),
                    reserva.getHoraInicio(), reserva.getHoraFin(), null)) {
                throw new IllegalStateException("El recurso " + recurso.getId()
                        + " acaba de ser reservado por otro usuario en ese horario. Intente de nuevo.");
            }
        }
        reservaDao.create(reserva); // el DAO le asigna el id RES-xxxxxx
        Reserva guardada = reservaDao.read(reserva.getId());
        for (Consumer<Reserva> oyente : oyentesNuevaReserva) {
            oyente.accept(guardada);
        }
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
