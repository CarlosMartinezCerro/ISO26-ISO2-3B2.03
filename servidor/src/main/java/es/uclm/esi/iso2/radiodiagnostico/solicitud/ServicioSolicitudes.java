package es.uclm.esi.iso2.radiodiagnostico.solicitud;

import java.util.List;

public interface ServicioSolicitudes {

    List<String> listarPruebasDisponibles();

    SolicitudPrueba solicitar(String dniPaciente, DatosSolicitudPrueba datos);
}
