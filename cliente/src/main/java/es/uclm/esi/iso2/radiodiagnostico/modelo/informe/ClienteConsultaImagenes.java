package es.uclm.esi.iso2.radiodiagnostico.modelo.informe;

/**
 * Comunicación del cliente con el servidor para el CdU19 · Consultar imagen.
 * Va separada de {@link ClienteImagenes} (CdU17) para que cada caso de uso
 * dependa solo de lo que usa; la misma clase de comunicaciones puede implementar ambas.
 */
public interface ClienteConsultaImagenes {

    /** @throws ErrorComunicacionException si no se puede contactar con el servidor */
    RespuestaListadoImagenes obtenerImagenesPaciente(String idMedico, String dniPaciente);

    /** @throws ErrorComunicacionException si no se puede contactar con el servidor */
    RespuestaConsultaImagen obtenerImagen(String idMedico, String idImagen);
}
