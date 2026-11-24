package es.uclm.esi.iso2.radiodiagnostico.vista;

import es.uclm.esi.iso2.radiodiagnostico.informe.PruebaHistorica;
import java.util.List;

public interface VistaHistorialPruebas {

    String obtenerDniPaciente();

    void mostrarPruebas(List<PruebaHistorica> pruebas);

    void mostrarMensaje(String mensaje);

    void mostrarError(String mensaje);

    void limpiar();
}