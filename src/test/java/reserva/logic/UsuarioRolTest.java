package reserva.logic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UsuarioRolTest {

    @Test
    void administradorTieneRolAdministrador() {
        Usuario u = new Administrador();
        assertEquals("ADMINISTRADOR", u.getRol());
        assertTrue(u instanceof Usuario);
    }

    @Test
    void funcionarioTieneRolFuncionario() {
        Usuario u = new Funcionario();
        assertEquals("FUNCIONARIO", u.getRol());
    }

    @Test
    void identificacionYClaveSePuedenAsignar() {
        Funcionario f = new Funcionario();
        f.setId(111);
        f.setIdentificacion("111");
        f.setNombre("Juan Perez");
        f.setClave("111");
        f.setTelefono("3323");

        assertEquals(111, f.getId());
        assertEquals("111", f.getIdentificacion());
        assertEquals("Juan Perez", f.getNombre());
        assertEquals("111", f.getClave());
        assertEquals("3323", f.getTelefono());
    }
}
