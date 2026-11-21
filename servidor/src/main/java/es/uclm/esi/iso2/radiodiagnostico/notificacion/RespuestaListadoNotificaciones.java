package es.uclm.esi.iso2.radiodiagnostico.notificacion;

import java.util.List;

/** Historial de notificaciones de un paciente que el servidor devuelve al cliente. */
public record RespuestaListadoNotificaciones(boolean correcto, String mensaje, List<Notificacion> notificaciones) {
}
