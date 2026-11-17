package es.uclm.esi.iso2.radiodiagnostico.cita;

/** Respuesta que el servidor devuelve al cliente tras intentar asignar una cita. */
public record ResultadoAsignacion(boolean correcto, String mensaje) {
}
