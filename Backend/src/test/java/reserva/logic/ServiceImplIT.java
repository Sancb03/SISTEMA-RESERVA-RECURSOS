package reserva.logic;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reserva.data.BaseDePruebas;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Prueba de INTEGRACION del Backend contra MySQL de verdad (base
 * "reservarecursos_test", creada desde cero con sql/sistema_reservas.sql).
 * Es lo que antes eran DataTest y FlujoReservaIT, que probaban Data contra el
 * XML. Si no hay MySQL corriendo, la clase se salta con un aviso.
 *
 * Cada prueba usa ids propios (ver sigId), así que no dependen del orden.
 */
@DisplayName("ServiceImpl + MySQL: CRUD, permisos, reglas y disponibilidad")
class ServiceImplIT {

    private static final AtomicInteger contador = new AtomicInteger();

    private static ServiceImpl service;
    private static Usuario admin;
    private static Usuario juan;

    @BeforeAll
    static void prepararBase() {
        assumeTrue(BaseDePruebas.preparar(), "No hay MySQL disponible: se salta la prueba de integración");
        service = new ServiceImpl();
        admin = service.buscarUsuarioPorIdentificacion("admin");
        juan = service.buscarUsuarioPorIdentificacion("111");
    }

    private static String sigId(String prefijo) {
        return prefijo + contador.incrementAndGet();
    }

    private Categoria crearCategoria(String descripcion) {
        Categoria c = new Categoria();
        c.setDescripcion(descripcion);
        assertTrue(service.guardarCategoria(c, admin));
        return c;
    }

    private Recurso crearRecurso(Categoria categoria) {
        Recurso r = new Recurso();
        r.setId(sigId("IT-R"));
        r.setCategoria(categoria);
        r.setDescripcion("Recurso de prueba");
        assertTrue(service.guardarRecurso(r, admin));
        return r;
    }

    private Reserva crearReserva(Usuario funcionario, LocalDate fecha, LocalTime inicio, LocalTime fin, Recurso... recursos) {
        Reserva reserva = new Reserva();
        reserva.setActividad("Actividad de prueba");
        reserva.setFecha(fecha);
        reserva.setHoraInicio(inicio);
        reserva.setHoraFin(fin);
        reserva.setFuncionario(funcionario);
        reserva.setRecursos(new ArrayList<>(List.of(recursos)));
        assertTrue(service.guardarReserva(reserva));
        return reserva;
    }

    private static int numeroDe(String id) {
        return Integer.parseInt(id.substring(id.indexOf('-') + 1));
    }

    // ---------- Usuarios y Login ----------

    @Test
    @DisplayName("Los usuarios semilla del script existen y tienen el rol correcto")
    void usuariosSemilla() {
        assertTrue(admin instanceof Administrador);
        assertNull(admin.getClave(), "la clave no debe salir del Backend");
        assertTrue(juan instanceof Funcionario);
        assertEquals("Juan Perez", juan.getNombre());
        assertNull(service.buscarUsuarioPorIdentificacion("no-existe"));
    }

    @Test
    @DisplayName("login correcto devuelve el usuario sin su clave")
    void loginCorrecto() {
        Usuario u = service.login("111", "111");
        assertEquals("Juan Perez", u.getNombre());
        assertTrue(u instanceof Funcionario);
        assertNull(u.getClave(), "la clave no debe volver al Frontend");

        assertTrue(service.login(" admin ", "admin") instanceof Administrador, "la identificación se recorta");
    }

    @Test
    @DisplayName("login incorrecto: clave mala, usuario inexistente y campos vacíos lanzan IllegalArgumentException")
    void loginIncorrecto() {
        IllegalArgumentException claveMala =
                assertThrows(IllegalArgumentException.class, () -> service.login("111", "mala"));
        assertEquals("La clave es incorrecta.", claveMala.getMessage());

        IllegalArgumentException sinUsuario =
                assertThrows(IllegalArgumentException.class, () -> service.login("no-existe", "x"));
        assertEquals("No existe un usuario con esa identificación.", sinUsuario.getMessage());

        assertThrows(IllegalArgumentException.class, () -> service.login("", "111"));
        assertThrows(IllegalArgumentException.class, () -> service.login("111", " "));
        assertThrows(IllegalArgumentException.class, () -> service.login(null, null));
    }

