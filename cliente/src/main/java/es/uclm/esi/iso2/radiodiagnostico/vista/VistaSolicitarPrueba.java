package es.uclm.esi.iso2.radiodiagnostico.vista;

import java.util.List;

public interface VistaSolicitarPrueba {

    void mostrarPruebas(List<String> pruebas);
    String obtenerTipoPrueba();
    String obtenerPrioridad();
    void establecerEnviando(boolean enviando);
    void mostrarExito(String mensaje);
    void mostrarError(String mensaje);
}
