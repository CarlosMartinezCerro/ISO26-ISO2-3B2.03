package es.uclm.esi.iso2.radiodiagnostico.informe;

import java.util.List;

public record ResultadoHistorialPruebas(boolean correcto, String mensaje, List<PruebaHistorica> pruebas) {
}