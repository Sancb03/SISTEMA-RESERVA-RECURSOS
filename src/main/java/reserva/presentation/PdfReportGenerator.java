package reserva.presentation;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Genera un PDF simple (título + tabla) sin depender de ninguna librería externa,
 * para que cualquier módulo del proyecto pueda cumplir con "generar reporte en PDF"
 * sin tener que agregar una dependencia nueva a pom.xml.
 * <p>
 * Uso:
 * <pre>
 *   PdfReportGenerator.generar(
 *       archivoDestino,
 *       "Listado de Categorías",
 *       new String[] {"Id", "Descripcion"},
 *       filas // List&lt;String[]&gt;, cada arreglo con el mismo largo que las columnas
 *   );
 * </pre>
 */
public final class PdfReportGenerator {

    private static final Charset PDF_CHARSET = Charset.forName("windows-1252");
    private static final int FONT_SIZE = 10;
    private static final int LEADING = 14;
    private static final int MARGIN_LEFT = 40;
    private static final int MARGIN_TOP = 40;
    private static final int PAGE_WIDTH = 612;  // carta (letter), en puntos
    private static final int PAGE_HEIGHT = 792;
    private static final int FILAS_POR_PAGINA = 40;

    private PdfReportGenerator() {
    }

    public static void generar(File destino, String titulo, String[] columnas, List<String[]> filas) throws IOException {
        int[] anchos = calcularAnchos(columnas, filas);
        List<String> encabezado = construirLineasDeTabla(columnas, anchos);
        List<String> lineasDatos = new ArrayList<>();
        for (String[] fila : filas) {
            lineasDatos.add(formatearFila(fila, anchos));
        }

        List<List<String>> paginas = new ArrayList<>();
        if (lineasDatos.isEmpty()) {
            List<String> pagina = new ArrayList<>(encabezado);
            pagina.add("(sin registros)");
            paginas.add(pagina);
        } else {
            for (int i = 0; i < lineasDatos.size(); i += FILAS_POR_PAGINA) {
                List<String> pagina = new ArrayList<>(encabezado);
                pagina.addAll(lineasDatos.subList(i, Math.min(i + FILAS_POR_PAGINA, lineasDatos.size())));
                paginas.add(pagina);
            }
        }

        byte[] pdf = construirPdf(titulo, paginas);
        try (FileOutputStream out = new FileOutputStream(destino)) {
            out.write(pdf);
        }
    }

    private static int[] calcularAnchos(String[] columnas, List<String[]> filas) {
        int n = columnas.length;
        int[] anchos = new int[n];
        for (int i = 0; i < n; i++) {
            anchos[i] = columnas[i] != null ? columnas[i].length() : 0;
        }
        for (String[] fila : filas) {
            for (int i = 0; i < n && i < fila.length; i++) {
                String valor = fila[i] != null ? fila[i] : "";
                anchos[i] = Math.max(anchos[i], valor.length());
            }
        }
        return anchos;
    }

    /** Encabezado + línea separadora, alineados con ancho fijo (fuente Courier). */
    private static List<String> construirLineasDeTabla(String[] columnas, int[] anchos) {
        List<String> lineas = new ArrayList<>();
        lineas.add(formatearFila(columnas, anchos));
        StringBuilder separador = new StringBuilder();
        for (int ancho : anchos) {
            separador.append("-".repeat(ancho)).append("  ");
        }
        lineas.add(separador.toString());
        return lineas;
    }

