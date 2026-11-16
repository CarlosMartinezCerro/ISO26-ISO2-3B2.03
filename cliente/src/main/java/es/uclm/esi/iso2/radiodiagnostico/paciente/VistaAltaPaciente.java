package es.uclm.esi.iso2.radiodiagnostico.paciente;

public interface VistaAltaPaciente {

    DatosAltaPaciente obtenerDatos();

    void mostrarExito(String mensaje);

    void mostrarError(String mensaje);
}
