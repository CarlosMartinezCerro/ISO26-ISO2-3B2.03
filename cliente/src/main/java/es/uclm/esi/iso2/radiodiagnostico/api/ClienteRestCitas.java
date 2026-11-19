package es.uclm.esi.iso2.radiodiagnostico.api;

import es.uclm.esi.iso2.radiodiagnostico.cita.ClienteCitas;
import es.uclm.esi.iso2.radiodiagnostico.cita.DatosAsignacionCita;
import es.uclm.esi.iso2.radiodiagnostico.cita.ErrorComunicacionException;
import es.uclm.esi.iso2.radiodiagnostico.cita.ResultadoAsignacion;
import es.uclm.esi.iso2.radiodiagnostico.cita.SolicitudPendiente;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

/** CdU25 por REST: implementa ClienteCitas llamando a la API del servidor. */
public class ClienteRestCitas implements ClienteCitas {

    private static final String MENSAJE_ERROR = "No se ha podido comunicar con el servidor.";

    private final ClienteHttp http;

    public ClienteRestCitas(ClienteHttp http) {
        this.http = Objects.requireNonNull(http, "El cliente HTTP es obligatorio");
    }

    @Override
    public List<SolicitudPendiente> obtenerSolicitudesPendientes() {
        try {
            return List.of(Json.leer(http.get("/api/solicitudes/pendientes", null), SolicitudPendiente[].class));
        } catch (IOException | JsonInvalidoException e) {
            throw new ErrorComunicacionException(MENSAJE_ERROR, e);
        }
    }

    @Override
    public ResultadoAsignacion asignarCita(DatosAsignacionCita datos) {
        try {
            return Json.leer(http.post("/api/citas", Json.escribir(datos)), ResultadoAsignacion.class);
        } catch (IOException | JsonInvalidoException e) {
            throw new ErrorComunicacionException(MENSAJE_ERROR, e);
        }
    }
}
