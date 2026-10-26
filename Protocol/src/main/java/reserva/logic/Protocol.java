package reserva.logic;

/**
 * Constantes que comparten Frontend y Backend (módulo Protocol): dónde está el
 * servidor, el código numérico de cada operación y los códigos de respuesta.
 * Cada petición viaja como un int (la operación) seguido de sus parámetros;
 * cada respuesta empieza con un int (ERROR_NO_ERROR o el tipo de error).
 */
public class Protocol {
    public static final String SERVER = "localhost";
    public static final int PORT = 5000;

    // ----- USUARIOS (usado por Login) -----
    public static final int GUARDAR_USUARIO = 101;
    public static final int ACTUALIZAR_USUARIO = 102;
    public static final int ELIMINAR_USUARIO = 103;
    public static final int BUSCAR_USUARIO_POR_ID = 104;
    public static final int BUSCAR_USUARIO_POR_IDENTIFICACION = 105;
    public static final int LISTAR_USUARIOS = 106;
    public static final int AUTENTICAR = 107;
    public static final int CAMBIAR_CLAVE = 108;

    // ----- FUNCIONARIOS -----
    public static final int GUARDAR_FUNCIONARIO = 201;
    public static final int ACTUALIZAR_FUNCIONARIO = 202;
    public static final int ELIMINAR_FUNCIONARIO = 203;
    public static final int BUSCAR_FUNCIONARIO_POR_ID = 204;
    public static final int BUSCAR_FUNCIONARIOS_POR_NOMBRE = 205;
    public static final int LISTAR_FUNCIONARIOS = 206;

    // ----- CATEGORIAS -----
    public static final int GUARDAR_CATEGORIA = 301;
    public static final int ACTUALIZAR_CATEGORIA = 302;
    public static final int ELIMINAR_CATEGORIA = 303;
    public static final int BUSCAR_CATEGORIA_POR_ID = 304;
    public static final int BUSCAR_CATEGORIAS_POR_DESCRIPCION = 305;
    public static final int LISTAR_CATEGORIAS = 306;

    // ----- RECURSOS -----
    public static final int GUARDAR_RECURSO = 401;
    public static final int ACTUALIZAR_RECURSO = 402;
    public static final int ELIMINAR_RECURSO = 403;
    public static final int BUSCAR_RECURSO_POR_ID = 404;
    public static final int BUSCAR_RECURSOS_POR_CATEGORIA = 405;
    public static final int LISTAR_RECURSOS = 406;

    // ----- RESERVAS -----
    public static final int GUARDAR_RESERVA = 501;
    public static final int ACTUALIZAR_RESERVA = 502;
    public static final int ELIMINAR_RESERVA = 503;
    public static final int BUSCAR_RESERVA_POR_ID = 504;
    public static final int LISTAR_RESERVAS = 505;
    public static final int LISTAR_RESERVAS_POR_FUNCIONARIO = 506;
    public static final int EXISTE_SOLAPE = 507;

    // ----- Respuestas -----
    public static final int ERROR_NO_ERROR = 0;
    public static final int ERROR_ERROR = 1;      // error inesperado del servidor
    public static final int ERROR_SECURITY = 2;   // SecurityException (permisos / clave incorrecta)
    public static final int ERROR_STATE = 3;      // IllegalStateException (choque de horario, en uso...)
    public static final int ERROR_ARGUMENT = 4;   // IllegalArgumentException (dato inválido)

    // ----- Conexión -----
    public static final int SYNC = 10;      // conexión de peticiones
    public static final int ASYNC = 11;     // conexión de alertas (se une a la sesión SYNC por su sid)
    public static final int DISCONNECT = 12;
    public static final int DELIVER_RESERVA = 13;  // el Backend avisa de una reserva nueva
}
