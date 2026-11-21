package es.uclm.esi.iso2.radiodiagnostico.notificacion;

/**
 * Motivo de la notificación. El CdU5 envía avisos {@link #GENERAL}; los CdU6 (notificar cita) y
 * CdU7 (notificar informe) reutilizan el mismo servicio con su propio tipo.
 */
public enum TipoNotificacion {
    GENERAL,
    CITA,
    INFORME
}
