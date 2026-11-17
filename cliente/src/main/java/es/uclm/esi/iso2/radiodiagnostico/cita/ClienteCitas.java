package es.uclm.esi.iso2.radiodiagnostico.cita;

import java.util.List;

/** Comunicación del cliente con el servidor para el CdU25. */
public interface ClienteCitas {

    /** @throws ErrorComunicacionException si no se puede contactar con el servidor */
    List<SolicitudPendiente> obtenerSolicitudesPendientes();

    /** @throws ErrorComunicacionException si no se puede contactar con el servidor */
    ResultadoAsignacion asignarCita(DatosAsignacionCita datos);
}
