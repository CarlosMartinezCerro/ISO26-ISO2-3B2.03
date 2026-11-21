package es.uclm.esi.iso2.radiodiagnostico.notificacion;

/** La pasarela de SMS o de avisos no ha podido entregar la notificación. */
public class PasarelaNoDisponibleException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public PasarelaNoDisponibleException(String mensaje) {
        super(mensaje);
    }
}