    @Test
    @DisplayName("cambiarClave valida la clave actual en el Backend y la nueva clave sirve al volver a entrar")
    void cambiarClave() {
        assertTrue(service.cambiarClave("222", "222", "nuevaClave1"));

        assertEquals("Maria Perez", service.login("222", "nuevaClave1").getNombre());
        assertThrows(IllegalArgumentException.class, () -> service.login("222", "222"),
                "la clave vieja ya no debe servir");

        // se deja como estaba (la clave semilla "222" es más corta que el mínimo de cambiarClave,
        // por eso se restaura con actualizarUsuario)
        Usuario maria = service.buscarUsuarioPorIdentificacion("222");
        maria.setClave("222");
        assertTrue(service.actualizarUsuario(maria));
        assertEquals("Maria Perez", service.login("222", "222").getNombre());
    }

    @Test
    @DisplayName("cambiarClave rechaza clave actual incorrecta, clave nueva corta y usuario inexistente")
    void cambiarClaveRechazaDatosMalos() {
        IllegalArgumentException actualMala = assertThrows(IllegalArgumentException.class,
                () -> service.cambiarClave("444", "equivocada", "claveNueva1"));
        assertEquals("La clave actual no coincide.", actualMala.getMessage());

        assertThrows(IllegalArgumentException.class, () -> service.cambiarClave("444", "444", "123"));
        assertThrows(IllegalArgumentException.class, () -> service.cambiarClave("no-existe", "x", "claveNueva1"));

        assertEquals("Keren Rojas", service.login("444", "444").getNombre());
    }

    @Test
    @DisplayName("Ninguna consulta de usuarios o funcionarios devuelve la clave")
    void laClaveNuncaViajaAlFrontend() {
        assertNull(service.buscarUsuarioPorId(juan.getId()).getClave());
        assertNull(service.buscarUsuarioPorIdentificacion("111").getClave());
        assertTrue(service.listarUsuarios().stream().allMatch(u -> u.getClave() == null));
        assertTrue(service.listarFuncionarios(admin).stream().allMatch(f -> f.getClave() == null));
        assertTrue(service.buscarFuncionariosPorNombre("perez", admin).stream().allMatch(f -> f.getClave() == null));
        assertNull(service.buscarFuncionarioPorId(juan.getId(), admin).getClave());
    }

    @Test
    @DisplayName("actualizarUsuario sin clave conserva la que ya estaba guardada")
    void actualizarUsuarioSinClaveConservaLaClave() {
        Usuario keren = service.buscarUsuarioPorIdentificacion("444");
        keren.setNombre("Keren Rojas");
        assertTrue(service.actualizarUsuario(keren));
        assertEquals("Keren Rojas", service.login("444", "444").getNombre());
    }

    // ---------- Funcionarios ----------

    @Test
    @DisplayName("guardarFuncionario deja la clave inicial y la identificación iguales al id")
    void guardarFuncionarioClaveIgualAlId() {
        Funcionario nuevo = new Funcionario();
        nuevo.setId(7001);
        nuevo.setNombre("Ana Lopez");
        nuevo.setTelefono("8888");

        assertTrue(service.guardarFuncionario(nuevo, admin));

        assertNull(nuevo.getClave(), "la copia que vuelve al Frontend no lleva la clave");

        Usuario guardado = service.buscarUsuarioPorIdentificacion("7001");
        assertNull(guardado.getClave());
        assertEquals("Ana Lopez", guardado.getNombre());
        assertEquals("8888", ((Funcionario) guardado).getTelefono());
        assertEquals("Ana Lopez", service.login("7001", "7001").getNombre(), "la clave inicial es el id");
    }

    @Test
    @DisplayName("guardarFuncionario rechaza un id repetido")
    void guardarFuncionarioRechazaIdRepetido() {
        Funcionario repetido = new Funcionario();
        repetido.setId(juan.getId());
        repetido.setNombre("Otro Juan");
        assertFalse(service.guardarFuncionario(repetido, admin));
    }

