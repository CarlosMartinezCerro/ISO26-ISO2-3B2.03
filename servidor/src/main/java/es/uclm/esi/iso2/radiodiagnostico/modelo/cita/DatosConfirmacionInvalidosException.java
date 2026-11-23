package es.uclm.esi.iso2.radiodiagnostico.modelo.cita;

public class DatosConfirmacionInvalidosException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DatosConfirmacionInvalidosException(String mensaje) {
        super(mensaje);
    }
}
