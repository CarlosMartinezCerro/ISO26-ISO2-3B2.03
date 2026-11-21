package es.uclm.esi.iso2.radiodiagnostico.notificacion;

/** Los datos de la notificación no son válidos (campo vacío, texto demasiado largo...). */
public class NotificacionInvalidaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public NotificacionInvalidaException(String mensaje) {
        super(mensaje);
    }
}
