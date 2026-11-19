package es.uclm.esi.iso2.radiodiagnostico.informe;

/** Resultado de consultar antecedentes a traves del servidor. */
public record ResultadoAntecedentes(
        boolean correcto,
        String mensaje,
        String antecedentes,
        String historialEspecialidad) {
}
