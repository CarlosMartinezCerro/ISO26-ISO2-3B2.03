package es.uclm.esi.iso2.radiodiagnostico.prueba;

/**
 * Comunicación del cliente con el servidor para el CdU11 · Consultar prueba de seguimiento
 * periódica. Es una interfaz aparte de {@link ClienteSeguimientos} (CdU13) para que cada caso de
 * uso dependa solo de lo que usa; las dos las implementa {@link ClienteSeguimientosHttp}.
 */
public interface ClienteConsultaSeguimientos {

    /**
     * Consulta las pruebas de seguimiento periódicas de un paciente (GET /seguimientos).
     *
     * @throws ErrorComunicacionException si no se puede contactar con el servidor
     */
    ResultadoConsultaSeguimientos consultarSeguimientos(String dniPaciente);
}
