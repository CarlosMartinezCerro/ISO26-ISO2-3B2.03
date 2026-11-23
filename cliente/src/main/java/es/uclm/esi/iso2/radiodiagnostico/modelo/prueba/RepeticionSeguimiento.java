package es.uclm.esi.iso2.radiodiagnostico.modelo.prueba;

import java.time.LocalDate;

/** Solicitud pendiente de cita que el servidor ha generado para una repetición del seguimiento. */
public record RepeticionSeguimiento(String id, String idSolicitudSeguimiento, int numero,
                                    LocalDate fechaPrevista, String estado) {
}
