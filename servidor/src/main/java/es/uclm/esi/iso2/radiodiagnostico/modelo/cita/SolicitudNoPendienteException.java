package es.uclm.esi.iso2.radiodiagnostico.modelo.cita;

public class SolicitudNoPendienteException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public SolicitudNoPendienteException(String mensaje) {
        super(mensaje);
    }
}
