package es.uclm.esi.iso2.radiodiagnostico.modelo.informe;

import java.util.List;

/** Respuesta del servidor al pedir las imágenes de un paciente (CdU19). */
public record RespuestaListadoImagenes(boolean correcto, String mensaje,
                                       List<ResumenImagen> imagenes) {
}