    private static String formatearFila(String[] valores, int[] anchos) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < anchos.length; i++) {
            String valor = (valores != null && i < valores.length && valores[i] != null) ? valores[i] : "";
            sb.append(valor);
            sb.append(" ".repeat(Math.max(0, anchos[i] - valor.length())));
            sb.append("  ");
        }
        return sb.toString();
    }

    private static byte[] construirPdf(String titulo, List<List<String>> paginas) throws IOException {
        ByteArrayOutputStream pdf = new ByteArrayOutputStream();
        List<Integer> offsets = new ArrayList<>(); // offsets[objNum - 1]

        int numPaginas = paginas.size();
        int catalogoObj = 1;
        int paginasObj = 2;
        int fuenteObj = 3;
        int primeraPaginaObj = 4; // cada página usa 2 objetos: página y su stream de contenido

        escribir(pdf, "%PDF-1.4\n");

        // 1) Catálogo
        marcarOffset(pdf, offsets, catalogoObj);
        escribir(pdf, catalogoObj + " 0 obj\n<< /Type /Catalog /Pages " + paginasObj + " 0 R >>\nendobj\n");

        // 2) Árbol de páginas
        StringBuilder kids = new StringBuilder();
        for (int i = 0; i < numPaginas; i++) {
            int pageObjNum = primeraPaginaObj + i * 2;
            kids.append(pageObjNum).append(" 0 R ");
        }
        marcarOffset(pdf, offsets, paginasObj);
        escribir(pdf, paginasObj + " 0 obj\n<< /Type /Pages /Kids [" + kids.toString().trim() + "] /Count " + numPaginas + " >>\nendobj\n");

        // 3) Fuente (Courier, con acentos vía WinAnsiEncoding)
        marcarOffset(pdf, offsets, fuenteObj);
        escribir(pdf, fuenteObj + " 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Courier /Encoding /WinAnsiEncoding >>\nendobj\n");

        // 4) Cada página + su contenido
        for (int i = 0; i < numPaginas; i++) {
            int pageObjNum = primeraPaginaObj + i * 2;
            int contentObjNum = pageObjNum + 1;

            String contenido = construirContenidoPagina(titulo, paginas.get(i), i + 1, numPaginas);
            byte[] contenidoBytes = contenido.getBytes(PDF_CHARSET);

            marcarOffset(pdf, offsets, pageObjNum);
            escribir(pdf, pageObjNum + " 0 obj\n<< /Type /Page /Parent " + paginasObj + " 0 R "
                    + "/Resources << /Font << /F1 " + fuenteObj + " 0 R >> >> "
                    + "/MediaBox [0 0 " + PAGE_WIDTH + " " + PAGE_HEIGHT + "] "
                    + "/Contents " + contentObjNum + " 0 R >>\nendobj\n");

            marcarOffset(pdf, offsets, contentObjNum);
            escribir(pdf, contentObjNum + " 0 obj\n<< /Length " + contenidoBytes.length + " >>\nstream\n");
            pdf.write(contenidoBytes);
            escribir(pdf, "\nendstream\nendobj\n");
        }

        int totalObjetos = fuenteObj + numPaginas * 2; // catalogo(1) + paginas(1) + fuente(1) + paginas*2
        int xrefOffset = pdf.size();

        escribir(pdf, "xref\n0 " + (totalObjetos + 1) + "\n");
        escribir(pdf, "0000000000 65535 f \n");
        for (int i = 0; i < totalObjetos; i++) {
            escribir(pdf, String.format("%010d 00000 n \n", offsets.get(i)));
        }

        escribir(pdf, "trailer\n<< /Size " + (totalObjetos + 1) + " /Root " + catalogoObj + " 0 R >>\n");
        escribir(pdf, "startxref\n" + xrefOffset + "\n%%EOF");

        return pdf.toByteArray();
    }

    private static String construirContenidoPagina(String titulo, List<String> lineas, int numPagina, int totalPaginas) {
        StringBuilder sb = new StringBuilder();
        sb.append("BT\n");
        sb.append("/F1 13 Tf\n");
        int y = PAGE_HEIGHT - MARGIN_TOP;
        sb.append(MARGIN_LEFT).append(' ').append(y).append(" Td\n");
        sb.append('(').append(escapar(titulo)).append(") Tj\n");

        sb.append("/F1 ").append(FONT_SIZE).append(" Tf\n");
        int salto = LEADING + 10; // espacio extra entre el título y la tabla
        sb.append("0 -").append(salto).append(" Td\n");

        for (int i = 0; i < lineas.size(); i++) {
            if (i > 0) {
                sb.append("0 -").append(LEADING).append(" Td\n");
            }
            sb.append('(').append(escapar(lineas.get(i))).append(") Tj\n");
        }
        sb.append("ET\n");

        if (totalPaginas > 1) {
            sb.append("BT\n/F1 8 Tf\n");
            sb.append(PAGE_WIDTH - MARGIN_LEFT - 60).append(' ').append(20).append(" Td\n");
            sb.append('(').append("Pagina ").append(numPagina).append(" de ").append(totalPaginas).append(") Tj\n");
            sb.append("ET\n");
        }
        return sb.toString();
    }

    private static String escapar(String texto) {
        if (texto == null) {
            return "";
        }
        return texto.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)")
                .replace("\r", " ").replace("\n", " ");
    }

    private static void marcarOffset(ByteArrayOutputStream pdf, List<Integer> offsets, int objNum) {
        while (offsets.size() < objNum) {
            offsets.add(0);
        }
        offsets.set(objNum - 1, pdf.size());
    }

    private static void escribir(ByteArrayOutputStream pdf, String texto) throws IOException {
        pdf.write(texto.getBytes(PDF_CHARSET));
    }
}
