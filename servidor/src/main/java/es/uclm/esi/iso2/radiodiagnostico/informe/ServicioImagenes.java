package es.uclm.esi.iso2.radiodiagnostico.informe;

import java.util.List;

/** Contrato del servidor para el CdU17 · Crear imagen y el CdU19 · Consultar imagen. */
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

    /**
     * CdU19: imágenes del paciente que ha creado el médico, de la más reciente a la más antigua.
     *
     * @throws DatosImagenInvalidosException si falta el médico o el paciente
     */
    List<Imagen> listarImagenesPaciente(String idMedico, String dniPaciente);

    /**
     * CdU19: devuelve una imagen concreta.
     *
     * @throws DatosImagenInvalidosException si falta el médico o la imagen
     * @throws ImagenNoEncontradaException si no existe
     * @throws AccesoDenegadoException si la imagen no es de un paciente del médico
     */
    Imagen consultarImagen(String idMedico, String idImagen);
}
