package es.uclm.esi.iso2.radiodiagnostico.cita;

import java.time.LocalDateTime;
import java.util.Optional;

public interface CitaRepositorio {

    void guardar(Cita cita);

    Optional<Cita> buscarPorSolicitud(String idSolicitud);

    Optional<Cita> buscarPorId(String id);

    /** Sustituye la cita con el mismo id por la versión recibida. */
    void actualizar(Cita cita);

    boolean existeCitaEn(String sala, LocalDateTime fechaHora);
}
