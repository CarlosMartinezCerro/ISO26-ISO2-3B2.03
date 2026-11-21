package es.uclm.esi.iso2.radiodiagnostico.notificacion;

/** Acceso del cliente al servidor para el CdU5 · Notificar. */
public interface ClienteNotificaciones {

    /** @throws ErrorComunicacionException si no se puede contactar con el servidor */
    ResultadoNotificacion notificar(DatosNotificacion datos);

    /** @throws ErrorComunicacionException si no se puede contactar con el servidor */
    RespuestaListadoNotificaciones historialPaciente(String dniPaciente);
}
