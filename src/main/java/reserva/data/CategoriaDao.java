package reserva.data;

import reserva.logic.Administrador;
import reserva.logic.Categoria;
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

public class CategoriaDao {

    private static final String ARCHIVO = "data/categorias.xml";
    private static final String PREFIJO = "CAT-";
    private static final int DIGITOS = 6;

    private void validarAdmin(Usuario usuarioActual) {
        if (!(usuarioActual instanceof Administrador)) {
            throw new SecurityException("Solo un administrador puede gestionar categorías.");
        }
    }

    // ---------- API publica ----------

    public boolean guardar(Categoria categoria, Usuario usuarioActual) {
        validarAdmin(usuarioActual);
        if (categoria == null || categoria.getDescripcion() == null || categoria.getDescripcion().isBlank()) {
            return false;
        }

        List<Categoria> categorias = listarInterno();
        categoria.setId(generarSiguienteId(categorias));
        categorias.add(categoria);
        guardarTodos(categorias);
        return true;
    }

    public boolean actualizar(Categoria categoria, Usuario usuarioActual) {
        validarAdmin(usuarioActual);
        if (categoria == null || categoria.getId() == null) return false;

        List<Categoria> categorias = listarInterno();
        Categoria existente = buscarPorId(categorias, categoria.getId());
        if (existente == null) return false;

        existente.setDescripcion(categoria.getDescripcion());
        guardarTodos(categorias);
        return true;
    }

    public boolean eliminar(String id, Usuario usuarioActual) {
        validarAdmin(usuarioActual);
        List<Categoria> categorias = listarInterno();
        boolean eliminado = categorias.removeIf(c -> c.getId().equals(id));
        if (eliminado) {
            guardarTodos(categorias);
        }
        return eliminado;
    }

    public Categoria buscarPorId(String id, Usuario usuarioActual) {
        validarAdmin(usuarioActual);
        return buscarPorId(listarInterno(), id);
    }

    public List<Categoria> buscarPorDescripcion(String texto, Usuario usuarioActual) {
        validarAdmin(usuarioActual);
        List<Categoria> resultado = new ArrayList<>();
        if (texto == null || texto.isBlank()) return resultado;
        String buscado = texto.toLowerCase();
        for (Categoria c : listarInterno()) {
            if (c.getDescripcion() != null && c.getDescripcion().toLowerCase().contains(buscado)) {
                resultado.add(c);
            }
        }
        return resultado;
    }

    public List<Categoria> listar(Usuario usuarioActual) {
        validarAdmin(usuarioActual);
        return listarInterno();
    }

    // ---------- Helpers internos ----------

    private Categoria buscarPorId(List<Categoria> categorias, String id) {
        for (Categoria c : categorias) {
            if (c.getId().equals(id)) return c;
        }
        return null;
    }

    private String generarSiguienteId(List<Categoria> categorias) {
        int maxNumero = 0;
        for (Categoria c : categorias) {
            String id = c.getId();
            if (id != null && id.startsWith(PREFIJO)) {
                try {
                    int numero = Integer.parseInt(id.substring(PREFIJO.length()));
                    maxNumero = Math.max(maxNumero, numero);
                } catch (NumberFormatException ignored) {
                    // id con formato inesperado, se ignora para el calculo del siguiente numero
                }
            }
        }
        return PREFIJO + String.format("%0" + DIGITOS + "d", maxNumero + 1);
    }

    private List<Categoria> listarInterno() {
        List<Categoria> resultado = new ArrayList<>();
        File archivo = new File(ARCHIVO);
        if (!archivo.exists()) {
            return resultado;
        }
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(archivo);
            doc.getDocumentElement().normalize();

            NodeList nodos = doc.getElementsByTagName("categoria");
            for (int i = 0; i < nodos.getLength(); i++) {
                Element el = (Element) nodos.item(i);
                Categoria c = new Categoria();
                c.setId(textoDe(el, "id"));
                c.setDescripcion(textoDe(el, "descripcion"));
                resultado.add(c);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error al leer " + ARCHIVO + ": " + e.getMessage(), e);
        }
        return resultado;
    }

    private void guardarTodos(List<Categoria> categorias) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();

            Element raiz = doc.createElement("categorias");
            doc.appendChild(raiz);

            for (Categoria c : categorias) {
                Element el = doc.createElement("categoria");
                el.appendChild(textoElemento(doc, "id", c.getId()));
                el.appendChild(textoElemento(doc, "descripcion", c.getDescripcion()));
                raiz.appendChild(el);
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

    private Element textoElemento(Document doc, String tag, String valor) {
        Element el = doc.createElement(tag);
        el.setTextContent(valor == null ? "" : valor);
        return el;
    }

    private String textoDe(Element padre, String tag) {
        NodeList nodos = padre.getElementsByTagName(tag);
        if (nodos.getLength() == 0) return null;
        return nodos.item(0).getTextContent();
    }
}
