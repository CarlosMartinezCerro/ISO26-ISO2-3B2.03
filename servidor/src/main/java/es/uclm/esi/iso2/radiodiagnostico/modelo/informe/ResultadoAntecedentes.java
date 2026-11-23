package es.uclm.esi.iso2.radiodiagnostico.modelo.informe;

/** Resultado REST de la consulta de antecedentes clinicos. */
public record ResultadoAntecedentes(
        boolean correcto,
        String mensaje,
        String antecedentes,
        String historialEspecialidad) {
}
