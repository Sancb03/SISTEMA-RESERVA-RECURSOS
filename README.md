# -SISTEMA-RESERVA-RECURSOS
Proyecto Progra III

El Backend y las pruebas de integración leen la contraseña de MySQL desde la variable de entorno `RESERVA_DB_PASSWORD`.
Si no se define, se usa el valor de `database_password` en `Backend/src/main/resources/database.properties`.
