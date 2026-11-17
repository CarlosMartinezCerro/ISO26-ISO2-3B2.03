package es.uclm.esi.iso2.radiodiagnostico.informe;

/** Respuesta del servidor al pedir una imagen concreta (CdU19). imagen es null si hay error. */
public record ResultadoConsultaImagen(boolean correcto, String mensaje, Imagen imagen) {
}
