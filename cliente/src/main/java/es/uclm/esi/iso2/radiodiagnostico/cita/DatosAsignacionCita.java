package es.uclm.esi.iso2.radiodiagnostico.cita;

import java.time.LocalDateTime;

public record DatosAsignacionCita(String idSolicitud, LocalDateTime fechaHora, String sala) {
}
