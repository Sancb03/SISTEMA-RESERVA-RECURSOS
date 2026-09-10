package reserva.data;

import jakarta.xml.bind.annotation.*;
import reserva.logic.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Única fuente de verdad de los datos del sistema: un solo elemento raíz JAXB
 * (todo en un solo archivo XML) que además expone todos los métodos que antes
 * estaban repartidos en UsuarioDao/CategoriaDao/FuncionarioDao/RecursoDao/ReservaDao.
 * Esas 5 clases ya no existen: donde antes se hacía "new FuncionarioDao()" ahora
 * se usa directamente "Data.instance()".
 */
@XmlRootElement(name = "data")
@XmlAccessorType(XmlAccessType.FIELD)
public class Data {

    /**
     * Se puede sobreescribir con -Dreserva.data.archivo=... (así los tests usan
     * un archivo aparte y nunca tocan los datos reales de la aplicación).
     */
    private static final String ARCHIVO = System.getProperty("reserva.data.archivo", "data/sistema.xml");

    private static Data instancia;

    @XmlElementWrapper(name = "usuarios")
    @XmlElements({
            @XmlElement(name = "administrador", type = Administrador.class),
            @XmlElement(name = "funcionario", type = Funcionario.class)
    })
    private List<Usuario> usuarios = new ArrayList<>();

    @XmlElementWrapper(name = "categorias")
    @XmlElement(name = "categoria")
    private List<Categoria> categorias = new ArrayList<>();

    @XmlElementWrapper(name = "recursos")
    @XmlElement(name = "recurso")
    private List<Recurso> recursos = new ArrayList<>();

    @XmlElementWrapper(name = "reservas")
    @XmlElement(name = "reserva")
    private List<Reserva> reservas = new ArrayList<>();

    public Data() {
    }

    /** Punto de entrada único: reemplaza a "new XxxDao()". Carga (o crea) el XML la primera vez que se usa. */
    public static synchronized Data instance() {
        if (instancia == null) {
            instancia = cargar();
            if (instancia.usuarios.isEmpty()) {
                instancia.sembrarUsuariosDePrueba();
            }
        }
        return instancia;
    }

    private static Data cargar() {
        try {
            return new XmlPersister(ARCHIVO).load();
        } catch (Exception e) {
            throw new RuntimeException("No se pudo leer " + ARCHIVO + ": " + e.getMessage(), e);
        }
    }

    private void guardarCambios() {
        try {
            new XmlPersister(ARCHIVO).store(this);
        } catch (Exception e) {
            throw new RuntimeException("No se pudo escribir " + ARCHIVO + ": " + e.getMessage(), e);
        }
    }

    private void validarAdmin(Usuario usuarioActual) {
        if (!(usuarioActual instanceof Administrador)) {
            throw new SecurityException("Solo un administrador puede realizar esta acción.");
        }
    }

    private void sembrarUsuariosDePrueba() {
        Administrador admin = new Administrador();
        admin.setId(1);
        admin.setIdentificacion("admin");
        admin.setNombre("Administrador");
        admin.setClave("admin");
        usuarios.add(admin);

        Funcionario juan = new Funcionario();
        juan.setId(2);
        juan.setIdentificacion("111");
        juan.setNombre("Juan Perez");
        juan.setClave("111");
        juan.setTelefono("3323");
        usuarios.add(juan);

        Funcionario maria = new Funcionario();
        maria.setId(3);
        maria.setIdentificacion("222");
        maria.setNombre("Maria Perez");
        maria.setClave("222");
        maria.setTelefono("222222");
        usuarios.add(maria);

        guardarCambios();
    }

    // ===================== USUARIOS (usado por Login) =====================

    public boolean guardarUsuario(Usuario usuario) {
        if (usuario == null) {
            return false;
        }
        if (buscarUsuarioPorId(usuario.getId()) != null) {
            return false;
        }
        usuarios.add(usuario);
        guardarCambios();
        return true;
    }

