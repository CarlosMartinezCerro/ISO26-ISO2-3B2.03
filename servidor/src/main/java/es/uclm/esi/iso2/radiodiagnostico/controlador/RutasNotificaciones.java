package es.uclm.esi.iso2.radiodiagnostico.controlador;

import es.uclm.esi.iso2.radiodiagnostico.notificacion.DatosNotificacion;
import es.uclm.esi.iso2.radiodiagnostico.notificacion.RespuestaListadoNotificaciones;
import es.uclm.esi.iso2.radiodiagnostico.notificacion.ResultadoNotificacion;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import java.util.Objects;

/**
 * API REST del CdU5 · Notificar (D-009).
 *
 * <pre>
 * POST /notificaciones
 *   cuerpo:    {"dniPaciente", "canal": "SMS" | "APLICACION", "asunto", "mensaje"}
 *   respuesta: {"correcto", "mensaje", "idNotificacion"}  con 201 si se envía y 400 si se rechaza
 *
 * GET /pacientes/{dni}/notificaciones
 *   respuesta: {"correcto", "mensaje", "notificaciones": [...]}  con 200, o 400 si se rechaza
 * </pre>
 */
public class RutasNotificaciones implements Rutas {

    public static final String RUTA_NOTIFICACIONES = "/notificaciones";
    public static final String RUTA_NOTIFICACIONES_PACIENTE = "/pacientes/{dni}/notificaciones";
    static final String PETICION_MAL_FORMADA = "La petición de notificación no tiene un formato válido.";

    private final ControladorNotificaciones controlador;

    public RutasNotificaciones(ControladorNotificaciones controlador) {
        this.controlador = Objects.requireNonNull(controlador, "El controlador es obligatorio");
    }

    @Override
    public void registrar(Javalin app) {
        app.post(RUTA_NOTIFICACIONES, this::notificar);
        app.get(RUTA_NOTIFICACIONES_PACIENTE, this::historialPaciente);
    }

    private void notificar(Context ctx) {
        DatosNotificacion datos;
        try {
            datos = ctx.bodyAsClass(DatosNotificacion.class);
        } catch (Exception e) {
            // JSON inválido o canal desconocido: Jackson lanza excepciones que Javalin no declara.
            ctx.status(HttpStatus.BAD_REQUEST).json(new ResultadoNotificacion(false, PETICION_MAL_FORMADA, null));
            return;
        }
        ResultadoNotificacion resultado = controlador.notificar(datos);
        ctx.status(resultado.correcto() ? HttpStatus.CREATED : HttpStatus.BAD_REQUEST).json(resultado);
    }

    private void historialPaciente(Context ctx) {
        RespuestaListadoNotificaciones respuesta = controlador.historialPaciente(ctx.pathParam("dni"));
        ctx.status(respuesta.correcto() ? HttpStatus.OK : HttpStatus.BAD_REQUEST).json(respuesta);
    }
}
