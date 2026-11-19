-- Tabla de imágenes (CdU17 Crear imagen, CdU19 Consultar imagen)
CREATE TABLE IF NOT EXISTS imagen (
    id              VARCHAR(20)  NOT NULL PRIMARY KEY,
    id_informe      VARCHAR(50)  NOT NULL,
    dni_paciente    VARCHAR(20)  NOT NULL,
    id_medico       VARCHAR(20)  NOT NULL,
    tipo_prueba     VARCHAR(50)  NOT NULL,
    nombre_fichero  VARCHAR(255) NOT NULL,
    contenido       LONGBLOB     NOT NULL,
    fecha_creacion  DATETIME(6)  NOT NULL,
    referencia_pacs VARCHAR(255),
    INDEX idx_imagen_paciente (dni_paciente),
    INDEX idx_imagen_informe (id_informe)
);
