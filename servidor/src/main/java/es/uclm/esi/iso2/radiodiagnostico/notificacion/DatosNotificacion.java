package es.uclm.esi.iso2.radiodiagnostico.notificacion;

/** Lo que el cliente envía para notificar a un paciente (CdU5). */
public record DatosNotificacion(String dniPaciente, CanalNotificacion canal, String asunto, String mensaje) {
}
