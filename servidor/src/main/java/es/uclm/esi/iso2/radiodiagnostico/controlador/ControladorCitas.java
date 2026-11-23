package es.uclm.esi.iso2.radiodiagnostico.controlador;

import es.uclm.esi.iso2.radiodiagnostico.cita.Cita;
import es.uclm.esi.iso2.radiodiagnostico.cita.DatosAsignacionCita;
import es.uclm.esi.iso2.radiodiagnostico.cita.DatosAsignacionInvalidosException;
import es.uclm.esi.iso2.radiodiagnostico.cita.HuecoOcupadoException;
import es.uclm.esi.iso2.radiodiagnostico.cita.ResultadoAsignacion;
import es.uclm.esi.iso2.radiodiagnostico.cita.ServicioCitas;
import es.uclm.esi.iso2.radiodiagnostico.cita.SolicitudNoPendienteException;
import es.uclm.esi.iso2.radiodiagnostico.solicitud.SolicitudNoEncontradaException;
import es.uclm.esi.iso2.radiodiagnostico.solicitud.SolicitudPrueba;
import java.util.List;
import java.util.Objects;

/**
 * Controlador del servidor para el CdU25 · Asignar cita solicitada.
 * Recibe las peticiones del cliente, llama al modelo y convierte sus
 * excepciones en respuestas con un mensaje que el cliente puede mostrar.
 */
public class ControladorCitas {

    static final String ERROR_GUARDAR_CITA = "No se ha podido guardar la cita. Compruebe que el paciente "
            + "está dado de alta (CdU22) e inténtelo de nuevo.";

    private final ServicioCitas servicio;

    public ControladorCitas(ServicioCitas servicio) {
        this.servicio = Objects.requireNonNull(servicio, "El servicio de citas es obligatorio");
    }

    public List<SolicitudPrueba> solicitudesPendientes() {
        return servicio.listarSolicitudesPendientes();
    }
    public List<Cita> consultarCitas(String dniPaciente) {
        return servicio.buscarCitasPorPaciente(dniPaciente);
    }

    public ResultadoAsignacion asignarCita(DatosAsignacionCita datos) {
        try {
            Cita cita = servicio.asignarCita(datos);
            return new ResultadoAsignacion(true, "Cita asignada para la solicitud "
                    + cita.idSolicitud() + " el " + cita.fechaHora() + " en " + cita.sala() + ".");
        } catch (DatosAsignacionInvalidosException | SolicitudNoEncontradaException
                 | SolicitudNoPendienteException | HuecoOcupadoException e) {
            return new ResultadoAsignacion(false, e.getMessage());
        } catch (IllegalStateException e) {
            // Fallo de la base de datos (por ejemplo, el paciente no existe en MySQL):
            // no se muestran detalles internos al cliente.
            return new ResultadoAsignacion(false, ERROR_GUARDAR_CITA);
        }
    }
}
