package reserva.data;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import reserva.logic.Administrador;
import reserva.logic.Categoria;
import reserva.logic.Funcionario;
import reserva.logic.Recurso;
import reserva.logic.Reserva;
import reserva.logic.Usuario;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de UNIDAD: cada prueba usa un "new Data()" fresco y vacío (no el
 * singleton Data.instance()), así que no dependen ni afectan el archivo XML
 * real ni a otras pruebas.
 */
@DisplayName("Data: CRUD, permisos y validaciones")
class DataTest {

    private Data data;
    private Usuario admin;
    private Usuario funcionario;

    @BeforeEach
    void setUp() {
        data = new Data();

        admin = new Administrador();
        admin.setId(1);
        admin.setIdentificacion("admin");
        admin.setNombre("Administrador");
        admin.setClave("admin");

        funcionario = new Funcionario();
        funcionario.setId(2);
        funcionario.setIdentificacion("111");
        funcionario.setNombre("Juan Perez");
        funcionario.setClave("111");
    }

    // ---------- Usuarios ----------

    @Test
    @DisplayName("guardarUsuario rechaza un id repetido")
    void guardarUsuarioRechazaIdRepetido() {
        assertTrue(data.guardarUsuario(admin));
        Usuario otro = new Administrador();
        otro.setId(1); // mismo id que admin
        assertFalse(data.guardarUsuario(otro));
    }

    @Test
    @DisplayName("buscarUsuarioPorIdentificacion encuentra por el id de login")
    void buscarPorIdentificacion() {
        data.guardarUsuario(admin);
        Usuario encontrado = data.buscarUsuarioPorIdentificacion("admin");
        assertNotNull(encontrado);
        assertEquals("Administrador", encontrado.getNombre());
        assertNull(data.buscarUsuarioPorIdentificacion("no-existe"));
    }

    // ---------- Funcionarios ----------

    @Test
    @DisplayName("guardarFuncionario deja la clave inicial igual al id")
    void guardarFuncionarioClaveIgualAlId() {
        Funcionario nuevo = new Funcionario();
        nuevo.setId(222);
        nuevo.setNombre("Maria Perez");
        nuevo.setTelefono("222222");

        assertTrue(data.guardarFuncionario(nuevo, admin));
        assertEquals("222", nuevo.getClave());
        assertEquals("222", nuevo.getIdentificacion());
    }

    @Test
    @DisplayName("Un funcionario no puede gestionar funcionarios (solo el admin)")
    void funcionarioNoPuedeGestionarFuncionarios() {
        Funcionario nuevo = new Funcionario();
        nuevo.setId(333);
        nuevo.setNombre("Otro");

        assertThrows(SecurityException.class, () -> data.guardarFuncionario(nuevo, funcionario));
    }

    @Test
    @DisplayName("eliminarFuncionario no borra un Administrador")
    void eliminarFuncionarioNoBorraAdmin() {
        data.guardarUsuario(admin);
        assertFalse(data.eliminarFuncionario(admin.getId(), admin));
        assertNotNull(data.buscarUsuarioPorId(admin.getId()));
    }

    // ---------- Categorias ----------

    @Test
    @DisplayName("guardarCategoria autogenera el id con formato CAT-000001")
    void guardarCategoriaAutogeneraId() {
        Categoria c1 = new Categoria();
        c1.setDescripcion("Sala para 10 personas");
        assertTrue(data.guardarCategoria(c1, admin));
        assertEquals("CAT-000001", c1.getId());

        Categoria c2 = new Categoria();
        c2.setDescripcion("Laptop windows 11");
        assertTrue(data.guardarCategoria(c2, admin));
        assertEquals("CAT-000002", c2.getId());
    }

    @Test
    @DisplayName("guardarCategoria rechaza descripcion vacia")
    void guardarCategoriaRechazaDescripcionVacia() {
        Categoria c = new Categoria();
        c.setDescripcion("   ");
        assertFalse(data.guardarCategoria(c, admin));
    }

