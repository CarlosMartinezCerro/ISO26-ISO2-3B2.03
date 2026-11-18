package es.uclm.esi.iso2.radiodiagnostico.informe;

/**
 * Sistema externo RIS/PACS donde se archivan las imágenes médicas.
 * Mientras no haya integración real se usa {@link SistemaPacsSimulado}.
 */
public interface SistemaPacs {

    /**
     * Archiva la imagen en el PACS.
     *
     * @return la referencia con la que el PACS identifica la imagen
     * @throws PacsNoDisponibleException si el PACS no responde
     */
    String archivar(Imagen imagen);
}
