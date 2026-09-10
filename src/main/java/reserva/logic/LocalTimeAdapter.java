package reserva.logic;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import java.time.LocalTime;

/** Permite que JAXB serialice/deserialice LocalTime (que no sabe manejar de forma nativa). */
public class LocalTimeAdapter extends XmlAdapter<String, LocalTime> {

    @Override
    public LocalTime unmarshal(String v) throws Exception {
        return (v == null || v.isBlank()) ? null : LocalTime.parse(v);
    }

    @Override
    public String marshal(LocalTime v) throws Exception {
        return (v == null) ? null : v.toString();
    }
}
