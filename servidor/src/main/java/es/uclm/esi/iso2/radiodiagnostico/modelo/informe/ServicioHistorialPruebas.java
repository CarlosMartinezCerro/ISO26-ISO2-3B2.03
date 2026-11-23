package es.uclm.esi.iso2.radiodiagnostico.modelo.informe;

import java.util.List;

public interface ServicioHistorialPruebas {

    List<PruebaHistorica> consultarPruebasHistoricas(String dniPaciente);
}