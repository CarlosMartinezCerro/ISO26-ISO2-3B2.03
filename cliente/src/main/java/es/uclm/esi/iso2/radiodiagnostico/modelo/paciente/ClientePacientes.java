package es.uclm.esi.iso2.radiodiagnostico.modelo.paciente;

/** Comunicación del cliente con el servidor para el CdU22. */
public interface ClientePacientes {

    /**
     * Envía los datos al servidor y devuelve su respuesta, tanto si acepta el alta como si no.
     *
     * @throws ErrorComunicacionException si no se puede contactar con el servidor
     */
    ResultadoAlta darDeAlta(DatosAltaPaciente datos);
}
