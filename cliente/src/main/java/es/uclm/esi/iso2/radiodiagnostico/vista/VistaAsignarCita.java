package es.uclm.esi.iso2.radiodiagnostico.vista;

import es.uclm.esi.iso2.radiodiagnostico.cita.DatosAsignacionCita;
import es.uclm.esi.iso2.radiodiagnostico.cita.SolicitudPendiente;
import java.util.List;

/** Lo que el controlador necesita de cualquier vista del CdU25 (escritorio, web...). */
public interface VistaAsignarCita {

    void mostrarSolicitudes(List<SolicitudPendiente> solicitudes);

    /** @return los datos introducidos, o null si están incompletos o mal escritos */
    DatosAsignacionCita obtenerDatos();

    void mostrarExito(String mensaje);

    void mostrarError(String mensaje);
}
