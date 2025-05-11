-- Hacer que el campo email sea nullable en la tabla alumnos
ALTER TABLE alumnos ALTER COLUMN email NVARCHAR(255) NULL; 