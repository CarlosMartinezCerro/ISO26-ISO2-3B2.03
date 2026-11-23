package es.uclm.esi.iso2.radiodiagnostico.modelo.informe;

public class DatosImagenInvalidosException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DatosImagenInvalidosException(String mensaje) {
        super(mensaje);
    }
}
