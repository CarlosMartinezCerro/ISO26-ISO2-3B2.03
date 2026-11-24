package es.uclm.esi.iso2.radiodiagnostico.controlador;

import es.uclm.esi.iso2.radiodiagnostico.solicitud.DatosSolicitudPrueba;
import es.uclm.esi.iso2.radiodiagnostico.solicitud.DatosSolicitudInvalidosException;
import es.uclm.esi.iso2.radiodiagnostico.solicitud.EstadoSolicitud;
import es.uclm.esi.iso2.radiodiagnostico.solicitud.ResultadoSolicitud;
import es.uclm.esi.iso2.radiodiagnostico.solicitud.ServicioSolicitudes;
import java.util.List;
import java.util.Objects;

public class ControladorSolicitudes {

    private final ServicioSolicitudes servicio;

    public ControladorSolicitudes(ServicioSolicitudes servicio) {
        this.servicio = Objects.requireNonNull(servicio);
    }

    public List<String> pruebasDisponibles() {
        return servicio.listarPruebasDisponibles();
    }

    public ResultadoSolicitud solicitar(String dniPaciente, DatosSolicitudPrueba datos) {
        try {
            var solicitud = servicio.solicitar(dniPaciente, datos);
            return new ResultadoSolicitud(true,
                    "Solicitud registrada correctamente.", solicitud.id(), solicitud.estado());
        } catch (DatosSolicitudInvalidosException e) {
            return new ResultadoSolicitud(false, e.getMessage(), null, null);
        }
    }
}