    @Test
    @DisplayName("listarCategorias funciona para cualquier usuario logueado, no solo admin")
    void listarCategoriasEsDeLecturaLibre() {
        Categoria c = new Categoria();
        c.setDescripcion("Sala de Juntas");
        data.guardarCategoria(c, admin);

        List<Categoria> comoFuncionario = data.listarCategorias(funcionario);
        assertEquals(1, comoFuncionario.size());
    }

    @Test
    @DisplayName("listarCategorias exige una sesión activa")
    void listarCategoriasExigeSesion() {
        assertThrows(SecurityException.class, () -> data.listarCategorias(null));
    }

    // ---------- Recursos ----------

    @Test
    @DisplayName("guardarRecurso respeta el id manual y lo vincula a su categoria")
    void guardarRecursoConCategoria() {
        Categoria cat = new Categoria();
        cat.setDescripcion("Laptop windows 11");
        data.guardarCategoria(cat, admin);

        Recurso r = new Recurso();
        r.setId("238715");
        r.setCategoria(cat);
        r.setDescripcion("Laptop #238715");

        assertTrue(data.guardarRecurso(r, admin));
        assertEquals("238715", data.buscarRecursoPorId("238715", admin).getId());
    }

    @Test
    @DisplayName("guardarRecurso rechaza un id duplicado")
    void guardarRecursoRechazaIdDuplicado() {
        Categoria cat = new Categoria();
        cat.setDescripcion("Laptop windows 11");
        data.guardarCategoria(cat, admin);

        Recurso r1 = new Recurso();
        r1.setId("238715");
        r1.setCategoria(cat);
        r1.setDescripcion("Laptop A");
        data.guardarRecurso(r1, admin);

        Recurso r2 = new Recurso();
        r2.setId("238715");
        r2.setCategoria(cat);
        r2.setDescripcion("Laptop B");

        assertFalse(data.guardarRecurso(r2, admin));
    }

    @Test
    @DisplayName("buscarRecursosPorCategoria solo trae los de esa categoria")
    void buscarRecursosPorCategoria() {
        Categoria salas = new Categoria();
        salas.setDescripcion("Sala para 10 personas");
        data.guardarCategoria(salas, admin);

        Categoria laptops = new Categoria();
        laptops.setDescripcion("Laptop windows 11");
        data.guardarCategoria(laptops, admin);

        Recurso sala1 = new Recurso();
        sala1.setId("S-1");
        sala1.setCategoria(salas);
        data.guardarRecurso(sala1, admin);

        Recurso laptop1 = new Recurso();
        laptop1.setId("238715");
        laptop1.setCategoria(laptops);
        data.guardarRecurso(laptop1, admin);

        List<Recurso> soloLaptops = data.buscarRecursosPorCategoria(laptops, funcionario);
        assertEquals(1, soloLaptops.size());
        assertEquals("238715", soloLaptops.get(0).getId());
    }

    // ---------- Reservas y disponibilidad (lo más delicado del proyecto) ----------

    private Recurso crearRecursoDePrueba(String id) {
        Categoria cat = new Categoria();
        cat.setDescripcion("Sala de Juntas");
        data.guardarCategoria(cat, admin);

        Recurso r = new Recurso();
        r.setId(id);
        r.setCategoria(cat);
        r.setDescripcion("Recurso de prueba");
        data.guardarRecurso(r, admin);
        return r;
    }

    private Reserva crearReserva(Recurso recurso, LocalDate fecha, LocalTime inicio, LocalTime fin) {
        Reserva reserva = new Reserva();
        reserva.setActividad("Actividad de prueba");
        reserva.setFecha(fecha);
        reserva.setHoraInicio(inicio);
        reserva.setHoraFin(fin);
        reserva.setFuncionario(funcionario);
        reserva.setRecursos(List.of(recurso));
        data.guardarReserva(reserva);
        return reserva;
    }

