package es.uclm.esi.iso2.radiodiagnostico.persistencia;

/** La base de datos no está disponible o la operación ha fallado. */
public class ErrorBaseDatosException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ErrorBaseDatosException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
