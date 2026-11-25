package es.uclm.esi.iso2.radiodiagnostico.informe;

import java.util.Objects;
import java.util.List;

import es.uclm.esi.iso2.radiodiagnostico.sistemasexternos.HistorialClinico;

/** Implementacion del servicio de informes apoyada en el historial clinico externo. */
public class ServicioInformeImpl implements ServicioInforme {

    private final HistorialClinico historialClinico;

    public ServicioInformeImpl(HistorialClinico historialClinico) {
        this.historialClinico = Objects.requireNonNull(
                historialClinico, "El historial clinico es obligatorio");
    }

    @Override
    public Informe crearInforme(Informe informe) {
        if (informe == null) {
            throw new DatosInformeInvalidosException("El informe es obligatorio.");
        }
        if (informe.dniPaciente() == null || informe.dniPaciente().isBlank()) {
            throw new DniPacienteInvalidoException("El DNI del paciente es obligatorio.");
        }
        if (informe.especialidad() == null || informe.especialidad().isBlank()
                || informe.consultaActual() == null || informe.consultaActual().isBlank()
                || informe.diagnostico() == null || informe.diagnostico().isBlank()
                || informe.fecha() == null) {
            throw new DatosInformeInvalidosException(
                    "especialidad, consultaActual, diagnostico y fecha son obligatorios.");
        }
        return historialClinico.archivar(informe);
    }

    @Override
    public Informe obtenerAntecedentes(Informe informe) {
        if (informe == null) {
            throw new DatosInformeInvalidosException("El informe es obligatorio.");
        }
        if (informe.dniPaciente() == null || informe.dniPaciente().isBlank()) {
            throw new DniPacienteInvalidoException("El DNI del paciente es obligatorio.");
        }
        if (informe.especialidad() == null || informe.especialidad().isBlank()) {
            throw new DatosInformeInvalidosException("La especialidad es obligatoria.");
        }
        return historialClinico.getAntecedentes(informe);
    }

    @Override
    public List<InformeResumen> listarInformes(String dni) {
        if (dni == null || dni.isBlank()) {
            throw new DniPacienteInvalidoException("El DNI del paciente es obligatorio.");
        }
        return historialClinico.getInformes(dni);
    }

    @Override
    public Informe obtenerInforme(long id) {
        if (id <= 0) {
            throw new InformeNoEncontradoException(id);
        }
        return historialClinico.getInforme(id);
    }
}
