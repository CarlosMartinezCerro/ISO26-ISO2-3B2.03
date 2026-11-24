package es.uclm.esi.iso2.radiodiagnostico.solicitud;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ServicioSolicitudesImpl implements ServicioSolicitudes {

    private final SolicitudRepositorio solicitudes;

    public ServicioSolicitudesImpl(SolicitudRepositorio solicitudes) {
        this.solicitudes = Objects.requireNonNull(solicitudes,
                "El repositorio de solicitudes es obligatorio");
    }

    @Override
    public List<String> listarPruebasDisponibles() {
        return CatalogoPruebas.disponibles();
    }

    @Override
    public SolicitudPrueba solicitar(String dniPaciente, DatosSolicitudPrueba datos) {
        if (dniPaciente == null || dniPaciente.isBlank()) {
            throw new DatosSolicitudInvalidosException("El paciente identificado es obligatorio.");
        }
        if (datos == null) {
            throw new DatosSolicitudInvalidosException("Los datos de la solicitud son obligatorios.");
        }
        if (datos.tipoPrueba() == null || datos.tipoPrueba().isBlank()) {
            throw new DatosSolicitudInvalidosException("El tipo de prueba es obligatorio.");
        }
        String tipoPrueba = datos.tipoPrueba().trim();
        if (!CatalogoPruebas.estaDisponible(tipoPrueba)) {
            throw new DatosSolicitudInvalidosException("La prueba seleccionada no está disponible.");
        }
        if (datos.prioridad() == null) {
            throw new DatosSolicitudInvalidosException("La prioridad es obligatoria.");
        }

        SolicitudPrueba solicitud = new SolicitudPrueba(
                "SOL-" + UUID.randomUUID(), dniPaciente.trim(), tipoPrueba,
                datos.prioridad(), EstadoSolicitud.PENDIENTE);
        solicitudes.guardar(solicitud);
        return solicitud;
    }
}
