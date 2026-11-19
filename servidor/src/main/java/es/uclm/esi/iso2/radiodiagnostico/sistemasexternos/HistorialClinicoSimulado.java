package es.uclm.esi.iso2.radiodiagnostico.sistemasexternos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import es.uclm.esi.iso2.radiodiagnostico.informe.Informe;

/** Implementacion simulada del sistema externo de historial clinico. */
public class HistorialClinicoSimulado implements HistorialClinico {

    private final List<Informe> informesArchivados = new ArrayList<>();

    @Override
    public void getAntecedentes(Informe informe) {
        validar(informe);
        String dni = informe.getPaciente().getDni().trim();
        String especialidad = informe.getEspecialidad().trim();

        informe.setAntecedentes("Antecedentes simulados del paciente " + dni + ".");
        informe.setHistorialEspecialidad(
                "Historial simulado de " + especialidad + " para el paciente " + dni + ".");
    }

    @Override
    public void archivar(Informe informe) {
        validar(informe);
        informesArchivados.add(informe);
    }

    /** Devuelve una copia inmodificable de los informes archivados. */
    public List<Informe> obtenerInformesArchivados() {
        return Collections.unmodifiableList(new ArrayList<>(informesArchivados));
    }

    private void validar(Informe informe) {
        if (informe == null) {
            throw new IllegalArgumentException("El informe es obligatorio.");
        }
        if (informe.getPaciente() == null
                || informe.getPaciente().getDni() == null
                || informe.getPaciente().getDni().isBlank()) {
            throw new IllegalArgumentException("El DNI del paciente es obligatorio.");
        }
        if (informe.getEspecialidad() == null || informe.getEspecialidad().isBlank()) {
            throw new IllegalArgumentException("La especialidad es obligatoria.");
        }
    }
}
