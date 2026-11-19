package es.uclm.esi.iso2.radiodiagnostico.api;

import es.uclm.esi.iso2.radiodiagnostico.cita.DatosAsignacionCita;
import es.uclm.esi.iso2.radiodiagnostico.cita.ResultadoAsignacion;
import es.uclm.esi.iso2.radiodiagnostico.controlador.ControladorCitas;
import es.uclm.esi.iso2.radiodiagnostico.controlador.ControladorImagenes;
import es.uclm.esi.iso2.radiodiagnostico.informe.DatosImagen;
import es.uclm.esi.iso2.radiodiagnostico.informe.Imagen;
import es.uclm.esi.iso2.radiodiagnostico.informe.ResultadoCreacionImagen;
import es.uclm.esi.iso2.radiodiagnostico.informe.ResultadoListadoImagenes;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Operaciones de la API REST (D-009) para el CdU25, el CdU19 y el CdU17.
 * Recibe y devuelve texto JSON y delega en los controladores; no depende de Javalin,
 * así se puede probar sin levantar el servidor. Las rutas se declaran en ServidorRadiodiagnostico.
 *
 * <pre>
 * GET  /api/solicitudes/pendientes                      CdU25: solicitudes sin cita
 * POST /api/citas                     cuerpo: DatosAsignacionCita        CdU25: asignar cita
 * GET  /api/pacientes/{dni}/imagenes  cabecera X-Medico  CdU19: imágenes del paciente
 * GET  /api/imagenes/{id}             cabecera X-Medico  CdU19: una imagen con su contenido
 * POST /api/imagenes                  cuerpo: DatosImagen                CdU17: crear imagen
 * </pre>
 * Siempre se responde 200 con {"correcto": true/false, "mensaje": ...}, igual que los controladores.
 */
public class ApiRest {

    public static final String CABECERA_MEDICO = "X-Medico";
    static final String MENSAJE_JSON_INVALIDO = "Los datos enviados no tienen un formato válido.";

    /** Lo que viaja de cada imagen en el listado: todo menos el contenido, que puede pesar mucho. */
    public record ResumenImagen(String id, String idInforme, String tipoPrueba,
                                LocalDateTime fechaCreacion, String nombreFichero) {
    }

    public record ListadoImagenes(boolean correcto, String mensaje, List<ResumenImagen> imagenes) {
    }

    private final ControladorCitas citas;
    private final ControladorImagenes imagenes;

    public ApiRest(ControladorCitas citas, ControladorImagenes imagenes) {
        this.citas = Objects.requireNonNull(citas, "El controlador de citas es obligatorio");
        this.imagenes = Objects.requireNonNull(imagenes, "El controlador de imágenes es obligatorio");
    }

    /** CdU25 · GET /api/solicitudes/pendientes */
    public String solicitudesPendientes() {
        return Json.escribir(citas.solicitudesPendientes());
    }

    /** CdU25 · POST /api/citas */
    public String asignarCita(String cuerpo) {
        try {
            return Json.escribir(citas.asignarCita(Json.leer(cuerpo, DatosAsignacionCita.class)));
        } catch (JsonInvalidoException e) {
            return Json.escribir(new ResultadoAsignacion(false, MENSAJE_JSON_INVALIDO));
        }
    }

    /** CdU19 · GET /api/pacientes/{dni}/imagenes */
    public String imagenesPaciente(String idMedico, String dniPaciente) {
        ResultadoListadoImagenes resultado = imagenes.imagenesPaciente(idMedico, dniPaciente);
        List<ResumenImagen> resumen = resultado.imagenes().stream().map(ApiRest::resumir).toList();
        return Json.escribir(new ListadoImagenes(resultado.correcto(), resultado.mensaje(), resumen));
    }

    /** CdU19 · GET /api/imagenes/{id} */
    public String consultarImagen(String idMedico, String idImagen) {
        return Json.escribir(imagenes.consultarImagen(idMedico, idImagen));
    }

    /** CdU17 · POST /api/imagenes (contenido en Base64) */
    public String crearImagen(String cuerpo) {
        try {
            return Json.escribir(imagenes.crearImagen(Json.leer(cuerpo, DatosImagen.class)));
        } catch (JsonInvalidoException e) {
            return Json.escribir(new ResultadoCreacionImagen(false, MENSAJE_JSON_INVALIDO));
        }
    }

    private static ResumenImagen resumir(Imagen imagen) {
        return new ResumenImagen(imagen.id(), imagen.idInforme(), imagen.tipoPrueba(),
                imagen.fechaCreacion(), imagen.nombreFichero());
    }
}
