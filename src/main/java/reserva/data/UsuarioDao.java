package reserva.data;

import reserva.logic.Administrador;
import reserva.logic.Funcionario;
import reserva.logic.Usuario;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class UsuarioDao {
    private static final String ARCHIVO = "data/usuarios.xml";



    public boolean guardar(Usuario usuario) {
        if (usuario == null) return false;
        List<Usuario> usuarios = listar();
        if (buscarPorId(usuarios, usuario.getId()) != null) {
            return false; // ya existe ese id
        }
        usuarios.add(usuario);
        guardarTodos(usuarios);
        return true;
    }

    public boolean actualizar(Usuario usuario) {
        if (usuario == null) return false;
        List<Usuario> usuarios = listar();
        Usuario existente = buscarPorId(usuarios, usuario.getId());
        if (existente == null) return false;

        existente.setIdentificacion(usuario.getIdentificacion());
        existente.setNombre(usuario.getNombre());
        existente.setClave(usuario.getClave());
        if (existente instanceof Funcionario ef && usuario instanceof Funcionario uf) {
            ef.setTelefono(uf.getTelefono());
        }
        guardarTodos(usuarios);
        return true;
    }

    public boolean eliminar(int id) {
        List<Usuario> usuarios = listar();
        boolean eliminado = usuarios.removeIf(u -> u.getId() == id);
        if (eliminado) {
            guardarTodos(usuarios);
        }
        return eliminado;
    }


    public Usuario buscarPorId(int id) {
        return buscarPorId(listar(), id);
    }

    public List<Usuario> listar() {
        List<Usuario> resultado = new ArrayList<>();
        File archivo = new File(ARCHIVO);
        if (!archivo.exists()) {
            return resultado;
        }
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(archivo);
            doc.getDocumentElement().normalize();

            NodeList nodos = doc.getElementsByTagName("usuario");
            for (int i = 0; i < nodos.getLength(); i++) {
                Element el = (Element) nodos.item(i);
                resultado.add(elementoAUsuario(el));
            }
        } catch (Exception e) {
            throw new RuntimeException("Error al leer " + ARCHIVO + ": " + e.getMessage(), e);
        }
        return resultado;
    }


    private Usuario buscarPorId(List<Usuario> usuarios, int id) {
        for (Usuario u : usuarios) {
            if (u.getId() == id) return u;
        }
        return null;
    }

    private void guardarTodos(List<Usuario> usuarios) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();

            Element raiz = doc.createElement("usuarios");
            doc.appendChild(raiz);

            for (Usuario u : usuarios) {
                raiz.appendChild(usuarioAElemento(doc, u));
            }

            File archivo = new File(ARCHIVO);
            archivo.getParentFile().mkdirs();

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            transformer.transform(new DOMSource(doc), new StreamResult(archivo));
        } catch (Exception e) {
            throw new RuntimeException("Error al escribir " + ARCHIVO + ": " + e.getMessage(), e);
        }
    }

    private Element usuarioAElemento(Document doc, Usuario u) {
        Element el = doc.createElement("usuario");
        el.setAttribute("tipo", u.getRol());

        el.appendChild(textoElemento(doc, "id", String.valueOf(u.getId())));
        el.appendChild(textoElemento(doc, "identificacion", u.getIdentificacion()));
        el.appendChild(textoElemento(doc, "nombre", u.getNombre()));
        el.appendChild(textoElemento(doc, "clave", u.getClave()));

        if (u instanceof Funcionario f) {
            el.appendChild(textoElemento(doc, "telefono", f.getTelefono()));
        }
        return el;
    }

    private Element textoElemento(Document doc, String tag, String valor) {
        Element el = doc.createElement(tag);
        el.setTextContent(valor == null ? "" : valor);
        return el;
    }

    private Usuario elementoAUsuario(Element el) {
        String tipo = el.getAttribute("tipo");
        Usuario u = "ADMINISTRADOR".equalsIgnoreCase(tipo) ? new Administrador() : new Funcionario();

        u.setId(Integer.parseInt(textoDe(el, "id")));
        u.setIdentificacion(textoDe(el, "identificacion"));
        u.setNombre(textoDe(el, "nombre"));
        u.setClave(textoDe(el, "clave"));

        if (u instanceof Funcionario f) {
            f.setTelefono(textoDe(el, "telefono"));
        }
        return u;
    }

    private String textoDe(Element padre, String tag) {
        NodeList nodos = padre.getElementsByTagName(tag);
        if (nodos.getLength() == 0) return null;
        return nodos.item(0).getTextContent();
    }



}
