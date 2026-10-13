package reserva.red;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reserva.logic.Administrador;
import reserva.logic.Categoria;
import reserva.logic.Service;
import reserva.logic.Usuario;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de UNIDAD del Despachador, sin base de datos ni sockets: se usa un
 * Service falso (java.lang.reflect.Proxy) que contesta lo que la prueba necesita.
 */
@DisplayName("Despachador: Peticion -> llamada al Service -> Respuesta")
class DespachadorTest {

    private final List<String> llamadas = new ArrayList<>();
    private Despachador despachador;

    @BeforeEach
    void setUp() {
        InvocationHandler falso = (proxy, metodo, args) -> {
            llamadas.add(metodo.getName());
            switch (metodo.getName()) {
                case "buscarUsuarioPorIdentificacion":
                    Administrador admin = new Administrador();
                    admin.setIdentificacion((String) args[0]);
                    return admin;
                case "listarCategorias":
                    if (args[0] == null) {
                        throw new SecurityException("Debe haber una sesión activa.");
                    }
                    Categoria c = new Categoria();
                    c.setId("CAT-000001");
                    return new ArrayList<>(List.of(c));
                case "guardarCategoria":
                    ((Categoria) args[0]).setId("CAT-000042"); // como hace el ServiceImpl real
                    return true;
                case "eliminarUsuario":
                    throw new NullPointerException("falla inesperada");
                default:
                    return null;
            }
        };
        Service service = (Service) Proxy.newProxyInstance(
                Service.class.getClassLoader(), new Class<?>[]{Service.class}, falso);
        despachador = new Despachador(service);
    }

    @Test
    @DisplayName("Invoca el método del Service con los parámetros recibidos y devuelve su resultado")
    void invocaElMetodo() {
        Respuesta r = despachador.despachar(new Peticion("buscarUsuarioPorIdentificacion", "admin"));

        assertTrue(r.isExito());
        assertEquals("admin", ((Usuario) r.getResultado()).getIdentificacion());
        assertEquals(List.of("buscarUsuarioPorIdentificacion"), llamadas);
    }

    @Test
    @DisplayName("En los guardar* la Respuesta trae de vuelta la entidad con el id que le puso el Backend")
    void guardarDevuelveLaEntidadActualizada() {
        Categoria nueva = new Categoria();
        nueva.setDescripcion("Sala");

        Respuesta r = despachador.despachar(new Peticion("guardarCategoria", nueva, new Administrador()));

        assertTrue(r.isExito());
        assertEquals(true, r.getResultado());
        assertEquals("CAT-000042", ((Categoria) r.getEntidadActualizada()).getId());
    }

    @Test
    @DisplayName("Los demás métodos no devuelven entidad actualizada")
    void otrosMetodosNoDevuelvenEntidad() {
        Respuesta r = despachador.despachar(new Peticion("buscarUsuarioPorIdentificacion", "admin"));

        assertNull(r.getEntidadActualizada());
    }

    @Test
    @DisplayName("Una SecurityException del Service llega como error de tipo SecurityException con su mensaje")
    void securityExceptionConservaSuTipo() {
        Respuesta r = despachador.despachar(new Peticion("listarCategorias", (Object) null));

        assertFalse(r.isExito());
        assertEquals(SecurityException.class.getName(), r.getTipoError());
        assertEquals("Debe haber una sesión activa.", r.getMensajeError());
    }

    @Test
    @DisplayName("Un error inesperado se reporta como RuntimeException genérica")
    void errorInesperadoEsRuntimeException() {
        Respuesta r = despachador.despachar(new Peticion("eliminarUsuario", 5));

        assertFalse(r.isExito());
        assertEquals(RuntimeException.class.getName(), r.getTipoError());
    }

    @Test
    @DisplayName("Una operación que no existe en Service se rechaza sin ejecutar nada")
    void operacionDesconocida() {
        Respuesta r = despachador.despachar(new Peticion("borrarTodo"));

        assertFalse(r.isExito());
        assertTrue(r.getMensajeError().contains("borrarTodo"));
        assertTrue(llamadas.isEmpty());
    }

    @Test
    @DisplayName("Un método de Object (hashCode, toString...) tampoco se puede invocar desde afuera")
    void noSeInvocanMetodosDeObject() {
        Respuesta r = despachador.despachar(new Peticion("hashCode"));

        assertFalse(r.isExito());
        assertTrue(llamadas.isEmpty());
    }

    @Test
    @DisplayName("La cantidad de parámetros tiene que coincidir con la del método")
    void cantidadDeParametrosIncorrecta() {
        Respuesta r = despachador.despachar(new Peticion("buscarUsuarioPorIdentificacion"));

        assertFalse(r.isExito());
        assertTrue(llamadas.isEmpty());
    }
}
