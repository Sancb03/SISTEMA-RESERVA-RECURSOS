package reserva.presentation.Reservas;

import reserva.data.CategoriaDao;
import reserva.data.RecursoDao;
import reserva.data.ReservaDao;
import reserva.logic.Categoria;
import reserva.logic.Recurso;
import reserva.logic.Reserva;
import reserva.logic.Sesion;
import reserva.logic.Usuario;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

class ReservasController {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm");

    private final ReservasView view;
    private final ReservasModel model;
    private final TablaModel tableModel;

    private final CategoriaDao categoriaDao;
    private final RecursoDao recursoDao;
    private final ReservaDao reservaDao;

    ReservasController(ReservasView view) {
        this(view, new ReservasModel(), new TablaModel());
    }

    ReservasController(ReservasView view, ReservasModel model, TablaModel tableModel) {
        this.view = view;
        this.model = model;
        this.tableModel = tableModel;
        this.categoriaDao = new CategoriaDao();
        this.recursoDao = new RecursoDao();
        this.reservaDao = new ReservaDao();
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
    void cargarCategorias() {
        model.setCategorias(categoriaDao.listar());
    }

    /** Carga y refresca "Mis reservas" para el funcionario que tiene la sesión activa. */
    void cargarMisReservas() throws Exception {
        Usuario usuario = usuarioActual();
        List<Reserva> reservas = reservaDao.listarPorFuncionario(usuario.getId());
        model.setMisReservas(reservas);
        tableModel.setFilas(construirFilas(reservas));
    }

    private List<Object[]> construirFilas(List<Reserva> reservas) {
        List<Object[]> filas = new ArrayList<>();
        for (Reserva r : reservas) {
            String horario = r.getHoraInicio().format(FORMATO_HORA) + " - " + r.getHoraFin().format(FORMATO_HORA);
            String recursos = nombresDeRecursos(r.getRecursosIds());
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

    private String nombresDeRecursos(List<Integer> recursosIds) {
        StringBuilder sb = new StringBuilder();
        for (Integer id : recursosIds) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            Recurso recurso = recursoDao.buscarPorId(id);
            if (recurso != null && recurso.getCodigo() != null && !recurso.getCodigo().isBlank()) {
                sb.append(recurso.getCodigo());
            } else {
                sb.append("Recurso #").append(id);
            }
        }
        return sb.toString();
    }

    /**
     * Intenta crear la reserva. Si alguna categoría solicitada no tiene ningún
     * recurso disponible en ese horario, no guarda nada y lanza Exception
     * indicando cuáles categorías no tienen disponibilidad (para que el
     * funcionario ajuste la reserva e intente de nuevo).
     */
    void reservar(String actividad, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin,
                  List<Categoria> categoriasSeleccionadas) throws Exception {

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

        List<Recurso> todosLosRecursos = recursoDao.listar();

        List<Integer> recursosAsignados = new ArrayList<>();
        List<String> categoriasNoDisponibles = new ArrayList<>();

        for (Categoria categoria : categoriasSeleccionadas) {
            Recurso disponible = buscarRecursoDisponible(todosLosRecursos, categoria, fecha, horaInicio, horaFin);
            if (disponible == null) {
                categoriasNoDisponibles.add(categoria.getNombre());
            } else {
                recursosAsignados.add(disponible.getId());
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
        reserva.setFuncionarioId(usuario.getId());
        reserva.setEstado(Reserva.ACTIVA);
        reserva.setRecursosIds(recursosAsignados);

        reservaDao.guardar(reserva);
        cargarMisReservas();
    }

    /** Busca, entre los recursos de esa categoría, el primero que no tenga choque de horario. */
    private Recurso buscarRecursoDisponible(List<Recurso> todosLosRecursos, Categoria categoria,
                                            LocalDate fecha, LocalTime horaInicio, LocalTime horaFin) {
        for (Recurso recurso : todosLosRecursos) {
            boolean esDeEstaCategoria = recurso.getCategoria() != null
                    && recurso.getCategoria().equalsIgnoreCase(categoria.getNombre());
            if (!esDeEstaCategoria) {
                continue;
            }
            boolean ocupado = reservaDao.existeSolape(recurso.getId(), fecha, horaInicio, horaFin, null);
            if (!ocupado) {
                return recurso;
            }
        }
        return null;
    }

    /** Cancela una reserva futura del funcionario actual, liberando sus recursos. */
    void cancelar(int reservaId) throws Exception {
        Usuario usuario = usuarioActual();

        Reserva reserva = reservaDao.buscarPorId(reservaId);
        if (reserva == null) {
            throw new Exception("Esa reserva ya no existe.");
        }
        if (reserva.getFuncionarioId() != usuario.getId()) {
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
        reservaDao.actualizar(reserva);
        cargarMisReservas();
    }
}