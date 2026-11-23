package es.uclm.esi.iso2.radiodiagnostico.informe;

import java.util.List;

/** Respuesta del servidor al pedir las imágenes de un paciente (CdU19). */
public record ResultadoListadoImagenes(boolean correcto, String mensaje, List<Imagen> imagenes) {
}
