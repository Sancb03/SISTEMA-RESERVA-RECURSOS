package reserva.logic;

import java.io.Serializable;

/**
 * "id o número de activo" (ej. "238715"), su categoría y su descripción.
 * Antes la relación con Categoria era @XmlIDREF; ahora el Backend es
 * quien arma este objeto completo (con la Categoria ya adentro) al leer
 * de MySQL, igual que hace PersonaDao con Departamento en el ejemplo
 * del profe.
 */
public class Recurso implements Serializable {
    private static final long serialVersionUID = 1L;


    private String id;
    private Categoria categoria;
    private String descripcion;

    public Recurso() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
