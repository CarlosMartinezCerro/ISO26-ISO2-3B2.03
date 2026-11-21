package es.uclm.esi.iso2.radiodiagnostico.controlador;

import es.uclm.esi.iso2.radiodiagnostico.notificacion.CanalNotificacion;
import es.uclm.esi.iso2.radiodiagnostico.notificacion.DatosNotificacion;
import es.uclm.esi.iso2.radiodiagnostico.notificacion.Notificacion;
import es.uclm.esi.iso2.radiodiagnostico.notificacion.NotificacionInvalidaException;
import es.uclm.esi.iso2.radiodiagnostico.notificacion.PacienteNoEncontradoException;
import es.uclm.esi.iso2.radiodiagnostico.notificacion.PasarelaNoDisponibleException;
import es.uclm.esi.iso2.radiodiagnostico.notificacion.RespuestaListadoNotificaciones;
import es.uclm.esi.iso2.radiodiagnostico.notificacion.ResultadoNotificacion;
import es.uclm.esi.iso2.radiodiagnostico.notificacion.ServicioNotificaciones;
import es.uclm.esi.iso2.radiodiagnostico.notificacion.TipoNotificacion;
import java.util.List;
import java.util.Objects;

/**
 * Controlador del servidor para el CdU5 · Notificar.
 * Recibe las peticiones del cliente, llama al modelo y convierte sus
 * excepciones en respuestas con un mensaje que el cliente puede mostrar.
 */
public class ControladorNotificaciones {

    static final String ERROR_INTERNO =
            "No se ha podido acceder a las notificaciones. Inténtelo de nuevo más tarde.";
    static final String PASARELA_NO_DISPONIBLE =
            "No se ha podido entregar la notificación. Inténtelo de nuevo más tarde.";

    private final ServicioNotificaciones servicio;

    public ControladorNotificaciones(ServicioNotificaciones servicio) {
        this.servicio = Objects.requireNonNull(servicio, "El servicio de notificaciones es obligatorio");
    }

    public ResultadoNotificacion notificar(DatosNotificacion datos) {
        try {
            Notificacion notificacion = servicio.notificar(datos, TipoNotificacion.GENERAL);
            return new ResultadoNotificacion(true, "Notificación " + notificacion.id() + " enviada "
                    + comoSeEnvio(notificacion) + ".", notificacion.id());
        } catch (NotificacionInvalidaException | PacienteNoEncontradoException e) {
            return new ResultadoNotificacion(false, e.getMessage(), null);
        } catch (PasarelaNoDisponibleException e) {
            return new ResultadoNotificacion(false, PASARELA_NO_DISPONIBLE, null);
        } catch (IllegalStateException e) {
            // Fallo de persistencia: no se muestran detalles internos al cliente.
            return new ResultadoNotificacion(false, ERROR_INTERNO, null);
        }
    }

    private static String comoSeEnvio(Notificacion notificacion) {
        return notificacion.canal() == CanalNotificacion.SMS
                ? "por SMS al " + notificacion.destino()
                : "a la aplicación del paciente " + notificacion.destino();
    }

    public RespuestaListadoNotificaciones historialPaciente(String dniPaciente) {
        try {
            List<Notificacion> notificaciones = servicio.historialPaciente(dniPaciente);
            return new RespuestaListadoNotificaciones(true,
                    notificaciones.size() + " notificación(es) encontradas.", notificaciones);
        } catch (NotificacionInvalidaException e) {
            return new RespuestaListadoNotificaciones(false, e.getMessage(), List.of());
        } catch (IllegalStateException e) {
            return new RespuestaListadoNotificaciones(false, ERROR_INTERNO, List.of());
        }
    }
}
