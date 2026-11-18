package es.uclm.esi.iso2.radiodiagnostico.controlador;

import es.uclm.esi.iso2.radiodiagnostico.paciente.DatosAltaPaciente;
import es.uclm.esi.iso2.radiodiagnostico.paciente.ResultadoAlta;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import java.util.Objects;

/**
 * API REST del CdU22 (D-009).
 *
 * <pre>
 * POST /pacientes
 *   cuerpo:    {"dni", "nombre", "apellidos", "fechaNacimiento": "aaaa-mm-dd", "telefono"}
 *   respuesta: {"correcto", "mensaje"}  con 201 si se da de alta y 400 si se rechaza
 * </pre>
 */
public class RutasPacientes {

    public static final String RUTA_PACIENTES = "/pacientes";
    static final String PETICION_MAL_FORMADA = "La petición de alta no tiene un formato válido.";

    private final ControladorPacientes controlador;

    public RutasPacientes(ControladorPacientes controlador) {
        this.controlador = Objects.requireNonNull(controlador, "El controlador es obligatorio");
    }

    public void registrar(Javalin app) {
        app.post(RUTA_PACIENTES, this::darDeAlta);
    }

    private void darDeAlta(Context ctx) {
        DatosAltaPaciente datos;
        try {
            datos = ctx.bodyAsClass(DatosAltaPaciente.class);
        } catch (Exception e) {
            // Jackson lanza excepciones comprobadas (JSON inválido, fecha mal escrita...)
            // que Javalin no declara, por eso se captura Exception.
            ctx.status(HttpStatus.BAD_REQUEST).json(new ResultadoAlta(false, PETICION_MAL_FORMADA));
            return;
        }
        ResultadoAlta resultado = controlador.darDeAlta(datos);
        ctx.status(resultado.correcto() ? HttpStatus.CREATED : HttpStatus.BAD_REQUEST).json(resultado);
    }
}
