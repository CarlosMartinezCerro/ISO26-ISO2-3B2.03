package es.uclm.esi.iso2.radiodiagnostico.prueba;

import es.uclm.esi.iso2.radiodiagnostico.informe.ErrorComunicacionException;

/** Comunicación del cliente con el servidor para las pruebas de seguimiento periódicas. */
public interface ClienteSeguimientos {

    /**
     * Consulta las pruebas de seguimiento periódicas de un paciente (CdU11).
     *
     * @throws ErrorComunicacionException si no se puede hablar con el servidor
     */
    ResultadoConsultaSeguimientos consultarSeguimientos(String dniPaciente);
}
