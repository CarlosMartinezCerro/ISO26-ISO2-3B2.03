package es.uclm.esi.iso2.radiodiagnostico.modelo.informe;

import java.time.LocalDateTime;

/** Resumen de una prueba histórica de un paciente (CdU10), construida a partir de Imagen y SolicitudPrueba. */
public record PruebaHistorica(String idPrueba, String dniPaciente, String tipoPrueba,
                              String estado, String idInforme, String referenciaPacs,
                              LocalDateTime fecha) {
}