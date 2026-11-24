package es.uclm.esi.iso2.radiodiagnostico.controlador;

import es.uclm.esi.iso2.radiodiagnostico.prueba.DatosSolicitudSeguimiento;
import es.uclm.esi.iso2.radiodiagnostico.prueba.ResultadoSolicitudSeguimiento;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import java.util.Objects;

/**
 * API REST del CdU13 (D-009).
 *
 * <pre>
 * POST /seguimientos
 *   cuerpo:    {"dniPaciente", "dniMedico", "tipoPrueba", "indicacionClinica", "justificacion",
 *               "prioridad": "URGENTE|PREFERENTE|ORDINARIO", "cada": N,
 *               "unidad": "DIAS|SEMANAS|MESES", "fechaInicio": "aaaa-mm-dd",
 *               "numeroRepeticiones": N  o  "fechaLimite": "aaaa-mm-dd"}
 *   respuesta: {"correcto", "mensaje", "idSolicitud", "repeticiones"}
 *              con 201 si se registra y 400 si se rechaza
 * </pre>
 */
public class RutasSolicitudSeguimiento implements Rutas {

    public static final String RUTA_SEGUIMIENTOS = "/seguimientos";
    static final String PETICION_MAL_FORMADA =
            "La petición de solicitud de seguimiento no tiene un formato válido.";

    private final ControladorSolicitudSeguimiento controlador;

    public RutasSolicitudSeguimiento(ControladorSolicitudSeguimiento controlador) {
        this.controlador = Objects.requireNonNull(controlador, "El controlador es obligatorio");
    }

    @Override
    public void registrar(Javalin app) {
        app.post(RUTA_SEGUIMIENTOS, this::solicitar);
    }

    private void solicitar(Context ctx) {
        DatosSolicitudSeguimiento datos;
        try {
            datos = ctx.bodyAsClass(DatosSolicitudSeguimiento.class);
        } catch (Exception e) {
            // Jackson lanza excepciones comprobadas (JSON inválido, fecha o enumerado mal
            // escritos...) que Javalin no declara, por eso se captura Exception.
            ctx.status(HttpStatus.BAD_REQUEST).json(
                    ResultadoSolicitudSeguimiento.error(PETICION_MAL_FORMADA));
            return;
        }
        ResultadoSolicitudSeguimiento resultado = controlador.solicitar(datos);
        ctx.status(resultado.correcto() ? HttpStatus.CREATED : HttpStatus.BAD_REQUEST).json(resultado);
    }
}