    @Test
    @DisplayName("Un funcionario no puede gestionar funcionarios (solo el admin)")
    void funcionarioNoPuedeGestionarFuncionarios() {
        Funcionario nuevo = new Funcionario();
        nuevo.setId(7002);
        nuevo.setNombre("Otro");

        assertThrows(SecurityException.class, () -> service.guardarFuncionario(nuevo, juan));
        assertThrows(SecurityException.class, () -> service.listarFuncionarios(juan));
    }

    @Test
    @DisplayName("actualizarFuncionario conserva la identificación y la clave que ya tenía")
    void actualizarFuncionarioConservaClave() {
        Funcionario nuevo = new Funcionario();
        nuevo.setId(7003);
        nuevo.setNombre("Pedro Mora");
        nuevo.setTelefono("1111");
        service.guardarFuncionario(nuevo, admin);

        // la pantalla de Funcionarios solo manda id, nombre y teléfono
        Funcionario editado = new Funcionario();
        editado.setId(7003);
        editado.setNombre("Pedro Mora Rojas");
        editado.setTelefono("2222");
        assertTrue(service.actualizarFuncionario(editado, admin));

        Usuario leido = service.buscarUsuarioPorIdentificacion("7003");
        assertNotNull(leido, "debe poder seguir entrando con su identificación");
        assertEquals("Pedro Mora Rojas", service.login("7003", "7003").getNombre(),
                "al editar no se pierde la clave");
        assertEquals("Pedro Mora Rojas", leido.getNombre());
        assertEquals("2222", ((Funcionario) leido).getTelefono());
    }

    @Test
    @DisplayName("eliminarFuncionario no borra un Administrador")
    void eliminarFuncionarioNoBorraAdmin() {
        assertFalse(service.eliminarFuncionario(admin.getId(), admin));
        assertNotNull(service.buscarUsuarioPorId(admin.getId()));
    }

    @Test
    @DisplayName("No se puede eliminar un funcionario que tiene reservas")
    void noSeEliminaFuncionarioConReservas() {
        Funcionario f = new Funcionario();
        f.setId(7004);
        f.setNombre("Con Reservas");
        service.guardarFuncionario(f, admin);
        Recurso recurso = crearRecurso(crearCategoria("Cat funcionario"));
        crearReserva(service.buscarUsuarioPorId(7004), LocalDate.of(2027, 1, 10),
                LocalTime.of(8, 0), LocalTime.of(9, 0), recurso);

        assertThrows(IllegalStateException.class, () -> service.eliminarFuncionario(7004, admin));
    }

    @Test
    @DisplayName("buscarFuncionariosPorNombre ignora mayúsculas y busca por parte del nombre")
    void buscarPorNombre() {
        List<Funcionario> encontrados = service.buscarFuncionariosPorNombre("PEREZ", admin);
        assertTrue(encontrados.size() >= 2, "Juan Perez y Maria Perez");
    }

    @Test
    @DisplayName("buscarFuncionariosPorNombre: sin coincidencias o texto vacío devuelve lista vacía; solo el admin puede")
    void buscarPorNombreSinResultados() {
        assertTrue(service.buscarFuncionariosPorNombre("zzz-no-existe", admin).isEmpty());
        assertTrue(service.buscarFuncionariosPorNombre("   ", admin).isEmpty());
        assertTrue(service.buscarFuncionariosPorNombre(null, admin).isEmpty());
        assertThrows(SecurityException.class, () -> service.buscarFuncionariosPorNombre("perez", juan));
    }

    // ---------- Categorias ----------

    @Test
    @DisplayName("guardarCategoria autogenera ids consecutivos con formato CAT-000000")
    void guardarCategoriaAutogeneraId() {
        Categoria c1 = crearCategoria("Sala para 10 personas");
        Categoria c2 = crearCategoria("Laptop windows 11");

        assertTrue(c1.getId().matches("CAT-\\d{6}"));
        assertEquals(numeroDe(c1.getId()) + 1, numeroDe(c2.getId()));
    }

    @Test
    @DisplayName("guardarCategoria rechaza descripción vacía")
    void guardarCategoriaRechazaDescripcionVacia() {
        Categoria c = new Categoria();
        c.setDescripcion("   ");
        assertFalse(service.guardarCategoria(c, admin));
    }

