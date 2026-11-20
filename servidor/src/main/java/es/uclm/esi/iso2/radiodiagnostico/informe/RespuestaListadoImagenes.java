package es.uclm.esi.iso2.radiodiagnostico.informe;

import java.util.List;

/** Respuesta REST del listado del CdU19 (GET /pacientes/{dni}/imagenes). */
public record RespuestaListadoImagenes(boolean correcto, String mensaje, List<ResumenImagen> imagenes) {

    public static RespuestaListadoImagenes de(ResultadoListadoImagenes resultado) {
        return new RespuestaListadoImagenes(resultado.correcto(), resultado.mensaje(),
                resultado.imagenes().stream().map(ResumenImagen::de).toList());
    }
}
