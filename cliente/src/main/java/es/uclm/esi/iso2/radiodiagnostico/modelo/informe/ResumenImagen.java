package es.uclm.esi.iso2.radiodiagnostico.modelo.informe;

import java.time.LocalDateTime;

/** Lo que la vista muestra de cada imagen en el listado (CdU19). */
public record ResumenImagen(String id, String idInforme, String tipoPrueba,
                            LocalDateTime fechaCreacion, String nombreFichero) {
}
