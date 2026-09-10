package reserva.presentation.Reservas;

import reserva.data.Data;
import reserva.logic.Categoria;
import reserva.logic.Recurso;
import reserva.logic.Reserva;
import reserva.logic.Sesion;
import reserva.logic.Usuario;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

class ReservasController {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm");

    private final ReservasView view;
    private final ReservasModel model;
    private final TablaModel tableModel;

    ReservasController(ReservasView view) {
        this(view, new ReservasModel(), new TablaModel());
    }

    ReservasController(ReservasView view, ReservasModel model, TablaModel tableModel) {
        this.view = view;
        this.model = model;
        this.tableModel = tableModel;
    }

    ReservasView getView() {
        return view;
    }

    ReservasModel getModel() {
        return model;
    }

    TablaModel getTableModel() {
        return tableModel;
    }

    private Usuario usuarioActual() throws Exception {
        Usuario usuario = Sesion.getUsuarioActual();
        if (usuario == null) {
            throw new Exception("No hay una sesión activa. Debe iniciar sesión de nuevo.");
        }
        return usuario;
    }

    /** Carga las categorías disponibles para mostrarlas en la lista de selección. */
    void cargarCategorias() throws Exception {
        model.setCategorias(Data.instance().listarCategorias(usuarioActual()));
    }

    /** Carga y refresca "Mis reservas" para el funcionario que tiene la sesión activa. */
    void cargarMisReservas() throws Exception {
        Usuario usuario = usuarioActual();
        List<Reserva> reservas = Data.instance().listarReservasPorFuncionario(usuario.getId());
        model.setMisReservas(reservas);
        tableModel.setFilas(construirFilas(reservas));
    }

    private List<Object[]> construirFilas(List<Reserva> reservas) {
        List<Object[]> filas = new ArrayList<>();
        for (Reserva r : reservas) {
            String horario = r.getHoraInicio().format(FORMATO_HORA) + " - " + r.getHoraFin().format(FORMATO_HORA);
            String recursos = nombresDeRecursos(r.getRecursos());
            filas.add(new Object[]{
                    r.getId(),
                    r.getActividad(),
                    r.getFecha().format(FORMATO_FECHA),
                    horario,
                    recursos,
                    r.getEstado()
            });
        }
        return filas;
    }

