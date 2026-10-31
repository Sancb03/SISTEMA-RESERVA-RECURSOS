# SISTEMA-RESERVA-RECURSOS
Proyecto #2 – EIF206 Programación III (UNA). Aplicación distribuida: **Frontend** (Swing, MVC) y **Backend** (servidor de sockets + MySQL). La estructura sigue el ejemplo *Personas* del profesor.

## Estructura (3 módulos Maven, como en Personas)
| Módulo | Contenido |
|---|---|
| `Protocol/` | Lo que comparten los dos procesos: clases del dominio (`Usuario`, `Funcionario`, `Administrador`, `Categoria`, `Recurso`, `Reserva`), la interfaz `IService` y las constantes de `Protocol` (puerto, código de cada operación, códigos de error). |
| `Backend/` | `Application` (main), `logic.Server` (acepta conexiones), `logic.Worker` (un hilo por Frontend, atiende cada operación con un `switch`), `logic.Service` (lógica real, singleton) y `data.*` (DAOs JDBC + `Database`). Es el único que toca MySQL. |
| `Frontend/` | `Application` (main), `logic.Service` (el **Proxy**: implementa `IService` y manda cada llamada por el socket), `presentation.*` (MVC: Model / View / Controller por pantalla), `presentation.SocketMonitor` + `SocketListener` (reciben las alertas) y `GeneradorPDF`. No tiene ninguna referencia a la base de datos. |
| `sql/` | `sistema_reservas.sql` (crea la base `reservarecursos`, tablas y datos base) y `datos_demo.sql` (reservas de demostración, fechas del 20-oct al 6-nov de 2026). |

## Cómo ejecutar
1. En MySQL ejecutar `sql/sistema_reservas.sql` y, si se quiere, `sql/datos_demo.sql`.
2. Contraseña de MySQL: variable de entorno `RESERVA_DB_PASSWORD` (si no existe se usa `database_password` de `Backend/src/main/resources/database.properties`, que se deja vacío para no subir contraseñas al repositorio).
3. **Backend**: ejecutar `reserva.Application` del módulo Backend (puerto 1234 por defecto; se puede pasar otro como argumento) o `java -jar Backend/target/RESERVA-BACKEND-1.0-SNAPSHOT-jar-with-dependencies.jar` después de `mvn package`.
4. **Frontend** (en la misma máquina u otra): ejecutar `reserva.Application` del módulo Frontend. La dirección del Backend está en `Frontend/src/main/resources/servidor.properties` (`servidor_host`, `servidor_puerto`) o se pasa con `-Dreserva.servidor.host=IP -Dreserva.servidor.puerto=1234`. Para usar dos máquinas se pone la IP del equipo donde corre el Backend y se permite el puerto 1234 en su firewall.
5. Usuario inicial: identificación `admin`, clave `admin`. Los funcionarios nuevos entran con su id como identificación y clave (luego pueden cambiarla desde el login).

## Cómo se comunican (igual que Personas: SYNC / ASYNC con sid)
- El Frontend abre una conexión **sincrónica** y manda `Protocol.SYNC`; el `Server` crea un `Worker` (con su hilo) y le devuelve un identificador de sesión (`sid`).
- Cada petición es un `int` con el código de la operación (`Protocol.GUARDAR_RESERVA`, `Protocol.LISTAR_CATEGORIAS`, …) seguido de sus parámetros. El `Worker` llama al `Service` y responde con `ERROR_NO_ERROR` + el resultado, o con un código de error (`ERROR_SECURITY`, `ERROR_STATE`, `ERROR_ARGUMENT`, `ERROR_ERROR`) + el mensaje, que el Proxy convierte de nuevo en la excepción correspondiente.
- Al iniciar sesión el Frontend crea un `SocketMonitor`, que abre una conexión **asincrónica** (`Protocol.ASYNC` + `sid`). El `Server` la une al `Worker` de esa sesión. Cuando un usuario guarda una reserva, `Server.deliver_reserva` se la manda (`Protocol.DELIVER_RESERVA`) a todos los demás Workers menos al que la hizo, y el `SocketMonitor` la entrega al panel *Alertas* con `SwingUtilities.invokeLater`.
- La autenticación (`autenticar`) y el cambio de clave (`cambiarClave`) se hacen en el Backend: la clave nunca viaja de vuelta al Frontend.
- El servidor solo deserializa clases del paquete `reserva` y tipos básicos de Java (`ObjectInputFilter`).

## Si el Frontend dice "No se pudo conectar con el servidor"
1. ¿El Backend está corriendo? (`reserva.Application` del módulo Backend debe imprimir `Servidor iniciado en el puerto 1234...`).
2. ¿Coinciden host y puerto? (`Frontend/src/main/resources/servidor.properties`).
3. ¿Es el Backend nuevo? En `Backend/src/main/java/reserva/` solo debe haber `Application.java`, `data/` y `logic/` (con `Server`, `Worker` y `Service`). Si aparece `Servidor.java` o una carpeta `red/`, es una copia vieja: el Frontend nuevo manda `Protocol.SYNC` y espera el `sid`, y el Backend viejo no lo entiende.
4. ¿Está MySQL arriba y con el script `sql/sistema_reservas.sql` ejecutado?

## Pruebas (JUnit Jupiter)
- `mvn test` (raíz): pruebas de **unidad** con Surefire, sin MySQL: `ServerTest` (Server + Worker con un `IService` falso), `ServiceTest` (el Proxy contra un servidor falso), `AlertasTest`, `EstadisticasCalculoTest`, `ReservaTest`, `UsuarioRolTest`.
- `mvn verify`: además corre las de **integración** con Failsafe (`*IT`): `ServiceIT` (Service + MySQL) y `ServerIT` (Frontend simulado ↔ Server ↔ MySQL, con alertas). Usan la base `reservarecursos_test`, creada desde `sql/sistema_reservas.sql`; si no hay MySQL se saltan.

## Reportes PDF
Cada módulo con tabla tiene su botón PDF; en Estadísticas los dos reportes (recursos por categoría y actividades por semana) incluyen la gráfica.
