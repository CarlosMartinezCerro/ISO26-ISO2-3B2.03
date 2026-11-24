package es.uclm.esi.iso2.radiodiagnostico.informe;

import java.time.LocalDate;

public record InformeResumen(long id, String especialidad, LocalDate fecha) {
}
