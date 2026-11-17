package es.uclm.esi.iso2.radiodiagnostico.cita;

/** Respuesta que el servidor devuelve al cliente tras intentar confirmar una cita. */
public record ResultadoConfirmacion(boolean correcto, String mensaje) {
}
