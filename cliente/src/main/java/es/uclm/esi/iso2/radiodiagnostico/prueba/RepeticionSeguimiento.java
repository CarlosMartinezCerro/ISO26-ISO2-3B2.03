package es.uclm.esi.iso2.radiodiagnostico.prueba;

import java.time.LocalDate;

/**
 * Cada una de las repeticiones de una prueba de seguimiento (generadas en el CdU13), tal como
 * llegan del servidor. {@code estado} es PENDIENTE_CITA o CITADA.
 */
public record RepeticionSeguimiento(String id, String idSolicitudSeguimiento, int numero,
                                    LocalDate fechaPrevista, String estado) {
}
