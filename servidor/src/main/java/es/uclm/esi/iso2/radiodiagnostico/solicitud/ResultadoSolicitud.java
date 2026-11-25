package es.uclm.esi.iso2.radiodiagnostico.solicitud;

public record ResultadoSolicitud(boolean correcto, String mensaje,
                                 String idSolicitud, EstadoSolicitud estado) {
}
