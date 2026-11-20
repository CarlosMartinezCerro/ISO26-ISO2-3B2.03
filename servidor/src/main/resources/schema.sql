CREATE TABLE IF NOT EXISTS paciente (
    dni VARCHAR(255) NOT NULL PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    apellidos VARCHAR(255) NOT NULL,
    fecha_nacimiento DATE NOT NULL,
    telefono VARCHAR(255)
);

-- CdU17 Crear imagen / CdU19 Consultar imagen
CREATE TABLE IF NOT EXISTS imagen (
    id VARCHAR(20) NOT NULL PRIMARY KEY,
    id_informe VARCHAR(255) NOT NULL,
    dni_paciente VARCHAR(255) NOT NULL,
    id_medico VARCHAR(255) NOT NULL,
    tipo_prueba VARCHAR(255) NOT NULL,
    nombre_fichero VARCHAR(255) NOT NULL,
    contenido LONGBLOB NOT NULL,
    fecha_creacion DATETIME(6) NOT NULL,
    referencia_pacs VARCHAR(255),
    INDEX idx_imagen_paciente (dni_paciente),
    INDEX idx_imagen_informe (id_informe)
);
