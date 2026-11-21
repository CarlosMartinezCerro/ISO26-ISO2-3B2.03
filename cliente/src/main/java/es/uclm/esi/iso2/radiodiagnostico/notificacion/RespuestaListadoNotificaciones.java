package es.uclm.esi.iso2.radiodiagnostico.notificacion;

import java.util.List;

/** Historial de notificaciones de un paciente que devuelve el servidor. */
public record RespuestaListadoNotificaciones(boolean correcto, String mensaje,
                                             List<NotificacionEnviada> notificaciones) {
}
