package es.uclm.esi.iso2.radiodiagnostico.notificacion;

/** Respuesta que el servidor devuelve al cliente tras intentar notificar a un paciente. */
public record ResultadoNotificacion(boolean correcto, String mensaje, String idNotificacion) {
}