    private String nombresDeRecursos(List<Recurso> recursos) {
        StringBuilder sb = new StringBuilder();
        for (Recurso recurso : recursos) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(recurso.getId());
        }
        return sb.toString();
    }

    /**
     * Intenta crear la reserva. Si alguna categoría solicitada no tiene ningún
     * recurso disponible en ese horario, no guarda nada y lanza Exception
     * indicando cuáles categorías no tienen disponibilidad (para que el
     * funcionario ajuste la reserva e intente de nuevo).
     * <p>
     * 'recursosElegidosManualmente' son los recursos concretos que el funcionario
     * escogió de la lista de disponibles (puede venir vacía): para cada categoría
     * solicitada, si escogió uno de esa categoría se usa ese; si no, se asigna
     * automáticamente el primero disponible, tal como pide el enunciado.
     */
    void reservar(String actividad, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin,
                  List<Categoria> categoriasSeleccionadas, List<Recurso> recursosElegidosManualmente) throws Exception {

        Usuario usuario = usuarioActual();

        if (actividad == null || actividad.isBlank()) {
            throw new Exception("Debe indicar la actividad.");
        }
        if (fecha == null) {
            throw new Exception("Debe indicar la fecha.");
        }
        if (horaInicio == null || horaFin == null) {
            throw new Exception("Debe indicar la hora de inicio y la hora de fin.");
        }
        if (!horaInicio.isBefore(horaFin)) {
            throw new Exception("La hora de inicio debe ser antes que la hora de fin.");
        }
        if (fecha.isBefore(LocalDate.now())) {
            throw new Exception("La fecha no puede ser en el pasado.");
        }
        if (categoriasSeleccionadas == null || categoriasSeleccionadas.isEmpty()) {
            throw new Exception("Debe seleccionar al menos una categoría de recurso.");
        }

        List<Recurso> todosLosRecursos = Data.instance().listarRecursos(usuario);
        List<Recurso> elegidos = (recursosElegidosManualmente != null) ? recursosElegidosManualmente : List.of();

        List<Recurso> recursosAsignados = new ArrayList<>();
        List<String> categoriasNoDisponibles = new ArrayList<>();

        for (Categoria categoria : categoriasSeleccionadas) {
            Recurso elegidoParaEstaCategoria = elegidos.stream()
                    .filter(r -> r.getCategoria() != null && r.getCategoria().getId().equals(categoria.getId()))
                    .findFirst()
                    .orElse(null);

            Recurso disponible;
            if (elegidoParaEstaCategoria != null) {
                boolean sigueLibre = !Data.instance().existeSolape(elegidoParaEstaCategoria, fecha, horaInicio, horaFin, null);
                disponible = sigueLibre ? elegidoParaEstaCategoria : null;
            } else {
                disponible = buscarRecursoDisponible(todosLosRecursos, categoria, fecha, horaInicio, horaFin);
            }

            if (disponible == null) {
                categoriasNoDisponibles.add(categoria.getDescripcion());
            } else {
                recursosAsignados.add(disponible);
            }
        }

        if (!categoriasNoDisponibles.isEmpty()) {
            throw new Exception("No hay disponibilidad para: " + String.join(", ", categoriasNoDisponibles)
                    + ". Ajuste la reserva e intente de nuevo.");
        }

        Reserva reserva = new Reserva();
        reserva.setActividad(actividad.trim());
        reserva.setFecha(fecha);
        reserva.setHoraInicio(horaInicio);
        reserva.setHoraFin(horaFin);
        reserva.setFuncionario(usuario);
        reserva.setEstado(Reserva.ACTIVA);
        reserva.setRecursos(recursosAsignados);

        Data.instance().guardarReserva(reserva);
        cargarMisReservas();
    }

    /** Todos los recursos libres (de esas categorías, fecha y horario) para que el funcionario escoja, si quiere. */
    List<Recurso> listarRecursosDisponibles(List<Categoria> categorias, LocalDate fecha,
                                            LocalTime horaInicio, LocalTime horaFin) throws Exception {
        Usuario usuario = usuarioActual();
        List<Recurso> todosLosRecursos = Data.instance().listarRecursos(usuario);

        List<Recurso> disponibles = new ArrayList<>();
        for (Categoria categoria : categorias) {
            for (Recurso recurso : todosLosRecursos) {
                boolean esDeEstaCategoria = recurso.getCategoria() != null
                        && recurso.getCategoria().getId().equals(categoria.getId());
                if (!esDeEstaCategoria) {
                    continue;
                }
                boolean ocupado = Data.instance().existeSolape(recurso, fecha, horaInicio, horaFin, null);
                if (!ocupado) {
                    disponibles.add(recurso);
                }
            }
        }
        return disponibles;
    }

    /** Busca, entre los recursos de esa categoría, el primero que no tenga choque de horario. */
    private Recurso buscarRecursoDisponible(List<Recurso> todosLosRecursos, Categoria categoria,
                                            LocalDate fecha, LocalTime horaInicio, LocalTime horaFin) {
        for (Recurso recurso : todosLosRecursos) {
            boolean esDeEstaCategoria = recurso.getCategoria() != null
                    && recurso.getCategoria().getId().equals(categoria.getId());
            if (!esDeEstaCategoria) {
                continue;
            }
            boolean ocupado = Data.instance().existeSolape(recurso, fecha, horaInicio, horaFin, null);
            if (!ocupado) {
                return recurso;
            }
        }
        return null;
    }

    /** Cancela una reserva futura del funcionario actual, liberando sus recursos. */
    void cancelar(String reservaId) throws Exception {
        Usuario usuario = usuarioActual();

        Reserva reserva = Data.instance().buscarReservaPorId(reservaId);
        if (reserva == null) {
            throw new Exception("Esa reserva ya no existe.");
        }
        if (reserva.getFuncionario() == null || reserva.getFuncionario().getId() != usuario.getId()) {
            throw new Exception("Solo puede cancelar sus propias reservas.");
        }
        if (!Reserva.ACTIVA.equals(reserva.getEstado())) {
            throw new Exception("Esa reserva ya está cancelada.");
        }

        LocalDateTime inicioReserva = LocalDateTime.of(reserva.getFecha(), reserva.getHoraInicio());
        if (inicioReserva.isBefore(LocalDateTime.now())) {
            throw new Exception("Solo se pueden cancelar reservas futuras.");
        }

        reserva.setEstado(Reserva.CANCELADA);
        Data.instance().actualizarReserva(reserva);
        cargarMisReservas();
    }

    /**
     * Le pasa la frase en lenguaje natural a un modelo de IA (LangChain4j + el proxy
     * gratuito de demo) para que extraiga actividad/fecha/horas/categorías.
     * El usuario podrá revisar y corregir lo que devuelva antes de reservar.
     */
    ReservaExtraccion extraerConIA(String frase) throws Exception {
        if (frase == null || frase.isBlank()) {
            throw new Exception("Escriba primero una frase describiendo la reserva.");
        }

        ChatLanguageModel modeloIA = OpenAiChatModel.builder()
                .baseUrl("http://langchain4j.dev/demo/openai/v1") // proxy gratuito de LangChain4j
                .apiKey("demo")                                    // llave de demo, sin costo
                .modelName("gpt-4o-mini")                          // modelo restringido del proxy de demo
                .build();

        ReservaExtractorService servicioIA = AiServices.create(ReservaExtractorService.class, modeloIA);

        String listaCategorias = model.getCategorias().stream()
                .map(Categoria::getDescripcion)
                .collect(Collectors.joining(", "));

        return servicioIA.extraer(frase, listaCategorias, LocalDate.now().toString());
    }
}


