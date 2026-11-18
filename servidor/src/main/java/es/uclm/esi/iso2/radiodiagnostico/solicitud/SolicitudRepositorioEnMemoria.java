package es.uclm.esi.iso2.radiodiagnostico.solicitud;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SolicitudRepositorioEnMemoria implements SolicitudRepositorio {

    private final Map<String, SolicitudPrueba> solicitudes = new LinkedHashMap<>();

    @Override
    public Optional<SolicitudPrueba> buscarPorId(String id) {
        return Optional.ofNullable(solicitudes.get(id));
    }

    @Override
    public List<SolicitudPrueba> buscarPorEstado(EstadoSolicitud estado) {
        return solicitudes.values().stream()
                .filter(solicitud -> solicitud.estado() == estado)
                .toList();
    }

    @Override
    public List<SolicitudPrueba> buscarPorPaciente(String dniPaciente) {
        return solicitudes.values().stream()
                .filter(solicitud -> solicitud.dniPaciente().equalsIgnoreCase(dniPaciente))
                .toList();
    }

    @Override
    public void guardar(SolicitudPrueba solicitud) {
        solicitudes.put(solicitud.id(), solicitud);
    }
}