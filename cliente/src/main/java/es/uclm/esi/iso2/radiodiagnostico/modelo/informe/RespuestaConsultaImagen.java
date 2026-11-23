package es.uclm.esi.iso2.radiodiagnostico.modelo.informe;

/** Respuesta del servidor al pedir una imagen (CdU19). imagen es null si correcto es false. */
public record RespuestaConsultaImagen(boolean correcto, String mensaje, ImagenConsultada imagen) {
}
