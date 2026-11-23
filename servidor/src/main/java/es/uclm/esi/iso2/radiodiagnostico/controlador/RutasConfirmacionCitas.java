package es.uclm.esi.iso2.radiodiagnostico.controlador;

import es.uclm.esi.iso2.radiodiagnostico.modelo.cita.DatosConfirmacionCita;
import es.uclm.esi.iso2.radiodiagnostico.modelo.cita.ResultadoConfirmacion;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import java.util.Objects;

/**
 * API REST del CdU2 (D-009).
 *
 * <pre>
 * POST /citas/{idCita}/confirmacion
 *   cuerpo:    {"dniPaciente"}   (si también llega "idCita" en el cuerpo, se ignora: manda la URL)
 *   respuesta: {"correcto", "mensaje"}  con 200 si se confirma y 400 si se rechaza
 * </pre>
 */
public class RutasConfirmacionCitas implements Rutas {

    public static final String RUTA_CONFIRMACION = "/citas/{idCita}/confirmacion";
    static final String PETICION_MAL_FORMADA = "La petición de confirmación no tiene un formato válido.";

    private final ControladorConfirmacionCitas controlador;

    public RutasConfirmacionCitas(ControladorConfirmacionCitas controlador) {
        this.controlador = Objects.requireNonNull(controlador, "El controlador es obligatorio");
    }

    @Override
    public void registrar(Javalin app) {
        app.post(RUTA_CONFIRMACION, this::confirmarCita);
    }

    private void confirmarCita(Context ctx) {
        DatosConfirmacionCita datos;
        try {
            datos = ctx.bodyAsClass(DatosConfirmacionCita.class);
        } catch (Exception e) {
            // Jackson lanza excepciones comprobadas que Javalin no declara, por eso se captura Exception.
            ctx.status(HttpStatus.BAD_REQUEST).json(new ResultadoConfirmacion(false, PETICION_MAL_FORMADA));
            return;
        }
        ResultadoConfirmacion resultado =
                controlador.confirmarCita(ctx.pathParam("idCita"), datos.dniPaciente());
        ctx.status(resultado.correcto() ? HttpStatus.OK : HttpStatus.BAD_REQUEST).json(resultado);
    }
}
