package es.uclm.esi.iso2.radiodiagnostico.prueba;

import java.time.LocalDate;

/**
 * Datos que el médico envía para solicitar una prueba de seguimiento periódica (CdU13).
 * Solo uno de {@code numeroRepeticiones} y {@code fechaLimite} tiene valor: el criterio de fin
 * elegido en la pantalla.
 */
public record DatosSolicitudSeguimiento(String dniPaciente, String dniMedico, String tipoPrueba,
                                        String indicacionClinica, String justificacion,
                                        PrioridadSeguimiento prioridad, Integer cada,
                                        UnidadPeriodicidad unidad, LocalDate fechaInicio,
                                        Integer numeroRepeticiones, LocalDate fechaLimite) {
}
