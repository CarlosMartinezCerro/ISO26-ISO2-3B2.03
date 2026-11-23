package es.uclm.esi.iso2.radiodiagnostico.modelo.informe;

import es.uclm.esi.iso2.radiodiagnostico.modelo.solicitud.SolicitudPrueba;
import es.uclm.esi.iso2.radiodiagnostico.modelo.solicitud.SolicitudRepositorio;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/** Implementación del CdU10 que reutiliza ImagenRepositorio e Imagen (CdU17) y SolicitudRepositorio (CdU25). */
public class ServicioHistorialPruebasImpl implements ServicioHistorialPruebas {

    private final ImagenRepositorio imagenes;
    private final SolicitudRepositorio solicitudes;

    public ServicioHistorialPruebasImpl(ImagenRepositorio imagenes, SolicitudRepositorio solicitudes) {
        this.imagenes = Objects.requireNonNull(imagenes, "El repositorio de imágenes es obligatorio");
        this.solicitudes = Objects.requireNonNull(solicitudes, "El repositorio de solicitudes es obligatorio");
    }

    @Override
    public List<PruebaHistorica> consultarPruebasHistoricas(String dniPaciente) {
        if (dniPaciente == null || dniPaciente.isBlank()) {
            throw new DniPacienteInvalidoException("El DNI del paciente es obligatorio.");
        }
        String dniNormalizado = dniPaciente.trim();

        List<PruebaHistorica> historial = new ArrayList<>();

        for (Imagen img : imagenes.buscarPorPaciente(dniNormalizado)) {
            historial.add(new PruebaHistorica(
                    img.id(),
                    img.dniPaciente(),
                    img.tipoPrueba(),
                    "REALIZADA",
                    img.idInforme(),
                    img.referenciaPacs(),
                    img.fechaCreacion()));
        }

        for (SolicitudPrueba sol : solicitudes.buscarPorPaciente(dniNormalizado)) {
            historial.add(new PruebaHistorica(
                    sol.id(),
                    sol.dniPaciente(),
                    sol.tipoPrueba(),
                    sol.estado().name(),
                    "-",
                    "-",
                    null));
        }

        historial.sort(Comparator.comparing(
                PruebaHistorica::fecha,
                Comparator.nullsLast(Comparator.reverseOrder())));

        return List.copyOf(historial);
    }
}