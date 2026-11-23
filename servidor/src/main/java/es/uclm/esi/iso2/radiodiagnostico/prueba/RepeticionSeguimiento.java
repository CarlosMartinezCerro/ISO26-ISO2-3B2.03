package es.uclm.esi.iso2.radiodiagnostico.prueba;

import java.time.LocalDate;

/**
 * Solicitud pendiente de cita generada automáticamente por una solicitud de seguimiento.
 * {@code numero} empieza en 1.
 */
public record RepeticionSeguimiento(String id, String idSolicitudSeguimiento, int numero,
                                    LocalDate fechaPrevista, EstadoRepeticionSeguimiento estado) {
}