    @Test
    @DisplayName("buscarCategoriasPorDescripcion ignora mayúsculas y busca por parte del texto")
    void buscarCategoriasPorDescripcion() {
        Categoria c = crearCategoria("Auditorio Principal");

        List<Categoria> encontradas = service.buscarCategoriasPorDescripcion("AUDITORIO prin", admin);
        assertTrue(encontradas.stream().anyMatch(x -> x.getId().equals(c.getId())));

        List<Categoria> porParte = service.buscarCategoriasPorDescripcion("ditorio", admin);
        assertTrue(porParte.stream().anyMatch(x -> x.getId().equals(c.getId())));

        assertTrue(service.buscarCategoriasPorDescripcion("zzz-no-existe", admin).isEmpty());
        assertTrue(service.buscarCategoriasPorDescripcion("  ", admin).isEmpty());
        assertThrows(SecurityException.class, () -> service.buscarCategoriasPorDescripcion("sala", juan));
    }

    @Test
    @DisplayName("actualizarCategoria modifica la descripción; un id inexistente devuelve false")
    void actualizarCategoria() {
        Categoria c = crearCategoria("Original");
        c.setDescripcion("Modificada");
        assertTrue(service.actualizarCategoria(c, admin));
        assertEquals("Modificada", service.buscarCategoriaPorId(c.getId(), admin).getDescripcion());

        Categoria fantasma = new Categoria();
        fantasma.setId("CAT-999999");
        fantasma.setDescripcion("x");
        assertFalse(service.actualizarCategoria(fantasma, admin));
    }

    @Test
    @DisplayName("listarCategorias sirve a cualquier usuario con sesión y exige sesión")
    void listarCategoriasEsDeLecturaLibre() {
        crearCategoria("Para listar");
        assertFalse(service.listarCategorias(juan).isEmpty());
        assertThrows(SecurityException.class, () -> service.listarCategorias(null));
    }

    @Test
    @DisplayName("No se puede eliminar una categoría que todavía tiene recursos")
    void noSeEliminaCategoriaConRecursos() {
        Categoria c = crearCategoria("Con recursos");
        crearRecurso(c);

        assertThrows(IllegalStateException.class, () -> service.eliminarCategoria(c.getId(), admin));

        Categoria vacia = crearCategoria("Sin recursos");
        assertTrue(service.eliminarCategoria(vacia.getId(), admin));
        assertNull(service.buscarCategoriaPorId(vacia.getId(), admin));
    }

    // ---------- Recursos ----------

    @Test
    @DisplayName("guardarRecurso respeta el id manual y lo vincula a su categoría")
    void guardarRecursoConCategoria() {
        Categoria cat = crearCategoria("Laptop windows 11");
        Recurso r = crearRecurso(cat);

        Recurso leido = service.buscarRecursoPorId(r.getId(), admin);
        assertEquals(r.getId(), leido.getId());
        assertEquals(cat.getId(), leido.getCategoria().getId());
        assertEquals("Laptop windows 11", leido.getCategoria().getDescripcion());
    }

    @Test
    @DisplayName("guardarRecurso rechaza un id ya usado, aunque sea de OTRA categoría")
    void idDeRecursoEsUnicoEnTodasLasCategorias() {
        Recurso r1 = crearRecurso(crearCategoria("Cat A"));

        Recurso r2 = new Recurso();
        r2.setId(r1.getId());
        r2.setCategoria(crearCategoria("Cat B"));
        r2.setDescripcion("Mismo id, otra categoría");

        assertFalse(service.guardarRecurso(r2, admin));
        assertEquals("Recurso de prueba", service.buscarRecursoPorId(r1.getId(), admin).getDescripcion(),
                "el recurso original no se debe pisar");
    }

    @Test
    @DisplayName("buscarRecursosPorCategoria solo trae los de esa categoría")
    void buscarRecursosPorCategoria() {
        Categoria salas = crearCategoria("Salas");
        Categoria laptops = crearCategoria("Laptops");
        crearRecurso(salas);
        Recurso laptop = crearRecurso(laptops);

        List<Recurso> soloLaptops = service.buscarRecursosPorCategoria(laptops, juan);
        assertEquals(1, soloLaptops.size());
        assertEquals(laptop.getId(), soloLaptops.get(0).getId());
    }