    public boolean actualizarUsuario(Usuario usuario) {
        if (usuario == null) {
            return false;
        }
        for (int i = 0; i < usuarios.size(); i++) {
            if (usuarios.get(i).getId() == usuario.getId()) {
                usuarios.set(i, usuario);
                guardarCambios();
                return true;
            }
        }
        return false;
    }

    public boolean eliminarUsuario(int id) {
        boolean eliminado = usuarios.removeIf(u -> u.getId() == id);
        if (eliminado) {
            guardarCambios();
        }
        return eliminado;
    }

    public Usuario buscarUsuarioPorId(int id) {
        for (Usuario u : usuarios) {
            if (u.getId() == id) {
                return u;
            }
        }
        return null;
    }

    public Usuario buscarUsuarioPorIdentificacion(String identificacion) {
        if (identificacion == null) {
            return null;
        }
        for (Usuario u : usuarios) {
            if (identificacion.equals(u.getIdentificacion())) {
                return u;
            }
        }
        return null;
    }

    public List<Usuario> listarUsuarios() {
        return new ArrayList<>(usuarios);
    }

    // ===================== FUNCIONARIOS =====================

    public boolean guardarFuncionario(Funcionario funcionario, Usuario usuarioActual) {
        validarAdmin(usuarioActual);
        if (funcionario == null) {
            return false;
        }
        if (funcionario.getIdentificacion() == null || funcionario.getIdentificacion().isBlank()) {
            funcionario.setIdentificacion(String.valueOf(funcionario.getId()));
        }
        funcionario.setClave(String.valueOf(funcionario.getId()));
        return guardarUsuario(funcionario);
    }

    public boolean actualizarFuncionario(Funcionario funcionario, Usuario usuarioActual) {
        validarAdmin(usuarioActual);
        if (funcionario == null) {
            return false;
        }
        Usuario existente = buscarUsuarioPorId(funcionario.getId());
        if (!(existente instanceof Funcionario)) {
            return false;
        }
        return actualizarUsuario(funcionario);
    }

    public boolean eliminarFuncionario(int id, Usuario usuarioActual) {
        validarAdmin(usuarioActual);
        Usuario existente = buscarUsuarioPorId(id);
        if (!(existente instanceof Funcionario)) {
            return false;
        }
        return eliminarUsuario(id);
    }

    public Funcionario buscarFuncionarioPorId(int id, Usuario usuarioActual) {
        validarAdmin(usuarioActual);
        Usuario u = buscarUsuarioPorId(id);
        return (u instanceof Funcionario) ? (Funcionario) u : null;
    }

    public List<Funcionario> buscarFuncionariosPorNombre(String nombre, Usuario usuarioActual) {
        validarAdmin(usuarioActual);
        List<Funcionario> resultado = new ArrayList<>();
        if (nombre == null || nombre.isBlank()) {
            return resultado;
        }
        String buscado = nombre.toLowerCase();
        for (Usuario u : usuarios) {
            if (u instanceof Funcionario f && f.getNombre() != null && f.getNombre().toLowerCase().contains(buscado)) {
                resultado.add(f);
            }
        }
        return resultado;
    }

    public List<Funcionario> listarFuncionarios(Usuario usuarioActual) {
        validarAdmin(usuarioActual);
        List<Funcionario> resultado = new ArrayList<>();
        for (Usuario u : usuarios) {
            if (u instanceof Funcionario f) {
                resultado.add(f);
            }
        }
        return resultado;
    }

    // ===================== CATEGORIAS =====================

    private static final String PREFIJO_CATEGORIA = "CAT-";
    private static final int DIGITOS_CATEGORIA = 6;

