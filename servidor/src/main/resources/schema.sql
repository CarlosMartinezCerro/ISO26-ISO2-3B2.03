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

-- CdU5 Notificar (también CdU6 y CdU7, con otro tipo)
CREATE TABLE IF NOT EXISTS notificacion (
    id VARCHAR(20) NOT NULL PRIMARY KEY,
    dni_paciente VARCHAR(255) NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    canal VARCHAR(20) NOT NULL,
    destino VARCHAR(255) NOT NULL,
    asunto VARCHAR(100) NOT NULL,
    mensaje VARCHAR(500) NOT NULL,
    fecha_envio DATETIME(6) NOT NULL,
    INDEX idx_notificacion_paciente (dni_paciente)
);
