package es.uclm.esi.iso2.radiodiagnostico.modelo.solicitud;

/** Prueba diagnóstica solicitada por un médico que hay que citar. */
public record SolicitudPrueba(String id, String dniPaciente, String tipoPrueba,
                              PrioridadSolicitud prioridad, EstadoSolicitud estado) {

    public SolicitudPrueba conEstado(EstadoSolicitud nuevoEstado) {
        return new SolicitudPrueba(id, dniPaciente, tipoPrueba, prioridad, nuevoEstado);
    }
}
