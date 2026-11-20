package es.uclm.esi.iso2.radiodiagnostico.informe;

public interface ClienteHistorialPruebas {

    ResultadoHistorialPruebas consultarPruebasHistoricas(String dniPaciente) throws ErrorComunicacionException;
}