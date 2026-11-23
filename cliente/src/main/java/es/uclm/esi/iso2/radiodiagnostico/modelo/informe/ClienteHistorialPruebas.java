package es.uclm.esi.iso2.radiodiagnostico.modelo.informe;

public interface ClienteHistorialPruebas {

    ResultadoHistorialPruebas consultarPruebasHistoricas(String dniPaciente) throws ErrorComunicacionException;
}