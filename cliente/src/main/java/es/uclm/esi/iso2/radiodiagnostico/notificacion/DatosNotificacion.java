package es.uclm.esi.iso2.radiodiagnostico.notificacion;

/**
 * Lo que la vista recoge para notificar a un paciente (CdU5).
 *
 * @param canal "SMS" o "APLICACION", como lo espera el servidor
 */
public record DatosNotificacion(String dniPaciente, String canal, String asunto, String mensaje) {
}
