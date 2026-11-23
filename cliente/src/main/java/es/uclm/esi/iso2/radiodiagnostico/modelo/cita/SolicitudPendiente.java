package es.uclm.esi.iso2.radiodiagnostico.modelo.cita;

/** Lo que la vista necesita mostrar de cada solicitud pendiente de cita. */
public record SolicitudPendiente(String id, String dniPaciente, String tipoPrueba,
                                 String prioridad) {
}
