package es.uclm.esi.iso2.radiodiagnostico.informe;

/** El sistema externo RIS/PACS no ha podido archivar la imagen. */
public class PacsNoDisponibleException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public PacsNoDisponibleException(String mensaje) {
        super(mensaje);
    }
}
