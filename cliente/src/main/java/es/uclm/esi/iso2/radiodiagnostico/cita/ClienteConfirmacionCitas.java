package es.uclm.esi.iso2.radiodiagnostico.cita;

/** Comunicación del cliente con el servidor para el CdU2. */
public interface ClienteConfirmacionCitas {

    /** @throws ErrorComunicacionException si no se puede contactar con el servidor */
    ResultadoConfirmacion confirmarCita(DatosConfirmacionCita datos);
}
