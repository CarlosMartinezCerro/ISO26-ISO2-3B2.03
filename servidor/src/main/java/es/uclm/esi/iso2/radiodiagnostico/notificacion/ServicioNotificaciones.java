package es.uclm.esi.iso2.radiodiagnostico.notificacion;

import java.util.List;

/** Modelo del CdU5 · Notificar. Lo reutilizan los CdU6 y CdU7 cambiando el tipo. */
public interface ServicioNotificaciones {

    /**
     * Envía una notificación a un paciente dado de alta y la guarda en su historial.
     *
     * @throws NotificacionInvalidaException si falta algún dato o es demasiado largo
     * @throws PacienteNoEncontradoException si el paciente no existe
     * @throws PasarelaNoDisponibleException si no se ha podido entregar (no se guarda)
     */
    Notificacion notificar(DatosNotificacion datos, TipoNotificacion tipo);

    /** @return el historial del paciente, de la más reciente a la más antigua */
    List<Notificacion> historialPaciente(String dniPaciente);
}
