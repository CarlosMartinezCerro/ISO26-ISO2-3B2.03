package es.uclm.esi.iso2.radiodiagnostico.cita;

import java.time.LocalDateTime;

/** Datos que introduce el personal administrativo para asignar la cita. */
public record DatosAsignacionCita(String idSolicitud, LocalDateTime fechaHora, String sala) {
}
