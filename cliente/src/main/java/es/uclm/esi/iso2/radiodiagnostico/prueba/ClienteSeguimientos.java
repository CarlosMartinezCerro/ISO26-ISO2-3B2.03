package es.uclm.esi.iso2.radiodiagnostico.prueba;

/** Comunicación del cliente con el servidor para el CdU13. */
public interface ClienteSeguimientos {

    /**
     * Envía la solicitud al servidor y devuelve su respuesta, tanto si la acepta como si no.
     *
     * @throws ErrorComunicacionException si no se puede contactar con el servidor
     */
    ResultadoSolicitudSeguimiento solicitar(DatosSolicitudSeguimiento datos);
}
