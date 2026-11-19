package es.uclm.esi.iso2.radiodiagnostico.api;

/** El cuerpo de una petición no es un JSON válido. */
public class JsonInvalidoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public JsonInvalidoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
