package es.uclm.esi.iso2.radiodiagnostico.controlador;

import es.uclm.esi.iso2.radiodiagnostico.modelo.informe.DatosImagen;
import es.uclm.esi.iso2.radiodiagnostico.modelo.informe.RespuestaListadoImagenes;
import es.uclm.esi.iso2.radiodiagnostico.modelo.informe.ResultadoConsultaImagen;
import es.uclm.esi.iso2.radiodiagnostico.modelo.informe.ResultadoCreacionImagen;
import es.uclm.esi.iso2.radiodiagnostico.modelo.informe.ResultadoListadoImagenes;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import java.util.Objects;

/**
 * API REST del CdU17 y del CdU19 (D-009).
 *
 * <pre>
 * POST /imagenes                                                         (CdU17)
 *   cuerpo:    {"idInforme", "dniPaciente", "idMedico", "tipoPrueba", "nombreFichero",
 *               "contenido": bytes del fichero en Base64}
 *   respuesta: {"correcto", "mensaje"}  con 201 si se crea y 400 si se rechaza
 *
 * GET /pacientes/{dni}/imagenes      cabecera X-Medico                   (CdU19)
 *   respuesta: {"correcto", "mensaje", "imagenes": [{"id", "idInforme", "tipoPrueba",
 *               "fechaCreacion", "nombreFichero"}]}  con 200, o 400 si se rechaza
 *
 * GET /imagenes/{id}                 cabecera X-Medico                   (CdU19)
 *   respuesta: {"correcto", "mensaje", "imagen": {... "contenido" en Base64}}
 *              con 200, o 400 si se rechaza (no existe, es de otro médico...)
 * </pre>
 */
public class RutasImagenes implements Rutas {

    public static final String RUTA_IMAGENES = "/imagenes";
    public static final String RUTA_IMAGENES_PACIENTE = "/pacientes/{dni}/imagenes";
    public static final String RUTA_IMAGEN = "/imagenes/{id}";
    /** Médico que consulta; hasta que exista el inicio de sesión lo indica el cliente. */
    public static final String CABECERA_MEDICO = "X-Medico";
    static final String PETICION_MAL_FORMADA = "La petición de imagen no tiene un formato válido.";

    private final ControladorImagenes controlador;

    public RutasImagenes(ControladorImagenes controlador) {
        this.controlador = Objects.requireNonNull(controlador, "El controlador es obligatorio");
    }

    @Override
    public void registrar(Javalin app) {
        app.post(RUTA_IMAGENES, this::crearImagen);
        app.get(RUTA_IMAGENES_PACIENTE, this::imagenesPaciente);
        app.get(RUTA_IMAGEN, this::consultarImagen);
    }

    private void imagenesPaciente(Context ctx) {
        ResultadoListadoImagenes resultado =
                controlador.imagenesPaciente(ctx.header(CABECERA_MEDICO), ctx.pathParam("dni"));
        ctx.status(resultado.correcto() ? HttpStatus.OK : HttpStatus.BAD_REQUEST)
                .json(RespuestaListadoImagenes.de(resultado));
    }

    private void consultarImagen(Context ctx) {
        ResultadoConsultaImagen resultado =
                controlador.consultarImagen(ctx.header(CABECERA_MEDICO), ctx.pathParam("id"));
        ctx.status(resultado.correcto() ? HttpStatus.OK : HttpStatus.BAD_REQUEST).json(resultado);
    }

    private void crearImagen(Context ctx) {
        DatosImagen datos;
        try {
            datos = ctx.bodyAsClass(DatosImagen.class);
        } catch (Exception e) {
            // Jackson lanza excepciones comprobadas (JSON inválido, Base64 incorrecto...)
            // que Javalin no declara, por eso se captura Exception.
            ctx.status(HttpStatus.BAD_REQUEST).json(new ResultadoCreacionImagen(false, PETICION_MAL_FORMADA));
            return;
        }
        ResultadoCreacionImagen resultado = controlador.crearImagen(datos);
        ctx.status(resultado.correcto() ? HttpStatus.CREATED : HttpStatus.BAD_REQUEST).json(resultado);
    }
}
