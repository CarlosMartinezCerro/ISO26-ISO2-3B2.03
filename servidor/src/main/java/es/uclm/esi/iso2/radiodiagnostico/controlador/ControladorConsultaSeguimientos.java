package es.uclm.esi.iso2.radiodiagnostico.controlador;

import es.uclm.esi.iso2.radiodiagnostico.informe.DniPacienteInvalidoException;
import es.uclm.esi.iso2.radiodiagnostico.prueba.ResultadoConsultaSeguimientos;
import es.uclm.esi.iso2.radiodiagnostico.prueba.SeguimientoPeriodico;
import es.uclm.esi.iso2.radiodiagnostico.prueba.ServicioConsultaSeguimiento;
import java.util.List;
import java.util.Objects;

/**
 * Controlador del servidor para el CdU11 · Consultar prueba de seguimiento periódica.
 * Sigue el mismo esquema que {@link ControladorHistorialPruebas} (CdU10): llama al modelo y
 * convierte sus excepciones en un resultado con un mensaje que el cliente puede mostrar.
 */
public class ControladorConsultaSeguimientos {

    static final String ERROR_INTERNO = "No se han podido consultar las pruebas de seguimiento. "
            + "Inténtelo de nuevo más tarde.";

    private final ServicioConsultaSeguimiento servicio;

    public ControladorConsultaSeguimientos(ServicioConsultaSeguimiento servicio) {
        this.servicio = Objects.requireNonNull(servicio,
                "El servicio de consulta de seguimientos es obligatorio");
    }

    public ResultadoConsultaSeguimientos consultarSeguimientos(String dniPaciente) {
        try {
            List<SeguimientoPeriodico> seguimientos = servicio.consultarSeguimientos(dniPaciente);
            String mensaje = seguimientos.isEmpty()
                    ? "El paciente " + dniPaciente.trim()
                            + " no tiene pruebas de seguimiento periódicas."
                    : seguimientos.size() == 1
                            ? "Se ha encontrado 1 prueba de seguimiento periódica."
                            : "Se han encontrado " + seguimientos.size()
                                    + " pruebas de seguimiento periódicas.";
            return new ResultadoConsultaSeguimientos(true, mensaje, seguimientos);
        } catch (DniPacienteInvalidoException e) {
            return ResultadoConsultaSeguimientos.error(e.getMessage());
        } catch (IllegalStateException e) {
            // Fallo de persistencia: no se muestran detalles internos al cliente.
            return ResultadoConsultaSeguimientos.error(ERROR_INTERNO);
        }
    }
}
