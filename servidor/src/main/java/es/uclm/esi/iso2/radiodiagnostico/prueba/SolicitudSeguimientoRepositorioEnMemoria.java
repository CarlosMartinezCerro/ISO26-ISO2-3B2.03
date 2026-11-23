package es.uclm.esi.iso2.radiodiagnostico.prueba;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SolicitudSeguimientoRepositorioEnMemoria implements SolicitudSeguimientoRepositorio {

    private final Map<String, SolicitudSeguimiento> solicitudes = new LinkedHashMap<>();

    @Override
    public Optional<SolicitudSeguimiento> buscarPorId(String id) {
        return Optional.ofNullable(solicitudes.get(id));
    }

    @Override
    public List<SolicitudSeguimiento> buscarPorPaciente(String dniPaciente) {
        return solicitudes.values().stream()
                .filter(solicitud -> solicitud.dniPaciente().equalsIgnoreCase(dniPaciente))
                .toList();
    }

    @Override
    public void guardar(SolicitudSeguimiento solicitud) {
        solicitudes.put(solicitud.id(), solicitud);
    }
}
