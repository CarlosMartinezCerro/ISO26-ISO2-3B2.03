package es.uclm.esi.iso2.radiodiagnostico.modelo.informe;

import java.time.LocalDateTime;

/** Imagen completa que el médico abre (CdU19), con el contenido para poder verla. */
public record ImagenConsultada(String id, String idInforme, String dniPaciente, String tipoPrueba,
                               LocalDateTime fechaCreacion, String nombreFichero,
                               byte[] contenido, String referenciaPacs) {
}
