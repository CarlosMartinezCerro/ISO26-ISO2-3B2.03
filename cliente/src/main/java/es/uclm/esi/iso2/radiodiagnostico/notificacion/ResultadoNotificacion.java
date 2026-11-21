package es.uclm.esi.iso2.radiodiagnostico.notificacion;

/** Respuesta del servidor tras intentar notificar a un paciente. */
public record ResultadoNotificacion(boolean correcto, String mensaje, String idNotificacion) {
}
