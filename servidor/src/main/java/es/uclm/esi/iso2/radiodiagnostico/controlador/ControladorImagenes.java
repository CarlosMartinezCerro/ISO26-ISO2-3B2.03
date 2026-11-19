package es.uclm.esi.iso2.radiodiagnostico.controlador;

import es.uclm.esi.iso2.radiodiagnostico.informe.AccesoDenegadoException;
import es.uclm.esi.iso2.radiodiagnostico.informe.DatosImagen;
import es.uclm.esi.iso2.radiodiagnostico.informe.DatosImagenInvalidosException;
import es.uclm.esi.iso2.radiodiagnostico.informe.Imagen;
import es.uclm.esi.iso2.radiodiagnostico.informe.ImagenNoEncontradaException;
import es.uclm.esi.iso2.radiodiagnostico.informe.PacsNoDisponibleException;
import es.uclm.esi.iso2.radiodiagnostico.informe.ResultadoConsultaImagen;
import es.uclm.esi.iso2.radiodiagnostico.informe.ResultadoCreacionImagen;
import es.uclm.esi.iso2.radiodiagnostico.informe.ResultadoListadoImagenes;
import es.uclm.esi.iso2.radiodiagnostico.informe.ServicioImagenes;
import es.uclm.esi.iso2.radiodiagnostico.persistencia.ErrorBaseDatosException;
import java.util.List;
import java.util.Objects;

/**
 * Controlador del servidor para el CdU17 · Crear imagen y el CdU19 · Consultar imagen.
 * Recibe las peticiones del cliente, llama al modelo y convierte sus
 * excepciones en respuestas con un mensaje que el cliente puede mostrar.
 */
public class ControladorImagenes {

    static final String MENSAJE_BASE_DATOS =
            "No se ha podido acceder a la base de datos. Inténtelo de nuevo más tarde.";

    private final ServicioImagenes servicio;

    public ControladorImagenes(ServicioImagenes servicio) {
        this.servicio = Objects.requireNonNull(servicio, "El servicio de imágenes es obligatorio");
    }

    public ResultadoCreacionImagen crearImagen(DatosImagen datos) {
        try {
            Imagen imagen = servicio.crearImagen(datos);
            return new ResultadoCreacionImagen(true, "Imagen " + imagen.id()
                    + " creada para el informe " + imagen.idInforme() + ".");
        } catch (DatosImagenInvalidosException | PacsNoDisponibleException e) {
            return new ResultadoCreacionImagen(false, e.getMessage());
        } catch (ErrorBaseDatosException e) {
            return new ResultadoCreacionImagen(false, MENSAJE_BASE_DATOS);
        }
    }

    /** CdU19: imágenes de un paciente que puede ver el médico. */
    public ResultadoListadoImagenes imagenesPaciente(String idMedico, String dniPaciente) {
        try {
            List<Imagen> imagenes = servicio.listarImagenesPaciente(idMedico, dniPaciente);
            String mensaje = imagenes.isEmpty()
                    ? "El paciente " + dniPaciente + " no tiene imágenes disponibles."
                    : imagenes.size() + " imagen(es) encontradas.";
            return new ResultadoListadoImagenes(true, mensaje, imagenes);
        } catch (DatosImagenInvalidosException e) {
            return new ResultadoListadoImagenes(false, e.getMessage(), List.of());
        } catch (ErrorBaseDatosException e) {
            return new ResultadoListadoImagenes(false, MENSAJE_BASE_DATOS, List.of());
        }
    }

    /** CdU19: una imagen concreta. */
    public ResultadoConsultaImagen consultarImagen(String idMedico, String idImagen) {
        try {
            return new ResultadoConsultaImagen(true, "Imagen recuperada.",
                    servicio.consultarImagen(idMedico, idImagen));
        } catch (DatosImagenInvalidosException | ImagenNoEncontradaException
                 | AccesoDenegadoException e) {
            return new ResultadoConsultaImagen(false, e.getMessage(), null);
        } catch (ErrorBaseDatosException e) {
            return new ResultadoConsultaImagen(false, MENSAJE_BASE_DATOS, null);
        }
    }
}
