package es.uclm.esi.iso2.radiodiagnostico.modelo.solicitud;

public class SolicitudNoEncontradaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public SolicitudNoEncontradaException(String mensaje) {
        super(mensaje);
    }
}
