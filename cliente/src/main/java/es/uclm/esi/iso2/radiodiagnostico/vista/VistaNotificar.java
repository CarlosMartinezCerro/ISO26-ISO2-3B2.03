package es.uclm.esi.iso2.radiodiagnostico.vista;

import es.uclm.esi.iso2.radiodiagnostico.notificacion.DatosNotificacion;
import es.uclm.esi.iso2.radiodiagnostico.notificacion.NotificacionEnviada;
import java.util.List;

/** Lo que el controlador necesita de cualquier vista del CdU5 (escritorio, web...). */
public interface VistaNotificar {

    /** @return los datos introducidos, o null si no se ha elegido canal */
    DatosNotificacion obtenerDatos();

    String obtenerDniPaciente();

    void mostrarHistorial(List<NotificacionEnviada> notificaciones, String mensaje);

    void mostrarExito(String mensaje);

    void mostrarError(String mensaje);

    /** Vacía el asunto y el mensaje (se mantiene el paciente para ver su historial). */
    void limpiarMensaje();
}
