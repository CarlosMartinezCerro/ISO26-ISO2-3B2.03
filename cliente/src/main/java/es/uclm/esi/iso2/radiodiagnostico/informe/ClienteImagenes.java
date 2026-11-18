package es.uclm.esi.iso2.radiodiagnostico.informe;

/** Comunicación del cliente con el servidor para el CdU17. */
public interface ClienteImagenes {

    /** @throws ErrorComunicacionException si no se puede contactar con el servidor */
    ResultadoCreacionImagen crearImagen(DatosImagen datos);
}
