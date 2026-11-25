package es.uclm.esi.iso2.radiodiagnostico.prueba;

public class DatosSeguimientoInvalidosException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DatosSeguimientoInvalidosException(String mensaje) {
        super(mensaje);
    }
}
