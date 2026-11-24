package es.uclm.esi.iso2.radiodiagnostico.solicitud;

import java.util.List;

public interface ClienteSolicitudes {

    List<String> obtenerPruebasDisponibles();

    ResultadoSolicitud solicitar(DatosSolicitudPrueba datos);
}