    @Test
    @DisplayName("actualizarRecurso puede cambiar su categoría y descripción")
    void actualizarRecurso() {
        Recurso r = crearRecurso(crearCategoria("Antes"));
        Categoria despues = crearCategoria("Después");
        r.setCategoria(despues);
        r.setDescripcion("Otra descripción");

        assertTrue(service.actualizarRecurso(r, admin));
        Recurso leido = service.buscarRecursoPorId(r.getId(), admin);
        assertEquals(despues.getId(), leido.getCategoria().getId());
        assertEquals("Otra descripción", leido.getDescripcion());
    }

    // ---------- Reservas y disponibilidad (lo más delicado del proyecto) ----------

    @Test
    @DisplayName("guardarReserva autogenera el id RES-000000, queda ACTIVA y se lee completa")
    void guardarReservaYLeerla() {
        Categoria sala = crearCategoria("Sala reservada");
        Categoria laptop = crearCategoria("Laptop reservada");
        Recurso r1 = crearRecurso(sala);
        Recurso r2 = crearRecurso(laptop);

        Reserva reserva = crearReserva(juan, LocalDate.of(2027, 2, 14), LocalTime.of(9, 0), LocalTime.of(11, 30), r1, r2);

        assertTrue(reserva.getId().matches("RES-\\d{6}"));
        Reserva leida = service.buscarReservaPorId(reserva.getId());
        assertEquals(Reserva.ACTIVA, leida.getEstado());
        assertEquals(LocalDate.of(2027, 2, 14), leida.getFecha());
        assertEquals(LocalTime.of(9, 0), leida.getHoraInicio());
        assertEquals(LocalTime.of(11, 30), leida.getHoraFin());
        assertEquals("Juan Perez", leida.getFuncionario().getNombre());
        assertNull(leida.getFuncionario().getClave(), "la clave no debe viajar dentro de la reserva");
        assertEquals(2, leida.getRecursos().size());
        assertTrue(leida.getRecursos().stream().anyMatch(x -> x.getCategoria().getDescripcion().equals("Sala reservada")));
    }

    @Test
    @DisplayName("existeSolape detecta un horario que se cruza en el mismo recurso y fecha")
    void existeSolapeDetectaCruce() {
        Recurso recurso = crearRecurso(crearCategoria("Solape"));
        LocalDate fecha = LocalDate.of(2027, 3, 1);
        crearReserva(juan, fecha, LocalTime.of(9, 0), LocalTime.of(11, 0), recurso);

        assertTrue(service.existeSolape(recurso, fecha, LocalTime.of(10, 0), LocalTime.of(12, 0), null),
                "10:00-12:00 se cruza con una reserva de 9:00-11:00");
    }

    @Test
    @DisplayName("existeSolape es false si empieza justo cuando termina la otra, en otra fecha o con otro recurso")
    void existeSolapeNoDetectaLoQueNoSeCruza() {
        Categoria cat = crearCategoria("Sin solape");
        Recurso recurso = crearRecurso(cat);
        Recurso otro = crearRecurso(cat);
        LocalDate fecha = LocalDate.of(2027, 3, 2);
        crearReserva(juan, fecha, LocalTime.of(9, 0), LocalTime.of(11, 0), recurso);

        assertFalse(service.existeSolape(recurso, fecha, LocalTime.of(11, 0), LocalTime.of(12, 0), null));
        assertFalse(service.existeSolape(recurso, fecha.plusDays(1), LocalTime.of(9, 0), LocalTime.of(11, 0), null));
        assertFalse(service.existeSolape(otro, fecha, LocalTime.of(9, 0), LocalTime.of(11, 0), null));
    }

    @Test
    @DisplayName("existeSolape puede excluir la reserva que se está modificando")
    void existeSolapeExcluyeReserva() {
        Recurso recurso = crearRecurso(crearCategoria("Excluir"));
        LocalDate fecha = LocalDate.of(2027, 3, 3);
        Reserva reserva = crearReserva(juan, fecha, LocalTime.of(9, 0), LocalTime.of(11, 0), recurso);

        assertTrue(service.existeSolape(recurso, fecha, LocalTime.of(9, 0), LocalTime.of(11, 0), null));
        assertFalse(service.existeSolape(recurso, fecha, LocalTime.of(9, 0), LocalTime.of(11, 0), reserva.getId()));
    }

