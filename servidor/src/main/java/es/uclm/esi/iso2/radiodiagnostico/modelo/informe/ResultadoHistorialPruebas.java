package es.uclm.esi.iso2.radiodiagnostico.modelo.informe;

import java.util.List;

public record ResultadoHistorialPruebas(boolean correcto, String mensaje, List<PruebaHistorica> pruebas) {
}