
package es.uclm.esi.iso2.radiodiagnostico.cita;

import java.util.List;

/** Comunicación del cliente con el servidor para las operaciones de citas. */
public interface ClienteCitas {

    /** Obtiene las solicitudes pendientes de asignación. */
    List<SolicitudPendiente> obtenerSolicitudesPendientes();

    /** Asigna una cita a una solicitud. */
    ResultadoAsignacion asignarCita(DatosAsignacionCita datos);

    /** Consulta las citas de un paciente. */
    List<CitaPaciente> consultarCitas();
}

