
package es.uclm.esi.iso2.radiodiagnostico.cita;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CitaRepositorio {

    void guardar(Cita cita);

    Optional<Cita> buscarPorSolicitud(String idSolicitud);

    boolean existeCitaEn(String sala, LocalDateTime fechaHora);

    List<Cita> buscarPorDniPaciente(String dniPaciente);
}
