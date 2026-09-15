package reserva;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class GeneradorPDF {

    public static void generarDesdeTabla(JTable tabla, String titulo, String nombreArchivo){
        try{
            PdfWriter writer = new PdfWriter(nombreArchivo);
            PdfDocument pdf = new PdfDocument(writer);
            Document documento = new Document(pdf);

            documento.add(new Paragraph(titulo));

            int cantColumnas = tabla.getColumnCount();

            Table tablaPDF = new Table(cantColumnas);

            //Encabezados de los pdf
            for(int columnas = 0; columnas < cantColumnas; columnas++){
                String nombreColumna = tabla.getColumnName(columnas);

                tablaPDF.addHeaderCell(new Cell().add(new Paragraph(nombreColumna)));
            }

            //Datos que van en el pdf
            int cantFilas = tabla.getRowCount();

            for(int fila = 0; fila < cantFilas; fila++){

                for(int columna = 0; columna < cantColumnas; columna++){
                    Object valor = tabla.getValueAt(fila, columna);

                    String texto;

                    if(valor == null){
                        texto = "";
                    }else{
                        texto = valor.toString();
                    }

                    tablaPDF.addCell(new Cell().add(new Paragraph(texto)));
                }
            }
            documento.add(tablaPDF);
            documento.close();

            abrirPDF(nombreArchivo);
        }catch (Exception e){
            e.printStackTrace();

            JOptionPane.showMessageDialog(null, "No se genero el PDF: " + e.getMessage());
        }
    }

    private static void abrirPDF(String nombreArchivo){
        try{
            File archivo = new File(nombreArchivo);

            if(archivo.exists()){
                if(Desktop.isDesktopSupported()){
                    Desktop.getDesktop().open(archivo);
                }else{
                    JOptionPane.showMessageDialog(null, "No se ha abierto automaticamente el archivo");
                }
            }
        }catch (Exception e){
            JOptionPane.showMessageDialog(null, "El PDF se genero pero no se pudo abrir automaticamente");
        }
    }
}