    @Test
    @DisplayName("guardarReserva autogenera el id con formato RES-000001")
    void guardarReservaAutogeneraId() {
        Recurso recurso = crearRecursoDePrueba("R-1");
        Reserva r = crearReserva(recurso, LocalDate.of(2026, 8, 14), LocalTime.of(9, 0), LocalTime.of(11, 0));
        assertEquals("RES-000001", r.getId());
        assertEquals(Reserva.ACTIVA, r.getEstado());
    }

    @Test
    @DisplayName("existeSolape detecta un horario que se cruza en el mismo recurso y fecha")
    void existeSolapeDetectaCruce() {
        Recurso recurso = crearRecursoDePrueba("R-1");
        crearReserva(recurso, LocalDate.of(2026, 8, 14), LocalTime.of(9, 0), LocalTime.of(11, 0));

        boolean solapa = data.existeSolape(recurso, LocalDate.of(2026, 8, 14),
                LocalTime.of(10, 0), LocalTime.of(12, 0), null);
        assertTrue(solapa, "10:00-12:00 se cruza con una reserva de 9:00-11:00");
    }

    @Test
    @DisplayName("existeSolape es false si el horario no se cruza (empieza justo cuando termina la otra)")
    void existeSolapeNoDetectaHorarioConsecutivo() {
        Recurso recurso = crearRecursoDePrueba("R-1");
        crearReserva(recurso, LocalDate.of(2026, 8, 14), LocalTime.of(9, 0), LocalTime.of(11, 0));

        boolean solapa = data.existeSolape(recurso, LocalDate.of(2026, 8, 14),
                LocalTime.of(11, 0), LocalTime.of(12, 0), null);
        assertFalse(solapa);
    }

    @Test
    @DisplayName("existeSolape es false en otra fecha o con otro recurso")
    void existeSolapeNoDetectaOtraFechaUOtroRecurso() {
        Recurso recurso = crearRecursoDePrueba("R-1");
        crearReserva(recurso, LocalDate.of(2026, 8, 14), LocalTime.of(9, 0), LocalTime.of(11, 0));

        assertFalse(data.existeSolape(recurso, LocalDate.of(2026, 8, 15),
                LocalTime.of(9, 0), LocalTime.of(11, 0), null));

        Recurso otroRecurso = crearRecursoDePrueba("R-2");
        assertFalse(data.existeSolape(otroRecurso, LocalDate.of(2026, 8, 14),
                LocalTime.of(9, 0), LocalTime.of(11, 0), null));
    }

    @Test
    @DisplayName("Cancelar una reserva libera el recurso para ese horario")
    void cancelarReservaLiberaElRecurso() {
        Recurso recurso = crearRecursoDePrueba("R-1");
        Reserva reserva = crearReserva(recurso, LocalDate.of(2026, 8, 14), LocalTime.of(9, 0), LocalTime.of(11, 0));

        reserva.setEstado(Reserva.CANCELADA);
        data.actualizarReserva(reserva);

        boolean solapa = data.existeSolape(recurso, LocalDate.of(2026, 8, 14),
                LocalTime.of(9, 0), LocalTime.of(11, 0), null);
        assertFalse(solapa, "una reserva cancelada no debe bloquear el horario");
    }

    @Test
    @DisplayName("listarReservasPorFuncionario solo trae las de ese funcionario")
    void listarReservasPorFuncionario() {
        Recurso recurso = crearRecursoDePrueba("R-1");
        crearReserva(recurso, LocalDate.of(2026, 8, 14), LocalTime.of(9, 0), LocalTime.of(11, 0));

        Usuario otroFuncionario = new Funcionario();
        otroFuncionario.setId(999);
        otroFuncionario.setNombre("Otro Funcionario");

        List<Reserva> misReservas = data.listarReservasPorFuncionario(funcionario.getId());
        assertEquals(1, misReservas.size());

        List<Reserva> reservasDelOtro = data.listarReservasPorFuncionario(otroFuncionario.getId());
        assertTrue(reservasDelOtro.isEmpty());
    }
}
