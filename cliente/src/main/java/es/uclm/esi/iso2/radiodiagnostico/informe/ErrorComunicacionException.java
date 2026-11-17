package es.uclm.esi.iso2.radiodiagnostico.informe;

/** El cliente no ha podido hablar con el servidor (caída de red, servidor parado...). */
public class ErrorComunicacionException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ErrorComunicacionException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
