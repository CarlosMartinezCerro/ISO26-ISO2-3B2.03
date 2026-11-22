package es.uclm.esi.iso2.radiodiagnostico.controlador;

import es.uclm.esi.iso2.radiodiagnostico.prueba.ResultadoConsultaSeguimientos;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import java.util.Objects;

/**
 * API REST del CdU11 (D-009). Usa la misma ruta que el CdU13
 * ({@link RutasSolicitudSeguimiento#RUTA_SEGUIMIENTOS}): POST crea un seguimiento y GET lo consulta.
 *
 * <pre>
 * GET /seguimientos?dniPaciente=12345678Z
 *   respuesta: {"correcto", "mensaje", "seguimientos": [...]}
 *              con 200 si la consulta es válida (aunque no haya seguimientos) y 400 si se rechaza
 * </pre>
 */
public class RutasConsultaSeguimientos implements Rutas {

    public static final String PARAMETRO_DNI = "dniPaciente";

    private final ControladorConsultaSeguimientos controlador;

    public RutasConsultaSeguimientos(ControladorConsultaSeguimientos controlador) {
        this.controlador = Objects.requireNonNull(controlador, "El controlador es obligatorio");
    }

    @Override
    public void registrar(Javalin app) {
        app.get(RutasSolicitudSeguimiento.RUTA_SEGUIMIENTOS, this::consultar);
    }

    private void consultar(Context ctx) {
        ResultadoConsultaSeguimientos resultado =
                controlador.consultarSeguimientos(ctx.queryParam(PARAMETRO_DNI));
        ctx.status(resultado.correcto() ? HttpStatus.OK : HttpStatus.BAD_REQUEST).json(resultado);
    }
}
