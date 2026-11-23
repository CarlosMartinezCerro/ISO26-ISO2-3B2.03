package es.uclm.esi.iso2.radiodiagnostico.prueba;

import java.time.LocalDate;
import java.util.List;

/**
 * Prueba de seguimiento periódica recibida del servidor (CdU11). Tiene los mismos campos que el
 * JSON de GET /seguimientos. Reutiliza {@link PrioridadSeguimiento} y {@link RepeticionSeguimiento}
 * del CdU13; el estado (EN_CURSO o FINALIZADO) llega como texto.
 */
public record SeguimientoPeriodico(String idSolicitud, String dniPaciente, String dniMedico,
                                   String tipoPrueba, String indicacionClinica,
                                   PrioridadSeguimiento prioridad,
                                   String periodicidad, LocalDate fechaInicio, LocalDate fechaFin,
                                   int repeticionesTotales, int repeticionesCitadas,
                                   LocalDate proximaFecha, String estado,
                                   List<RepeticionSeguimiento> repeticiones) {

    public SeguimientoPeriodico {
        repeticiones = repeticiones == null ? List.of() : List.copyOf(repeticiones);
    }
}
