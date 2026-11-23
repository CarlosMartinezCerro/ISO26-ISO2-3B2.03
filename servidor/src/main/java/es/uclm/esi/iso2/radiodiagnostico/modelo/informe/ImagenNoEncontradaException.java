package es.uclm.esi.iso2.radiodiagnostico.modelo.informe;

/** CdU19 · Consultar imagen. */
public class ImagenNoEncontradaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ImagenNoEncontradaException(String mensaje) {
        super(mensaje);
    }
}