    @Test
    @DisplayName("guardarReserva rechaza un recurso que otro usuario acaba de tomar (carrera entre dos Frontend)")
    void guardarReservaRechazaSolape() {
        Recurso recurso = crearRecurso(crearCategoria("Carrera"));
        LocalDate fecha = LocalDate.of(2027, 3, 4);
        crearReserva(juan, fecha, LocalTime.of(9, 0), LocalTime.of(11, 0), recurso);

        Reserva segunda = new Reserva();
        segunda.setActividad("Llegó tarde");
        segunda.setFecha(fecha);
        segunda.setHoraInicio(LocalTime.of(10, 0));
        segunda.setHoraFin(LocalTime.of(12, 0));
        segunda.setFuncionario(service.buscarUsuarioPorIdentificacion("222"));
        segunda.setRecursos(new ArrayList<>(List.of(recurso)));

        assertThrows(IllegalStateException.class, () -> service.guardarReserva(segunda));
        assertNull(segunda.getId(), "no se debe haber guardado");
    }

    @Test
    @DisplayName("Cancelar una reserva libera el recurso para ese horario")
    void cancelarReservaLiberaElRecurso() {
        Recurso recurso = crearRecurso(crearCategoria("Cancelar"));
        LocalDate fecha = LocalDate.of(2027, 3, 5);
        Reserva reserva = crearReserva(juan, fecha, LocalTime.of(9, 0), LocalTime.of(11, 0), recurso);

        reserva.setEstado(Reserva.CANCELADA);
        assertTrue(service.actualizarReserva(reserva));

        assertFalse(service.existeSolape(recurso, fecha, LocalTime.of(9, 0), LocalTime.of(11, 0), null),
                "una reserva cancelada no debe bloquear el horario");
        assertEquals(Reserva.CANCELADA, service.buscarReservaPorId(reserva.getId()).getEstado());
    }

    @Test
    @DisplayName("listarReservasPorFuncionario solo trae las de ese funcionario")
    void listarReservasPorFuncionario() {
        Recurso recurso = crearRecurso(crearCategoria("Por funcionario"));
        Usuario maria = service.buscarUsuarioPorIdentificacion("222");
        Reserva deMaria = crearReserva(maria, LocalDate.of(2027, 3, 6), LocalTime.of(9, 0), LocalTime.of(10, 0), recurso);

        List<Reserva> deLaMaria = service.listarReservasPorFuncionario(maria.getId());
        assertTrue(deLaMaria.stream().anyMatch(r -> r.getId().equals(deMaria.getId())));
        assertTrue(deLaMaria.stream().allMatch(r -> r.getFuncionario().getId() == maria.getId()));
    }

    @Test
    @DisplayName("eliminarReserva la borra junto con sus recursos asignados")
    void eliminarReserva() {
        Recurso recurso = crearRecurso(crearCategoria("Eliminar"));
        Reserva reserva = crearReserva(juan, LocalDate.of(2027, 3, 7), LocalTime.of(9, 0), LocalTime.of(10, 0), recurso);

        assertTrue(service.eliminarReserva(reserva.getId()));
        assertNull(service.buscarReservaPorId(reserva.getId()));
        assertFalse(service.eliminarReserva(reserva.getId()));
        assertTrue(service.eliminarRecurso(recurso.getId(), admin), "ya no está en ninguna reserva");
    }

    @Test
    @DisplayName("Los oyentes de 'nueva reserva' se enteran al guardar (así se arman las alertas)")
    void oyenteDeNuevaReserva() {
        List<Reserva> recibidas = new ArrayList<>();
        service.agregarOyenteNuevaReserva(recibidas::add);

        Recurso recurso = crearRecurso(crearCategoria("Oyente"));
        Reserva reserva = crearReserva(juan, LocalDate.of(2027, 3, 8), LocalTime.of(9, 0), LocalTime.of(10, 0), recurso);

        assertEquals(1, recibidas.size());
        assertEquals(reserva.getId(), recibidas.get(0).getId());
        assertEquals(1, recibidas.get(0).getRecursos().size());
    }
}
