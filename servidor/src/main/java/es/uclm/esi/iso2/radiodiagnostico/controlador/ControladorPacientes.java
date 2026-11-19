package es.uclm.esi.iso2.radiodiagnostico.controlador;

import es.uclm.esi.iso2.radiodiagnostico.paciente.DatosAltaPaciente;
import es.uclm.esi.iso2.radiodiagnostico.paciente.DatosInvalidosException;
import es.uclm.esi.iso2.radiodiagnostico.paciente.Paciente;
import es.uclm.esi.iso2.radiodiagnostico.paciente.PacienteDuplicadoException;
import es.uclm.esi.iso2.radiodiagnostico.paciente.ResultadoAlta;
import es.uclm.esi.iso2.radiodiagnostico.paciente.ServicioPacientes;
import java.util.Objects;

/**
 * Controlador del servidor para el CdU22 · Dar de alta paciente.
 * Recibe las peticiones del cliente, llama al modelo y convierte sus
 * excepciones en respuestas con un mensaje que el cliente puede mostrar.
 */
public class ControladorPacientes {

    static final String ERROR_INTERNO =
            "No se ha podido completar el alta del paciente. Inténtelo de nuevo más tarde.";

    private final ServicioPacientes servicio;

    public ControladorPacientes(ServicioPacientes servicio) {
        this.servicio = Objects.requireNonNull(servicio, "El servicio de pacientes es obligatorio");
    }

    public ResultadoAlta darDeAlta(DatosAltaPaciente datos) {
        try {
            Paciente paciente = servicio.darDeAlta(datos);
            return new ResultadoAlta(true, "Paciente " + paciente.nombre() + " "
                    + paciente.apellidos() + " (DNI " + paciente.dni() + ") dado de alta.");
        } catch (DatosInvalidosException | PacienteDuplicadoException e) {
            return new ResultadoAlta(false, e.getMessage());
        } catch (IllegalStateException e) {
            // Fallo de persistencia: no se muestran detalles internos al cliente.
            return new ResultadoAlta(false, ERROR_INTERNO);
        }
    }
}
