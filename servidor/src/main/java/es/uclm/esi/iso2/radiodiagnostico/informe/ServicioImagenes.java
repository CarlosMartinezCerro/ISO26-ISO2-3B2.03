package es.uclm.esi.iso2.radiodiagnostico.informe;

import java.util.List;

/** Contrato del servidor para el CdU17 · Crear imagen. */
public interface ServicioImagenes {

    /**
     * Crea una imagen asociada a un informe, la archiva en RIS/PACS y la guarda.
     *
     * @throws DatosImagenInvalidosException si falta algún dato, el fichero está vacío
     *         o el formato no está permitido (.dcm, .png, .jpg, .jpeg)
     * @throws PacsNoDisponibleException si el PACS no puede archivarla (no se guarda nada)
     */
    Imagen crearImagen(DatosImagen datos);

    List<Imagen> listarImagenesDeInforme(String idInforme);
}
