package es.uclm.esi.iso2.radiodiagnostico.cita;

public class DatosAsignacionInvalidosException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DatosAsignacionInvalidosException(String mensaje) {
        super(mensaje);
    }
}
