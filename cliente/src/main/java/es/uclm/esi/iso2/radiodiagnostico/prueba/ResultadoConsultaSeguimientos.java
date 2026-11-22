package es.uclm.esi.iso2.radiodiagnostico.prueba;

import java.util.List;

/** Respuesta del servidor a GET /seguimientos (CdU11). */
public record ResultadoConsultaSeguimientos(boolean correcto, String mensaje,
                                            List<SeguimientoPeriodico> seguimientos) {

    public ResultadoConsultaSeguimientos {
        seguimientos = seguimientos == null ? List.of() : List.copyOf(seguimientos);
    }
}
