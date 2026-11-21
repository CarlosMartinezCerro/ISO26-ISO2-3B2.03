package es.uclm.esi.iso2.radiodiagnostico.notificacion;

/**
 * Sistema externo que entrega las notificaciones (operador de SMS, servicio de avisos de la app).
 * Mientras no haya integración real se usa {@link PasarelaNotificacionesSimulada}.
 */
public interface PasarelaNotificaciones {

    /**
     * Entrega la notificación al paciente.
     *
     * @throws PasarelaNoDisponibleException si la pasarela no responde
     */
    void enviar(Notificacion notificacion);
}
