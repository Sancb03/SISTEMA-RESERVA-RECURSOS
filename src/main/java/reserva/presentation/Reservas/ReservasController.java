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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ReservasController {

    private final ReservasView view;
    private final ReservasModel model;

    public ReservasController(
            ReservasView view,
            ReservasModel model) {

        this.view = view;
        this.model = model;

        view.setController(this);
        view.setModel(model);
    }

    public ReservasView getView() {
        return view;
    }

    public ReservasModel getModel() {
        return model;
    }

    private Usuario usuarioActual() throws Exception {

        Usuario usuario = Sesion.getUsuarioActual();

        if (usuario == null) {
            throw new Exception(
                    "No hay una sesión activa. Debe iniciar sesión de nuevo."
            );
        }

        return usuario;
    }

    public void cargarCategorias() throws Exception {

        model.setCategorias(
                Data.instance().listarCategorias(
                        usuarioActual()
                )
        );
    }

    public void cargarMisReservas() throws Exception {

        Usuario usuario = usuarioActual();

        List<Reserva> reservas =
                Data.instance()
                        .listarReservasPorFuncionario(
                                usuario.getId()
                        );

        model.setMisReservas(reservas);
    }

    public void reservar(
            String actividad,
            LocalDate fecha,
            LocalTime horaInicio,
            LocalTime horaFin,
            List<Categoria> categoriasSeleccionadas,
            List<Recurso> recursosElegidosManualmente)
            throws Exception {

        Usuario usuario = usuarioActual();

        if (actividad == null || actividad.isBlank()) {
            throw new Exception(
                    "Debe indicar la actividad."
            );
        }

        if (fecha == null) {
            throw new Exception(
                    "Debe indicar la fecha."
            );
        }

        if (horaInicio == null || horaFin == null) {
            throw new Exception(
                    "Debe indicar la hora de inicio y la hora de fin."
            );
        }

        if (!horaInicio.isBefore(horaFin)) {
            throw new Exception(
                    "La hora de inicio debe ser antes que la hora de fin."
            );
        }

        if (fecha.isBefore(LocalDate.now())) {
            throw new Exception(
                    "La fecha no puede ser en el pasado."
            );
        }

        if (categoriasSeleccionadas == null ||
                categoriasSeleccionadas.isEmpty()) {

            throw new Exception(
                    "Debe seleccionar al menos una categoría de recurso."
            );
        }

        List<Recurso> todosLosRecursos =
                Data.instance().listarRecursos(
                        usuario
                );

        List<Recurso> elegidos =
                recursosElegidosManualmente != null
                        ? recursosElegidosManualmente
                        : List.of();

        List<Recurso> recursosAsignados =
                new ArrayList<>();

        List<String> categoriasNoDisponibles =
                new ArrayList<>();

        for (Categoria categoria :
                categoriasSeleccionadas) {

            Recurso elegidoParaEstaCategoria =
                    elegidos.stream()
                            .filter(r ->
                                    r.getCategoria() != null
                                            &&
                                            r.getCategoria()
                                                    .getId()
                                                    .equals(
                                                            categoria.getId()
                                                    )
                            )
                            .findFirst()
                            .orElse(null);

            Recurso disponible;

            if (elegidoParaEstaCategoria != null) {

                boolean sigueLibre =
                        !Data.instance()
                                .existeSolape(
                                        elegidoParaEstaCategoria,
                                        fecha,
                                        horaInicio,
                                        horaFin,
                                        null
                                );

                disponible =
                        sigueLibre
                                ? elegidoParaEstaCategoria
                                : null;

            } else {

                disponible =
                        buscarRecursoDisponible(
                                todosLosRecursos,
                                categoria,
                                fecha,
                                horaInicio,
                                horaFin
                        );
            }

            if (disponible == null) {

                categoriasNoDisponibles.add(
                        categoria.getDescripcion()
                );

            } else {

                recursosAsignados.add(
                        disponible
                );
            }
        }

        if (!categoriasNoDisponibles.isEmpty()) {

            throw new Exception(
                    "No hay disponibilidad para: "
                            + String.join(
                            ", ",
                            categoriasNoDisponibles
                    )
                            + ". Ajuste la reserva e intente de nuevo."
            );
        }

        Reserva reserva = new Reserva();

        reserva.setActividad(
                actividad.trim()
        );

        reserva.setFecha(
                fecha
        );

        reserva.setHoraInicio(
                horaInicio
        );

        reserva.setHoraFin(
                horaFin
        );

        reserva.setFuncionario(
                usuario
        );

        reserva.setEstado(
                Reserva.ACTIVA
        );

        reserva.setRecursos(
                recursosAsignados
        );

        Data.instance()
                .guardarReserva(
                        reserva
                );

        cargarMisReservas();
    }

    public List<Recurso> listarRecursosDisponibles(
            List<Categoria> categorias,
            LocalDate fecha,
            LocalTime horaInicio,
            LocalTime horaFin)
            throws Exception {

        Usuario usuario =
                usuarioActual();

        List<Recurso> todosLosRecursos =
                Data.instance()
                        .listarRecursos(
                                usuario
                        );

        List<Recurso> disponibles =
                new ArrayList<>();

        for (Categoria categoria :
                categorias) {

            for (Recurso recurso :
                    todosLosRecursos) {

                boolean esDeEstaCategoria =
                        recurso.getCategoria() != null
                                &&
                                recurso.getCategoria()
                                        .getId()
                                        .equals(
                                                categoria.getId()
                                        );

                if (!esDeEstaCategoria) {
                    continue;
                }

                boolean ocupado =
                        Data.instance()
                                .existeSolape(
                                        recurso,
                                        fecha,
                                        horaInicio,
                                        horaFin,
                                        null
                                );

                if (!ocupado) {
                    disponibles.add(recurso);
                }
            }
        }

        return disponibles;
    }

    private Recurso buscarRecursoDisponible(
            List<Recurso> todosLosRecursos,
            Categoria categoria,
            LocalDate fecha,
            LocalTime horaInicio,
            LocalTime horaFin) {

        for (Recurso recurso :
                todosLosRecursos) {

            boolean esDeEstaCategoria =
                    recurso.getCategoria() != null
                            &&
                            recurso.getCategoria()
                                    .getId()
                                    .equals(
                                            categoria.getId()
                                    );

            if (!esDeEstaCategoria) {
                continue;
            }

            boolean ocupado =
                    Data.instance()
                            .existeSolape(
                                    recurso,
                                    fecha,
                                    horaInicio,
                                    horaFin,
                                    null
                            );

            if (!ocupado) {
                return recurso;
            }
        }

        return null;
    }

    public void cancelar(
            String reservaId)
            throws Exception {

        Usuario usuario =
                usuarioActual();

        Reserva reserva =
                Data.instance()
                        .buscarReservaPorId(
                                reservaId
                        );

        if (reserva == null) {
            throw new Exception(
                    "Esa reserva ya no existe."
            );
        }

        if (reserva.getFuncionario() == null
                ||
                reserva.getFuncionario()
                        .getId()
                        != usuario.getId()) {

            throw new Exception(
                    "Solo puede cancelar sus propias reservas."
            );
        }

        if (!Reserva.ACTIVA.equals(
                reserva.getEstado())) {

            throw new Exception(
                    "Esa reserva ya está cancelada."
            );
        }

        LocalDateTime inicioReserva =
                LocalDateTime.of(
                        reserva.getFecha(),
                        reserva.getHoraInicio()
                );

        if (inicioReserva.isBefore(
                LocalDateTime.now())) {

            throw new Exception(
                    "Solo se pueden cancelar reservas futuras."
            );
        }

        reserva.setEstado(
                Reserva.CANCELADA
        );

        Data.instance()
                .actualizarReserva(
                        reserva
                );

        cargarMisReservas();
    }

    public ReservaExtraccion extraerConIA(
            String frase)
            throws Exception {

        if (frase == null ||
                frase.isBlank()) {

            throw new Exception(
                    "Escriba primero una frase describiendo la reserva."
            );
        }

        ChatLanguageModel modeloIA =
                OpenAiChatModel.builder()
                        .baseUrl(
                                "http://langchain4j.dev/demo/openai/v1"
                        )
                        .apiKey("demo")
                        .modelName(
                                "gpt-4o-mini"
                        )
                        .build();

        ReservaExtractorService servicioIA =
                AiServices.create(
                        ReservaExtractorService.class,
                        modeloIA
                );

        String listaCategorias =
                model.getCategorias()
                        .stream()
                        .map(
                                Categoria::getDescripcion
                        )
                        .collect(
                                Collectors.joining(", ")
                        );

        return servicioIA.extraer(
                frase,
                listaCategorias,
                LocalDate.now().toString()
        );
    }
}