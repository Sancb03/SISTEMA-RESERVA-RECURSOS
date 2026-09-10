package reserva.data;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/** Guarda/lee un objeto Data completo en un archivo XML, vía JAXB. */
public class XmlPersister {

    private final String path;

    public XmlPersister(String path) {
        this.path = path;
    }

    public Data load() throws Exception {
        File archivo = new File(path);
        if (!archivo.exists()) {
            return new Data();
        }
        JAXBContext jaxbContext = JAXBContext.newInstance(Data.class);
        try (FileInputStream is = new FileInputStream(archivo)) {
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            return (Data) unmarshaller.unmarshal(is);
        }
    }

    public void store(Data data) throws Exception {
        File archivo = new File(path);
        File carpeta = archivo.getParentFile();
        if (carpeta != null) {
            carpeta.mkdirs();
        }
        JAXBContext jaxbContext = JAXBContext.newInstance(Data.class);
        try (FileOutputStream os = new FileOutputStream(archivo)) {
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(data, os);
        }
    }
}
