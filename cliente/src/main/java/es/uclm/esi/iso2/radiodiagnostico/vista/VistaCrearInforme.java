package es.uclm.esi.iso2.radiodiagnostico.vista;

import es.uclm.esi.iso2.radiodiagnostico.informe.Informe;

public interface VistaCrearInforme {

    Informe obtenerInforme();

    String obtenerDniPaciente();

    String obtenerEspecialidad();

    void cargarAntecedentes(String antecedentes, String historialEspecialidad);

    void habilitarCamposInforme(boolean habilitado);

    void mostrarExito(String mensaje);

    void mostrarError(String mensaje);

    void limpiar();
}
