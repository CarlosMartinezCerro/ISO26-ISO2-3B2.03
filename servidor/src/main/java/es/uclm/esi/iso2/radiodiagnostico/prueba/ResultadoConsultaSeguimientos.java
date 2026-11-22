package es.uclm.esi.iso2.radiodiagnostico.prueba;

import java.util.List;

/**
 * Respuesta del servidor al cliente en el CdU11, con la misma forma que
 * {@code ResultadoHistorialPruebas} del CdU10. Si no es correcta, {@code seguimientos} está vacía.
 */
public record ResultadoConsultaSeguimientos(boolean correcto, String mensaje,
                                            List<SeguimientoPeriodico> seguimientos) {

    public ResultadoConsultaSeguimientos {
        seguimientos = seguimientos == null ? List.of() : List.copyOf(seguimientos);
    }

    public static ResultadoConsultaSeguimientos error(String mensaje) {
        return new ResultadoConsultaSeguimientos(false, mensaje, List.of());
    }
}
