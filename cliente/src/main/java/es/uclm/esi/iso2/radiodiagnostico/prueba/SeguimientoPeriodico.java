package es.uclm.esi.iso2.radiodiagnostico.prueba;

import java.time.LocalDate;
import java.util.List;

/**
 * Prueba de seguimiento periódica recibida del servidor (CdU11). Tiene los mismos campos que el
 * JSON de GET /seguimientos. La prioridad y el estado llegan como texto para que el cliente no
 * dependa de los enumerados del servidor.
 */
public record SeguimientoPeriodico(String idSolicitud, String dniPaciente, String dniMedico,
                                   String tipoPrueba, String indicacionClinica, String prioridad,
                                   String periodicidad, LocalDate fechaInicio, LocalDate fechaFin,
                                   int repeticionesTotales, int repeticionesCitadas,
                                   LocalDate proximaFecha, String estado,
                                   List<RepeticionSeguimiento> repeticiones) {

    public SeguimientoPeriodico {
        repeticiones = repeticiones == null ? List.of() : List.copyOf(repeticiones);
    }
}
