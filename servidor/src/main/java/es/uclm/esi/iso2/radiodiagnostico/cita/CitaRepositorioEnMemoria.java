package es.uclm.esi.iso2.radiodiagnostico.cita;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CitaRepositorioEnMemoria implements CitaRepositorio {

    private final List<Cita> citas = new ArrayList<>();

    @Override
    public void guardar(Cita cita) {
        citas.add(cita);
    }

    @Override
    public Optional<Cita> buscarPorSolicitud(String idSolicitud) {
        return citas.stream()
                .filter(cita -> cita.idSolicitud().equals(idSolicitud))
                .findFirst();
    }

    @Override
    public boolean existeCitaEn(String sala, LocalDateTime fechaHora) {
        return citas.stream()
                .anyMatch(cita -> cita.sala().equals(sala) && cita.fechaHora().equals(fechaHora));
    }

    @Override
    public Optional<Cita> buscarPorId(String id) {
        return citas.stream()
                .filter(cita -> cita.id().equals(id))
                .findFirst();
    }

    @Override
    public void actualizar(Cita cita) {
        citas.replaceAll(actual -> actual.id().equals(cita.id()) ? cita : actual);
    }
}
