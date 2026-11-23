
package es.uclm.esi.iso2.radiodiagnostico.cita;

import java.time.LocalDateTime;

/** Información de una cita recibida desde el servidor. */
public record CitaPaciente(
        String id,
        String idSolicitud,
        String dniPaciente,
        String tipoPrueba,
        LocalDateTime fechaHora,
        String sala,
        String estado) {
}

