package es.uclm.esi.iso2.radiodiagnostico.controlador;

import es.uclm.esi.iso2.radiodiagnostico.modelo.informe.DniPacienteInvalidoException;
import es.uclm.esi.iso2.radiodiagnostico.modelo.informe.PruebaHistorica;
import es.uclm.esi.iso2.radiodiagnostico.modelo.informe.ResultadoHistorialPruebas;
import es.uclm.esi.iso2.radiodiagnostico.modelo.informe.ServicioHistorialPruebas;
import java.util.List;
import java.util.Objects;

/**
 * Controlador del servidor para el CdU10 · Consultar pruebas históricas.
 */
public class ControladorHistorialPruebas {

    private final ServicioHistorialPruebas servicio;

    public ControladorHistorialPruebas(ServicioHistorialPruebas servicio) {
        this.servicio = Objects.requireNonNull(servicio, "El servicio de historial es obligatorio");
    }

    public ResultadoHistorialPruebas consultarPruebasHistoricas(String dniPaciente) {
        try {
            List<PruebaHistorica> pruebas = servicio.consultarPruebasHistoricas(dniPaciente);
            String mensaje = pruebas.isEmpty()
                    ? "El paciente " + dniPaciente.trim() + " no tiene pruebas históricas registradas."
                    : "Se han encontrado " + pruebas.size() + " pruebas históricas.";
            return new ResultadoHistorialPruebas(true, mensaje, pruebas);
        } catch (DniPacienteInvalidoException e) {
            return new ResultadoHistorialPruebas(false, e.getMessage(), List.of());
        }
    }
}