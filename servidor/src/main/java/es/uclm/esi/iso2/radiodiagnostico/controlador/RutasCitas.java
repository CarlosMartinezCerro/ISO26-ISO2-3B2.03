package es.uclm.esi.iso2.radiodiagnostico.controlador;

import es.uclm.esi.iso2.radiodiagnostico.cita.DatosAsignacionCita;
import es.uclm.esi.iso2.radiodiagnostico.cita.ResultadoAsignacion;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import java.util.Objects;

/**
 * API REST del CdU25 (D-009).
 *
 * <pre>
 * GET /solicitudes/pendientes
 *   respuesta: [{"id", "dniPaciente", "tipoPrueba", "prioridad", "estado"}]  con 200,
 *              ordenadas por prioridad (URGENTE, PREFERENTE, ORDINARIO)
 *
 * POST /citas
 *   cuerpo:    {"idSolicitud", "fechaHora": "aaaa-mm-ddThh:mm", "sala"}
 *   respuesta: {"correcto", "mensaje"}  con 201 si se asigna y 400 si se rechaza
 * </pre>
 */
public class RutasCitas implements Rutas {

    public static final String RUTA_SOLICITUDES_PENDIENTES = "/solicitudes/pendientes";
    public static final String RUTA_CITAS = "/citas";
    static final String PETICION_MAL_FORMADA = "La petición de cita no tiene un formato válido.";

    private final ControladorCitas controlador;

    public RutasCitas(ControladorCitas controlador) {
        this.controlador = Objects.requireNonNull(controlador, "El controlador es obligatorio");
    }

    @Override
    public void registrar(Javalin app) {
        app.get(RUTA_SOLICITUDES_PENDIENTES, ctx -> ctx.json(controlador.solicitudesPendientes()));
        app.post(RUTA_CITAS, this::asignarCita);
    }

    private void asignarCita(Context ctx) {
        DatosAsignacionCita datos;
        try {
            datos = ctx.bodyAsClass(DatosAsignacionCita.class);
        } catch (Exception e) {
            // Jackson lanza excepciones comprobadas (JSON inválido, fecha mal escrita...)
            // que Javalin no declara, por eso se captura Exception.
            ctx.status(HttpStatus.BAD_REQUEST).json(new ResultadoAsignacion(false, PETICION_MAL_FORMADA));
            return;
        }
        ResultadoAsignacion resultado = controlador.asignarCita(datos);
        ctx.status(resultado.correcto() ? HttpStatus.CREATED : HttpStatus.BAD_REQUEST).json(resultado);
    }
}
