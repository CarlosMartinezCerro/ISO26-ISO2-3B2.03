package es.uclm.esi.iso2.radiodiagnostico.prueba;

import es.uclm.esi.iso2.radiodiagnostico.solicitud.PrioridadSolicitud;
import java.time.LocalDate;
import java.util.List;

/**
 * Prueba de seguimiento periódica solicitada por un médico (CdU13), con las repeticiones
 * pendientes de cita que se han generado a partir de ella.
 * Solo uno de {@code numeroRepeticiones} y {@code fechaLimite} tiene valor.
 */
public record SolicitudSeguimiento(String id, String dniPaciente, String dniMedico,
                                   String tipoPrueba, String indicacionClinica,
                                   String justificacion, PrioridadSolicitud prioridad,
                                   PeriodicidadSeguimiento periodicidad, LocalDate fechaInicio,
                                   Integer numeroRepeticiones, LocalDate fechaLimite,
                                   List<RepeticionSeguimiento> repeticiones) {

    public SolicitudSeguimiento {
        repeticiones = List.copyOf(repeticiones);
    }
}
