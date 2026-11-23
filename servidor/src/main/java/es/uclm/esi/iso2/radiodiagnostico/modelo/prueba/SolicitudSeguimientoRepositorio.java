package es.uclm.esi.iso2.radiodiagnostico.modelo.prueba;

import java.util.List;
import java.util.Optional;

public interface SolicitudSeguimientoRepositorio {

    Optional<SolicitudSeguimiento> buscarPorId(String id);

    List<SolicitudSeguimiento> buscarPorPaciente(String dniPaciente);

    /** Guarda la solicitud, sustituyendo la que tenga el mismo id. */
    void guardar(SolicitudSeguimiento solicitud);
}
