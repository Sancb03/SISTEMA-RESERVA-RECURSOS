# SISTEMA-RESERVA-RECURSOS
Proyecto #2 – EIF206 Programación III (UNA)

## Pasos para correr el proyecto

1. **Cargar la base de datos en MySQL**
    - Abrir MySQL Workbench y conectarse.
    - File → Open SQL Script… → `sql/sistema_reservas.sql` → ejecutar (rayo).
    - (Opcional) Repetir con `sql/datos_demo.sql` para tener reservas de ejemplo.

2. **Poner la contraseña de MySQL**
    - Abrir `Backend/src/main/resources/database.properties` y escribir la contraseña en `database_password=`.

3. **Abrir el proyecto en IntelliJ**
    - Abrir la carpeta raíz (la que tiene el `pom.xml`).
    - Clic derecho en `pom.xml` → Maven → Reload project.

4. **Arrancar el Backend**
   - Ejecutar `Backend/src/main/java/reserva/Application.java`.
   - Debe imprimir `Servidor iniciado en el puerto 1234...` (dejarlo corriendo).
   - Para otro puerto sin tocar código: pasar el número como argumento
     (IntelliJ: Run → Edit Configurations → Program arguments → `5000`; o `java -jar RESERVA-BACKEND-1.0-SNAPSHOT-jar-with-dependencies.jar 5000`).

5. **Arrancar el Frontend**
    - Ejecutar `Frontend/src/main/java/reserva/Application.java`.

6. **Ingresar**
    - Identificación: `admin` — Clave: `admin`.

7. **Probar las alertas (opcional)**
    - En Run → Edit Configurations marcar *Allow multiple instances* en el Frontend y ejecutarlo dos veces.
    - Entrar con usuarios distintos (por ejemplo `111` y `222`; su clave inicial es igual a su identificación) y crear una reserva en una ventana: la otra la muestra en *Alertas*.

8. **Usar dos computadoras (opcional)**
   - En la del Backend: permitir en el firewall el puerto en que escucha (1234 por defecto).
   - En la del Frontend: en `Frontend/src/main/resources/servidor.properties` poner `servidor_host=<IP del Backend>` y `servidor_puerto=<puerto>`.

9. **Correr las pruebas**
   - `mvn test` (unidad, no necesita MySQL).
   - `mvn verify` (unidad + integración; requiere MySQL encendido, usa una base aparte `reservarecursos_test`).