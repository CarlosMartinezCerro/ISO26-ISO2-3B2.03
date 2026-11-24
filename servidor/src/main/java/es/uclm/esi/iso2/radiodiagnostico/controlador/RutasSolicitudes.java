package es.uclm.esi.iso2.radiodiagnostico.controlador;

import es.uclm.esi.iso2.radiodiagnostico.solicitud.DatosSolicitudPrueba;
import es.uclm.esi.iso2.radiodiagnostico.solicitud.ResultadoSolicitud;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import java.util.Map;
import java.util.Objects;

public class RutasSolicitudes implements Rutas {

    public static final String RUTA_SOLICITUDES = "/solicitudes";
    public static final String RUTA_PRUEBAS_DISPONIBLES = "/pruebas-disponibles";
    private final ControladorSolicitudes controlador;

    public RutasSolicitudes(ControladorSolicitudes controlador) {
        this.controlador = Objects.requireNonNull(controlador);
    }

    @Override
    public void registrar(Javalin app) {
        app.get(RUTA_PRUEBAS_DISPONIBLES,
                ctx -> ctx.json(controlador.pruebasDisponibles()));
        app.post(RUTA_SOLICITUDES, this::solicitar);
    }

    private void solicitar(Context ctx) {
        String dniPaciente = ctx.sessionAttribute("dniPaciente");
        if (dniPaciente == null || dniPaciente.isBlank()) {
            ctx.status(HttpStatus.UNAUTHORIZED)
                    .json(Map.of("correcto", false, "mensaje", "El paciente no está autenticado."));
            return;
        }

        DatosSolicitudPrueba datos;
        try {
            datos = ctx.bodyAsClass(DatosSolicitudPrueba.class);
        } catch (Exception e) {
            ctx.status(HttpStatus.BAD_REQUEST).json(new ResultadoSolicitud(
                    false, "La petición no tiene un formato válido.", null, null));
            return;
        }

        try {
            ResultadoSolicitud resultado = controlador.solicitar(dniPaciente, datos);
            ctx.status(resultado.correcto() ? HttpStatus.CREATED : HttpStatus.BAD_REQUEST)
                    .json(resultado);
        } catch (RuntimeException e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json(new ResultadoSolicitud(
                    false, "No se pudo registrar la solicitud.", null, null));
        }
    }
}
