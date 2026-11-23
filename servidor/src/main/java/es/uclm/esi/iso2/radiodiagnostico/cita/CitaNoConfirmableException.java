package es.uclm.esi.iso2.radiodiagnostico.cita;

public class CitaNoConfirmableException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public CitaNoConfirmableException(String mensaje) {
        super(mensaje);
    }
}
