-- ======================================================
-- Datos de demostración: las 12 reservas del juego de datos del Proyecto 1
-- (data-demo/sistema.xml) convertidas a INSERT (en el XML el funcionario se
-- guardaba por identificación; aquí por id de usuario: 111->2, 222->3, 444->444). Ejecutar DESPUÉS de sistema_reservas.sql.
-- ======================================================

USE reservarecursos;

INSERT INTO reservas (id, actividad, fecha, horaInicio, horaFin, funcionario, estado) VALUES
    ('RES-000001', 'Clase', '2026-09-18', '09:00:00', '12:00:00', 444, 'ACTIVA'),
    ('RES-000002', 'Reunion de equipo', '2026-09-08', '08:00:00', '09:00:00', 2, 'ACTIVA'),
    ('RES-000003', 'Taller de Excel', '2026-09-10', '14:00:00', '16:00:00', 3, 'ACTIVA'),
    ('RES-000004', 'Presentacion de resultados', '2026-09-15', '10:00:00', '11:00:00', 2, 'ACTIVA'),
    ('RES-000005', 'Reunion cancelada de prueba', '2026-09-16', '08:00:00', '09:00:00', 3, 'CANCELADA'),
    ('RES-000006', 'Capacitacion', '2026-09-22', '09:00:00', '11:00:00', 444, 'ACTIVA'),
    ('RES-000007', 'Sesion de directorio', '2026-09-23', '13:00:00', '14:00:00', 2, 'ACTIVA'),
    ('RES-000008', 'Uso simultaneo A', '2026-09-25', '10:00:00', '11:00:00', 2, 'ACTIVA'),
    ('RES-000009', 'Uso simultaneo B', '2026-09-25', '10:00:00', '11:00:00', 3, 'ACTIVA'),
    ('RES-000010', 'capacitación', '2026-09-22', '08:00:00', '10:00:00', 2, 'ACTIVA'),
    ('RES-000011', 'reunión de trabajo', '2026-09-19', '14:00:00', '16:00:00', 2, 'ACTIVA'),
    ('RES-000012', 'Preseentacion', '2026-09-17', '09:00:00', '10:00:00', 3, 'ACTIVA');

INSERT INTO reserva_recursos (reserva, recurso) VALUES
    ('RES-000001', '1001'),
    ('RES-000002', '1111'),
    ('RES-000003', '1002'),
    ('RES-000003', '2222'),
    ('RES-000004', '1122'),
    ('RES-000005', '2004'),
    ('RES-000006', '1002'),
    ('RES-000006', '1234'),
    ('RES-000007', '1111'),
    ('RES-000008', '1111'),
    ('RES-000009', '2222'),
    ('RES-000010', '1001'),
    ('RES-000011', '1001'),
    ('RES-000011', '1111'),
    ('RES-000012', '1122');
