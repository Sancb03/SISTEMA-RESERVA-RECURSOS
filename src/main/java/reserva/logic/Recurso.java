package reserva.logic;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlID;
import jakarta.xml.bind.annotation.XmlIDREF;

/**
 * "id o número de activo" según el enunciado (ej. "238715"), su categoría
 * (referencia real a una Categoria, no solo texto) y su descripción.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Recurso {

    @XmlID
    private String id;

    @XmlIDREF
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
