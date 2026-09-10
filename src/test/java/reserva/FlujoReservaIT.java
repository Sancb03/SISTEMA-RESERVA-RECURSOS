package reserva;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import reserva.data.Data;
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
 * Prueba de INTEGRACION: usa Data.instance() (el singleton real, con su
 * archivo XML de verdad -- ver "reserva.data.archivo" en el pom.xml para
 * Failsafe, que lo apunta a un archivo aparte de "data/sistema.xml").
 * <p>
 * Simula, en orden, el recorrido completo de un usuario real por el sistema:
 * un administrador da de alta una categoría y un recurso, y luego un
 * funcionario inicia sesión, hace una reserva, intenta chocar con ella y
 * finalmente la cancela.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FlujoReservaIT {

    private static Categoria categoriaCreada;
    private static Recurso recursoCreado;
    private static Reserva reservaCreada;

    @Test
    @Order(1)
    @DisplayName("1) El administrador de prueba puede iniciar sesión")
    void loginAdmin() {
        Usuario admin = Data.instance().buscarUsuarioPorIdentificacion("admin");
        assertNotNull(admin, "El usuario 'admin' debería existir desde el arranque (datos semilla)");
        assertEquals("admin", admin.getClave());
        assertTrue(admin instanceof Administrador);
    }

    @Test
    @Order(2)
    @DisplayName("2) El administrador crea una categoría nueva")
    void adminCreaCategoria() {
        Usuario admin = Data.instance().buscarUsuarioPorIdentificacion("admin");

        Categoria categoria = new Categoria();
        categoria.setDescripcion("Sala de Juntas IT-" + System.nanoTime());

        assertTrue(Data.instance().guardarCategoria(categoria, admin));
        assertNotNull(categoria.getId());

        categoriaCreada = categoria;
    }

    @Test
    @Order(3)
    @DisplayName("3) El administrador crea un recurso en esa categoría")
    void adminCreaRecurso() {
        Usuario admin = Data.instance().buscarUsuarioPorIdentificacion("admin");
        assertNotNull(categoriaCreada, "Depende de que el paso 2 haya corrido antes");

        Recurso recurso = new Recurso();
        recurso.setId("IT-RECURSO-" + System.nanoTime());
        recurso.setCategoria(categoriaCreada);
        recurso.setDescripcion("Recurso creado por la prueba de integración");

        assertTrue(Data.instance().guardarRecurso(recurso, admin));

        recursoCreado = recurso;
    }

    @Test
    @Order(4)
    @DisplayName("4) El funcionario '111' puede iniciar sesión")
    void loginFuncionario() {
        Usuario funcionario = Data.instance().buscarUsuarioPorIdentificacion("111");
        assertNotNull(funcionario, "El funcionario '111' debería existir desde el arranque (datos semilla)");
        assertTrue(funcionario instanceof Funcionario);
    }

    @Test
    @Order(5)
    @DisplayName("5) El funcionario reserva el recurso creado en el paso 3")
    void funcionarioCreaReserva() {
        Usuario funcionario = Data.instance().buscarUsuarioPorIdentificacion("111");
        assertNotNull(recursoCreado, "Depende de que el paso 3 haya corrido antes");

        LocalDate fecha = LocalDate.now().plusDays(1);

        // Antes de reservar, el recurso debe estar libre en ese horario
        assertFalse(Data.instance().existeSolape(recursoCreado, fecha, LocalTime.of(9, 0), LocalTime.of(11, 0), null));

        Reserva reserva = new Reserva();
        reserva.setActividad("Reunion de prueba de integracion");
        reserva.setFecha(fecha);
        reserva.setHoraInicio(LocalTime.of(9, 0));
        reserva.setHoraFin(LocalTime.of(11, 0));
        reserva.setFuncionario(funcionario);
        reserva.setRecursos(List.of(recursoCreado));

        assertTrue(Data.instance().guardarReserva(reserva));
        assertNotNull(reserva.getId());
        assertEquals(Reserva.ACTIVA, reserva.getEstado());

        reservaCreada = reserva;
    }

    @Test
    @Order(6)
    @DisplayName("6) La reserva aparece en \"Mis reservas\" del funcionario")
    void reservaApareceEnMisReservas() {
        Usuario funcionario = Data.instance().buscarUsuarioPorIdentificacion("111");

        List<Reserva> misReservas = Data.instance().listarReservasPorFuncionario(funcionario.getId());
        boolean estaLaReserva = misReservas.stream().anyMatch(r -> r.getId().equals(reservaCreada.getId()));
        assertTrue(estaLaReserva);
    }

    @Test
    @Order(7)
    @DisplayName("7) Ese mismo recurso y horario ya no está disponible para otra reserva")
    void recursoYaNoDisponibleParaOtraReserva() {
        boolean solapa = Data.instance().existeSolape(recursoCreado, reservaCreada.getFecha(),
                LocalTime.of(10, 0), LocalTime.of(12, 0), null);
        assertTrue(solapa, "El sistema no debería permitir otra reserva que se cruce con la ya hecha");
    }

    @Test
    @Order(8)
    @DisplayName("8) El funcionario cancela su reserva y el recurso se vuelve a liberar")
    void funcionarioCancelaReserva() {
        reservaCreada.setEstado(Reserva.CANCELADA);
        assertTrue(Data.instance().actualizarReserva(reservaCreada));

        boolean solapaDespuesDeCancelar = Data.instance().existeSolape(recursoCreado, reservaCreada.getFecha(),
                LocalTime.of(9, 0), LocalTime.of(11, 0), null);
        assertFalse(solapaDespuesDeCancelar, "Al cancelar, el recurso debe quedar libre otra vez");
    }
}
