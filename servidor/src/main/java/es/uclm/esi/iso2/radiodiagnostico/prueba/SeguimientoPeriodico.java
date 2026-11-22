package es.uclm.esi.iso2.radiodiagnostico.prueba;

import es.uclm.esi.iso2.radiodiagnostico.solicitud.PrioridadSolicitud;
import java.time.LocalDate;
import java.util.List;

/**
 * Resumen de una prueba de seguimiento periódica para el CdU11 · Consultar prueba de
 * seguimiento periódica. Se construye a partir de una {@link SolicitudSeguimiento} del CdU13.
 *
 * <p>{@code fechaFin} es la fecha prevista de la última repetición. {@code proximaFecha} es la
 * primera repetición con fecha prevista igual o posterior a hoy; es nula si el seguimiento ya ha
 * terminado, y en ese caso {@code estado} vale {@link #FINALIZADO}.
 */
public record SeguimientoPeriodico(String idSolicitud, String dniPaciente, String dniMedico,
                                   String tipoPrueba, String indicacionClinica,
                                   PrioridadSolicitud prioridad, String periodicidad,
                                   LocalDate fechaInicio, LocalDate fechaFin,
                                   int repeticionesTotales, int repeticionesCitadas,
                                   LocalDate proximaFecha, String estado,
                                   List<RepeticionSeguimiento> repeticiones) {

    public static final String EN_CURSO = "EN_CURSO";
    public static final String FINALIZADO = "FINALIZADO";

    public SeguimientoPeriodico {
        repeticiones = repeticiones == null ? List.of() : List.copyOf(repeticiones);
    }
}
