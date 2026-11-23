package es.uclm.esi.iso2.radiodiagnostico.modelo.prueba;

import java.util.List;

/**
 * Respuesta que el servidor devuelve al cliente tras intentar crear una solicitud de seguimiento.
 * Si no es correcta, {@code idSolicitud} es nulo y {@code repeticiones} está vacía.
 */
public record ResultadoSolicitudSeguimiento(boolean correcto, String mensaje, String idSolicitud,
                                            List<RepeticionSeguimiento> repeticiones) {

    public ResultadoSolicitudSeguimiento {
        repeticiones = repeticiones == null ? List.of() : List.copyOf(repeticiones);
    }

    public static ResultadoSolicitudSeguimiento error(String mensaje) {
        return new ResultadoSolicitudSeguimiento(false, mensaje, null, List.of());
    }
}
