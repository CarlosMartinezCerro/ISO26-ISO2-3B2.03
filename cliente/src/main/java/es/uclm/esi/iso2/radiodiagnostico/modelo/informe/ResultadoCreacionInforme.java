package es.uclm.esi.iso2.radiodiagnostico.modelo.informe;

/** Resultado de crear un informe a traves del servidor. */
public record ResultadoCreacionInforme(
        boolean correcto,
        String mensaje,
        InformeRespuesta informe) {
}
