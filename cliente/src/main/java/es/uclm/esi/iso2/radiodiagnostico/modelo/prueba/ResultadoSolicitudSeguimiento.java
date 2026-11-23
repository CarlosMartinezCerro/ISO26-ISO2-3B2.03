package es.uclm.esi.iso2.radiodiagnostico.modelo.prueba;

import java.util.List;

/** Respuesta del servidor tras intentar registrar una solicitud de seguimiento. */
public record ResultadoSolicitudSeguimiento(boolean correcto, String mensaje, String idSolicitud,
                                            List<RepeticionSeguimiento> repeticiones) {

    public ResultadoSolicitudSeguimiento {
        repeticiones = repeticiones == null ? List.of() : List.copyOf(repeticiones);
    }
}
