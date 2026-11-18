package es.uclm.esi.iso2.radiodiagnostico.informe;

import java.util.List;

public interface ServicioHistorialPruebas {

    List<PruebaHistorica> consultarPruebasHistoricas(String dniPaciente);
}