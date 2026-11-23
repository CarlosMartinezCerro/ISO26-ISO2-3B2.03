package es.uclm.esi.iso2.radiodiagnostico.controlador;

/** Operacion MVC del cliente para crear un informe. */
public interface ControladorCrearInforme {
    void crearInforme();

    void consultarAntecedentes(String dniPaciente, String especialidad);
}