    private String generarSiguienteIdCategoria() {
        int maxNumero = 0;
        for (Categoria c : categorias) {
            String id = c.getId();
            if (id != null && id.startsWith(PREFIJO_CATEGORIA)) {
                try {
                    maxNumero = Math.max(maxNumero, Integer.parseInt(id.substring(PREFIJO_CATEGORIA.length())));
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return PREFIJO_CATEGORIA + String.format("%0" + DIGITOS_CATEGORIA + "d", maxNumero + 1);
    }

    public boolean guardarCategoria(Categoria categoria, Usuario usuarioActual) {
        validarAdmin(usuarioActual);
        if (categoria == null || categoria.getDescripcion() == null || categoria.getDescripcion().isBlank()) {
            return false;
        }
        categoria.setId(generarSiguienteIdCategoria());
        categorias.add(categoria);
        guardarCambios();
        return true;
    }

    public boolean actualizarCategoria(Categoria categoria, Usuario usuarioActual) {
        validarAdmin(usuarioActual);
        if (categoria == null || categoria.getId() == null) {
            return false;
        }
        Categoria existente = buscarCategoriaPorIdInterno(categoria.getId());
        if (existente == null) {
            return false;
        }
        existente.setDescripcion(categoria.getDescripcion());
        guardarCambios();
        return true;
    }

    public boolean eliminarCategoria(String id, Usuario usuarioActual) {
        validarAdmin(usuarioActual);
        boolean eliminado = categorias.removeIf(c -> c.getId().equals(id));
        if (eliminado) {
            guardarCambios();
        }
        return eliminado;
    }

    public Categoria buscarCategoriaPorId(String id, Usuario usuarioActual) {
        validarAdmin(usuarioActual);
        return buscarCategoriaPorIdInterno(id);
    }

    private Categoria buscarCategoriaPorIdInterno(String id) {
        for (Categoria c : categorias) {
            if (c.getId().equals(id)) {
                return c;
            }
        }
        return null;
    }

    public List<Categoria> buscarCategoriasPorDescripcion(String texto, Usuario usuarioActual) {
        validarAdmin(usuarioActual);
        List<Categoria> resultado = new ArrayList<>();
        if (texto == null || texto.isBlank()) {
            return resultado;
        }
        String buscado = texto.toLowerCase();
        for (Categoria c : categorias) {
            if (c.getDescripcion() != null && c.getDescripcion().toLowerCase().contains(buscado)) {
                resultado.add(c);
            }
        }
        return resultado;
    }

    /**
     * A diferencia de las demás operaciones de Categoría, esta lectura NO se
     * restringe a administradores: Reservas y Calendarización también necesitan
     * que un funcionario pueda ver la lista de categorías (para elegir una),
     * aunque solo el administrador pueda gestionarlas.
     */
    public List<Categoria> listarCategorias(Usuario usuarioActual) {
        if (usuarioActual == null) {
            throw new SecurityException("Debe haber una sesión activa.");
        }
        return new ArrayList<>(categorias);
    }

    // ===================== RECURSOS =====================

    public boolean guardarRecurso(Recurso recurso, Usuario usuarioActual) {
        validarAdmin(usuarioActual);
        if (recurso == null || recurso.getId() == null || recurso.getId().isBlank()) {
            return false;
        }
        if (buscarRecursoPorIdInterno(recurso.getId()) != null) {
            return false;
        }
        recursos.add(recurso);
        guardarCambios();
        return true;
    }

    public boolean actualizarRecurso(Recurso recurso, Usuario usuarioActual) {
        validarAdmin(usuarioActual);
        if (recurso == null || recurso.getId() == null) {
            return false;
        }
        Recurso existente = buscarRecursoPorIdInterno(recurso.getId());
        if (existente == null) {
            return false;
        }
        existente.setCategoria(recurso.getCategoria());
        existente.setDescripcion(recurso.getDescripcion());
        guardarCambios();
        return true;
    }

    public boolean eliminarRecurso(String id, Usuario usuarioActual) {
        validarAdmin(usuarioActual);
        boolean eliminado = recursos.removeIf(r -> r.getId().equals(id));
        if (eliminado) {
            guardarCambios();
        }
        return eliminado;
    }

    public Recurso buscarRecursoPorId(String id, Usuario usuarioActual) {
        validarAdmin(usuarioActual);
        return buscarRecursoPorIdInterno(id);
    }

    private Recurso buscarRecursoPorIdInterno(String id) {
        for (Recurso r : recursos) {
            if (r.getId().equals(id)) {
                return r;
            }
        }
        return null;
    }

    /** Igual que listarRecursos: cualquier usuario logueado puede leer (lo necesita Reservas y Calendarización). */
    public List<Recurso> buscarRecursosPorCategoria(Categoria categoria, Usuario usuarioActual) {
        if (usuarioActual == null) {
            throw new SecurityException("Debe haber una sesión activa.");
        }
        List<Recurso> resultado = new ArrayList<>();
        if (categoria == null) {
            return resultado;
        }
        for (Recurso r : recursos) {
            if (r.getCategoria() != null && r.getCategoria().getId().equals(categoria.getId())) {
                resultado.add(r);
            }
        }
        return resultado;
    }

    /** Igual que listarCategorias: cualquier usuario logueado puede leer, solo el admin gestiona (guardar/actualizar/eliminar). */
    public List<Recurso> listarRecursos(Usuario usuarioActual) {
        if (usuarioActual == null) {
            throw new SecurityException("Debe haber una sesión activa.");
        }
        return new ArrayList<>(recursos);
    }

    // ===================== RESERVAS =====================

    private static final String PREFIJO_RESERVA = "RES-";
    private static final int DIGITOS_RESERVA = 6;

    private String generarSiguienteIdReserva() {
        int maxNumero = 0;
        for (Reserva r : reservas) {
            String id = r.getId();
            if (id != null && id.startsWith(PREFIJO_RESERVA)) {
                try {
                    maxNumero = Math.max(maxNumero, Integer.parseInt(id.substring(PREFIJO_RESERVA.length())));
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return PREFIJO_RESERVA + String.format("%0" + DIGITOS_RESERVA + "d", maxNumero + 1);
    }

    public boolean guardarReserva(Reserva reserva) {
        if (reserva == null) {
            return false;
        }
        reserva.setId(generarSiguienteIdReserva());
        reservas.add(reserva);
        guardarCambios();
        return true;
    }

    public boolean actualizarReserva(Reserva reserva) {
        if (reserva == null || reserva.getId() == null) {
            return false;
        }
        for (int i = 0; i < reservas.size(); i++) {
            if (reservas.get(i).getId().equals(reserva.getId())) {
                reservas.set(i, reserva);
                guardarCambios();
                return true;
            }
        }
        return false;
    }

    public boolean eliminarReserva(String id) {
        boolean eliminado = reservas.removeIf(r -> r.getId().equals(id));
        if (eliminado) {
            guardarCambios();
        }
        return eliminado;
    }

    public Reserva buscarReservaPorId(String id) {
        for (Reserva r : reservas) {
            if (r.getId().equals(id)) {
                return r;
            }
        }
        return null;
    }

    public List<Reserva> listarReservas() {
        return new ArrayList<>(reservas);
    }

    public List<Reserva> listarReservasPorFuncionario(int funcionarioId) {
        List<Reserva> resultado = new ArrayList<>();
        for (Reserva r : reservas) {
            if (r.getFuncionario() != null && r.getFuncionario().getId() == funcionarioId) {
                resultado.add(r);
            }
        }
        return resultado;
    }

    /**
     * ¿Ese recurso ya tiene una reserva ACTIVA que se cruza con el horario pedido,
     * en esa misma fecha? 'excluirReservaId' se usa al recalcular una reserva
     * para no chocar consigo misma (puede ser null).
     */
    public boolean existeSolape(Recurso recurso, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin, String excluirReservaId) {
        for (Reserva r : reservas) {
            if (excluirReservaId != null && excluirReservaId.equals(r.getId())) {
                continue;
            }
            if (!Reserva.ACTIVA.equals(r.getEstado())) {
                continue;
            }
            if (!r.getFecha().equals(fecha)) {
                continue;
            }
            boolean tieneEseRecurso = r.getRecursos().stream()
                    .anyMatch(x -> x.getId().equals(recurso.getId()));
            if (!tieneEseRecurso) {
                continue;
            }
            boolean seCruzan = horaInicio.isBefore(r.getHoraFin()) && r.getHoraInicio().isBefore(horaFin);
            if (seCruzan) {
                return true;
            }
        }
        return false;
    }
}
