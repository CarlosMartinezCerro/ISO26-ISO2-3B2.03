CREATE TABLE IF NOT EXISTS paciente (
    dni VARCHAR(255) NOT NULL PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    apellidos VARCHAR(255) NOT NULL,
    fecha_nacimiento DATE NOT NULL,
    telefono VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS cita (
    id VARCHAR(255) NOT NULL PRIMARY KEY,
    id_solicitud VARCHAR(255) NOT NULL UNIQUE,
    dni_paciente VARCHAR(255) NOT NULL,
    tipo_prueba VARCHAR(255) NOT NULL,
    fecha_hora DATETIME NOT NULL,
    sala VARCHAR(255) NOT NULL,
    estado VARCHAR(50) NOT NULL,

    CONSTRAINT fk_cita_paciente
        FOREIGN KEY (dni_paciente) REFERENCES paciente(dni)
);