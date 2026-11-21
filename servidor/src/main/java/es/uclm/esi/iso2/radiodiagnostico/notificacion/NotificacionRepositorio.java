package es.uclm.esi.iso2.radiodiagnostico.notificacion;

import java.util.List;

public interface NotificacionRepositorio {

    void guardar(Notificacion notificacion);

    /** @return las notificaciones del paciente, de la más reciente a la más antigua */
    List<Notificacion> buscarPorPaciente(String dniPaciente);

    int contar();
}
