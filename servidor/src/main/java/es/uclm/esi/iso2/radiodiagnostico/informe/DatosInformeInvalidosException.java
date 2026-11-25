package es.uclm.esi.iso2.radiodiagnostico.informe;

public class DatosInformeInvalidosException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DatosInformeInvalidosException(String mensaje) {
        super(mensaje);
    }
}
