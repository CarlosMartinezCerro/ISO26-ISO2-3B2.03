package es.uclm.esi.iso2.radiodiagnostico.notificacion;

import java.time.LocalDateTime;

/** Una notificación del historial del paciente, tal como la devuelve el servidor. */
public record NotificacionEnviada(String id, String dniPaciente, String tipo, String canal, String destino,
                                  String asunto, String mensaje, LocalDateTime fechaEnvio) {
}
