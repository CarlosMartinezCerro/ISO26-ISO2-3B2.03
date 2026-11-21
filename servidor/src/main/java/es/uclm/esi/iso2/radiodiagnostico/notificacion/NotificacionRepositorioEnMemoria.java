package es.uclm.esi.iso2.radiodiagnostico.notificacion;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class NotificacionRepositorioEnMemoria implements NotificacionRepositorio {

    private final List<Notificacion> notificaciones = new ArrayList<>();

    @Override
    public void guardar(Notificacion notificacion) {
        notificaciones.add(notificacion);
    }

    @Override
    public List<Notificacion> buscarPorPaciente(String dniPaciente) {
        return notificaciones.stream()
                .filter(notificacion -> notificacion.dniPaciente().equalsIgnoreCase(dniPaciente))
                .sorted(Comparator.comparing(Notificacion::fechaEnvio).thenComparing(Notificacion::id).reversed())
                .toList();
    }

    @Override
    public int contar() {
        return notificaciones.size();
    }
}
