package es.uclm.esi.iso2.radiodiagnostico.modelo.informe;

/** Resultado REST de la operacion de crear informe. */
public record ResultadoCreacionInforme(
        boolean correcto,
        String mensaje,
        InformeRespuesta informe) {
}
