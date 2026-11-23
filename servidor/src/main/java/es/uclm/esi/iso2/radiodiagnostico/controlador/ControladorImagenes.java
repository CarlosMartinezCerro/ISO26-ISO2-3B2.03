package es.uclm.esi.iso2.radiodiagnostico.controlador;

import es.uclm.esi.iso2.radiodiagnostico.modelo.informe.AccesoDenegadoException;
import es.uclm.esi.iso2.radiodiagnostico.modelo.informe.DatosImagen;
import es.uclm.esi.iso2.radiodiagnostico.modelo.informe.DatosImagenInvalidosException;
import es.uclm.esi.iso2.radiodiagnostico.modelo.informe.Imagen;
import es.uclm.esi.iso2.radiodiagnostico.modelo.informe.ImagenNoEncontradaException;
import es.uclm.esi.iso2.radiodiagnostico.modelo.informe.PacsNoDisponibleException;
import es.uclm.esi.iso2.radiodiagnostico.modelo.informe.ResultadoConsultaImagen;
import es.uclm.esi.iso2.radiodiagnostico.modelo.informe.ResultadoCreacionImagen;
import es.uclm.esi.iso2.radiodiagnostico.modelo.informe.ResultadoListadoImagenes;
import es.uclm.esi.iso2.radiodiagnostico.modelo.informe.ServicioImagenes;
import java.util.List;
import java.util.Objects;

/**
 * Controlador del servidor para el CdU17 · Crear imagen y el CdU19 · Consultar imagen.
 * Recibe las peticiones del cliente, llama al modelo y convierte sus
 * excepciones en respuestas con un mensaje que el cliente puede mostrar.
 */
public class ControladorImagenes {

    static final String ERROR_INTERNO =
            "No se ha podido acceder a las imágenes. Inténtelo de nuevo más tarde.";

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
        } catch (IllegalStateException e) {
            // Fallo de persistencia: no se muestran detalles internos al cliente.
            return new ResultadoCreacionImagen(false, ERROR_INTERNO);
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
        } catch (IllegalStateException e) {
            return new ResultadoListadoImagenes(false, ERROR_INTERNO, List.of());
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
        } catch (IllegalStateException e) {
            return new ResultadoConsultaImagen(false, ERROR_INTERNO, null);
        }
    }
}
