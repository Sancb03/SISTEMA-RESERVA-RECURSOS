package reserva.logic;

import java.io.Serializable;

/**
 * Ya NO lleva anotaciones JAXB: en el Proyecto 2 la persistencia la hace
 * el Backend contra MySQL (ver reserva.data.UsuarioDao allá), no XML.
 * Ahora es Serializable porque estos objetos viajan por el socket dentro
 * de una Peticion/Respuesta.
 */
public abstract class Usuario implements Serializable {
    private int id;
    private String identificacion;
    private String nombre;
    private String clave;

    protected Usuario() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIdentificacion() {
        return identificacion;
    }

    public void setIdentificacion(String identificacion) {
        this.identificacion = identificacion;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }

    public abstract String getRol();
}
