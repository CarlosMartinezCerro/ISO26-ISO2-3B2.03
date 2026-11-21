package es.uclm.esi.iso2.radiodiagnostico.notificacion;

/** Pasarela simulada: no envía nada de verdad, solo deja constancia en la consola del servidor. */
public class PasarelaNotificacionesSimulada implements PasarelaNotificaciones {

    @Override
    public void enviar(Notificacion notificacion) {
        System.out.println("[Notificación simulada] " + notificacion.canal() + " a "
                + notificacion.destino() + ": " + notificacion.asunto());
    }
}
