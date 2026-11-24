package es.uclm.esi.iso2.radiodiagnostico.prueba;

/** Estado de cada repetición generada por una solicitud de seguimiento (CdU13). */
public enum EstadoRepeticionSeguimiento {
    /** Generada automáticamente; el personal de admisión aún no le ha dado cita. */
    PENDIENTE_CITA,
    /** Ya tiene una cita asignada. */
    CITADA
}
