package es.uclm.esi.iso2.radiodiagnostico.informe;

import java.time.LocalDateTime;

/**
 * Lo que viaja de cada imagen en el listado del CdU19: todo menos el contenido,
 * que puede ocupar varios MB y solo se envía al consultar una imagen concreta.
 */
public record ResumenImagen(String id, String idInforme, String tipoPrueba,
                            LocalDateTime fechaCreacion, String nombreFichero) {

    public static ResumenImagen de(Imagen imagen) {
        return new ResumenImagen(imagen.id(), imagen.idInforme(), imagen.tipoPrueba(),
                imagen.fechaCreacion(), imagen.nombreFichero());
    }
}
