package es.uclm.esi.iso2.radiodiagnostico.controlador;

import es.uclm.esi.iso2.radiodiagnostico.informe.DatosImagen;
import es.uclm.esi.iso2.radiodiagnostico.informe.ResultadoCreacionImagen;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import java.util.Objects;

/**
 * API REST del CdU17 (D-009).
 *
 * <pre>
 * POST /imagenes
 *   cuerpo:    {"idInforme", "dniPaciente", "idMedico", "tipoPrueba", "nombreFichero",
 *               "contenido": bytes del fichero en Base64}
 *   respuesta: {"correcto", "mensaje"}  con 201 si se crea y 400 si se rechaza
 * </pre>
 */
public class RutasImagenes implements Rutas {

    public static final String RUTA_IMAGENES = "/imagenes";
    static final String PETICION_MAL_FORMADA = "La petición de imagen no tiene un formato válido.";

    private final ControladorImagenes controlador;

    public RutasImagenes(ControladorImagenes controlador) {
        this.controlador = Objects.requireNonNull(controlador, "El controlador es obligatorio");
    }

    @Override
    public void registrar(Javalin app) {
        app.post(RUTA_IMAGENES, this::crearImagen);
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
