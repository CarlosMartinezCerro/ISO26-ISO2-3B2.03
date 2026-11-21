package es.uclm.esi.iso2.radiodiagnostico.controlador;

import es.uclm.esi.iso2.radiodiagnostico.prueba.DatosSeguimientoInvalidosException;
import es.uclm.esi.iso2.radiodiagnostico.prueba.DatosSolicitudSeguimiento;
import es.uclm.esi.iso2.radiodiagnostico.prueba.ResultadoSolicitudSeguimiento;
import es.uclm.esi.iso2.radiodiagnostico.prueba.ServicioSolicitudSeguimiento;
import es.uclm.esi.iso2.radiodiagnostico.prueba.SolicitudSeguimiento;
import es.uclm.esi.iso2.radiodiagnostico.prueba.TipoPruebaSinSeguimientoException;
import es.uclm.esi.iso2.radiodiagnostico.prueba.UsuarioNoMedicoException;
import java.util.Objects;

/**
 * Controlador del servidor para el CdU13 · Solicitar prueba de seguimiento periódica.
 * Recibe las peticiones del cliente, llama al modelo y convierte sus
 * excepciones en respuestas con un mensaje que el cliente puede mostrar.
 */
public class ControladorSolicitudSeguimiento {

    static final String ERROR_INTERNO = "No se ha podido registrar la solicitud de seguimiento. "
            + "Inténtelo de nuevo más tarde.";

    private final ServicioSolicitudSeguimiento servicio;

    public ControladorSolicitudSeguimiento(ServicioSolicitudSeguimiento servicio) {
        this.servicio = Objects.requireNonNull(servicio,
                "El servicio de solicitudes de seguimiento es obligatorio");
    }

    public ResultadoSolicitudSeguimiento solicitar(DatosSolicitudSeguimiento datos) {
        try {
            SolicitudSeguimiento solicitud = servicio.solicitar(datos);
            return new ResultadoSolicitudSeguimiento(true, "Solicitud de seguimiento de "
                    + solicitud.tipoPrueba() + " registrada con " + solicitud.repeticiones().size()
                    + " solicitudes pendientes de cita.", solicitud.id(), solicitud.repeticiones());
        } catch (DatosSeguimientoInvalidosException | UsuarioNoMedicoException
                 | TipoPruebaSinSeguimientoException e) {
            return ResultadoSolicitudSeguimiento.error(e.getMessage());
        } catch (IllegalStateException e) {
            // Fallo de persistencia: no se muestran detalles internos al cliente.
            return ResultadoSolicitudSeguimiento.error(ERROR_INTERNO);
        }
    }
}
