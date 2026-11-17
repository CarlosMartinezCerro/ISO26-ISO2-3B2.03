package es.uclm.esi.iso2.radiodiagnostico.cita;

import java.time.LocalDateTime;

/** Cita asignada por el personal administrativo para una solicitud concreta. */
public record Cita(String id, String idSolicitud, String dniPaciente, String tipoPrueba,
                   LocalDateTime fechaHora, String sala, EstadoCita estado) {
}
