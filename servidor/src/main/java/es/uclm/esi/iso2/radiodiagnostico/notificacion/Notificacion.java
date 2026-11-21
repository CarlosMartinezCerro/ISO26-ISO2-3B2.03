package es.uclm.esi.iso2.radiodiagnostico.notificacion;

import java.time.LocalDateTime;

/**
 * Notificación enviada a un paciente.
 *
 * @param destino teléfono del paciente si el canal es SMS, o su DNI si es la aplicación
 */
public record Notificacion(String id, String dniPaciente, TipoNotificacion tipo, CanalNotificacion canal,
                           String destino, String asunto, String mensaje, LocalDateTime fechaEnvio) {
}
