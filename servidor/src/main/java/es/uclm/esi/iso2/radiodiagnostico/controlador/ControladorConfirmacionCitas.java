package es.uclm.esi.iso2.radiodiagnostico.controlador;

import es.uclm.esi.iso2.radiodiagnostico.modelo.cita.Cita;
import es.uclm.esi.iso2.radiodiagnostico.modelo.cita.CitaNoConfirmableException;
import es.uclm.esi.iso2.radiodiagnostico.modelo.cita.CitaNoEncontradaException;
import es.uclm.esi.iso2.radiodiagnostico.modelo.cita.DatosConfirmacionInvalidosException;
import es.uclm.esi.iso2.radiodiagnostico.modelo.cita.ResultadoConfirmacion;
import es.uclm.esi.iso2.radiodiagnostico.modelo.cita.ServicioConfirmacionCitas;
import java.util.Objects;

/**
 * Controlador del servidor para el CdU2 · Confirmar cita.
 * Recibe las peticiones del cliente, llama al modelo y convierte sus
 * excepciones en respuestas con un mensaje que el cliente puede mostrar.
 */
public class ControladorConfirmacionCitas {

    private final ServicioConfirmacionCitas servicio;

    public ControladorConfirmacionCitas(ServicioConfirmacionCitas servicio) {
        this.servicio = Objects.requireNonNull(servicio, "El servicio de confirmación es obligatorio");
    }

    public ResultadoConfirmacion confirmarCita(String idCita, String dniPaciente) {
        try {
            Cita cita = servicio.confirmarCita(idCita, dniPaciente);
            return new ResultadoConfirmacion(true, "Cita " + cita.id() + " confirmada para el "
                    + cita.fechaHora() + " en " + cita.sala() + ".");
        } catch (DatosConfirmacionInvalidosException | CitaNoEncontradaException
                 | CitaNoConfirmableException e) {
            return new ResultadoConfirmacion(false, e.getMessage());
        }
    }
}
