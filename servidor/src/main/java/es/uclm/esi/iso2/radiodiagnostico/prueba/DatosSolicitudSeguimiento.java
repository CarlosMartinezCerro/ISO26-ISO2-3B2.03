package es.uclm.esi.iso2.radiodiagnostico.prueba;

import es.uclm.esi.iso2.radiodiagnostico.solicitud.PrioridadSolicitud;
import java.time.LocalDate;

/**
 * Datos que envía el médico para solicitar una prueba de seguimiento periódica (CdU13).
 * El fin del seguimiento se indica con {@code numeroRepeticiones} o con {@code fechaLimite},
 * exactamente uno de los dos; el otro debe llegar nulo.
 */
public record DatosSolicitudSeguimiento(String dniPaciente, String dniMedico, String tipoPrueba,
                                        String indicacionClinica, String justificacion,
                                        PrioridadSolicitud prioridad, Integer cada,
                                        UnidadPeriodicidad unidad, LocalDate fechaInicio,
                                        Integer numeroRepeticiones, LocalDate fechaLimite) {
}
