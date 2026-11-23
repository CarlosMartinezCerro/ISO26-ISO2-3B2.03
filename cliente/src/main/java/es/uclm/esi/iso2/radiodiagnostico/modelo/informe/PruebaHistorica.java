package es.uclm.esi.iso2.radiodiagnostico.modelo.informe;

import java.time.LocalDateTime;

public record PruebaHistorica(String idPrueba, String dniPaciente, String tipoPrueba,
                              String estado, String idInforme, String referenciaPacs,
                              LocalDateTime fecha) {
}