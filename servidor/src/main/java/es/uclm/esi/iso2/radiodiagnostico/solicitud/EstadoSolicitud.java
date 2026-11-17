package es.uclm.esi.iso2.radiodiagnostico.solicitud;

/** Estado de una solicitud de prueba desde el punto de vista de la citación. */
public enum EstadoSolicitud {
    /** El médico la ha pedido y aún no tiene cita. */
    PENDIENTE,
    /** Ya tiene una cita asignada (CdU25). */
    CITADA
}
