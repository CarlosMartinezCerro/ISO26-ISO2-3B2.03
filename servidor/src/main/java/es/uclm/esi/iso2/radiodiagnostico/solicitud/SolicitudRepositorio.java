package es.uclm.esi.iso2.radiodiagnostico.solicitud;

import java.util.List;
import java.util.Optional;

public interface SolicitudRepositorio {

    Optional<SolicitudPrueba> buscarPorId(String id);

    List<SolicitudPrueba> buscarPorEstado(EstadoSolicitud estado);

    /** Guarda la solicitud, sustituyendo la que tenga el mismo id. */
    void guardar(SolicitudPrueba solicitud);
}
