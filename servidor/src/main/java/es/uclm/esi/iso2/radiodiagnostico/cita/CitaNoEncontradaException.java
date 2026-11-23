package es.uclm.esi.iso2.radiodiagnostico.cita;

public class CitaNoEncontradaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public CitaNoEncontradaException(String mensaje) {
        super(mensaje);
    }
}
