package es.uclm.esi.iso2.radiodiagnostico.informe;

/** CdU19 · Consultar imagen. */
public class AccesoDenegadoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public AccesoDenegadoException(String mensaje) {
        super(mensaje);
    }
}
