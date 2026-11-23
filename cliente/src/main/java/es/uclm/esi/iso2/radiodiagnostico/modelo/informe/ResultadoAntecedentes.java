package es.uclm.esi.iso2.radiodiagnostico.modelo.informe;

/** Resultado de consultar antecedentes a traves del servidor. */
public record ResultadoAntecedentes(
        boolean correcto,
        String mensaje,
        String antecedentes,
        String historialEspecialidad) {
}
