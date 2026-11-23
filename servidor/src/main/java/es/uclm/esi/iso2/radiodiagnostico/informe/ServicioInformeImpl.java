package es.uclm.esi.iso2.radiodiagnostico.informe;

import java.util.Objects;

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
        historialClinico.archivar(informe);
        return informe;
    }

    @Override
    public String obtenerAntecedentes(Informe informe) {
        historialClinico.getAntecedentes(informe);
        return informe.obtenerAntecedentes();
    }
}
