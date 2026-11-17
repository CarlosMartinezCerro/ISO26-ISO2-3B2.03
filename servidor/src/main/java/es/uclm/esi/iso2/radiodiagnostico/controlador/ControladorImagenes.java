package es.uclm.esi.iso2.radiodiagnostico.controlador;

import es.uclm.esi.iso2.radiodiagnostico.informe.DatosImagen;
import es.uclm.esi.iso2.radiodiagnostico.informe.DatosImagenInvalidosException;
import es.uclm.esi.iso2.radiodiagnostico.informe.Imagen;
import es.uclm.esi.iso2.radiodiagnostico.informe.PacsNoDisponibleException;
import es.uclm.esi.iso2.radiodiagnostico.informe.ResultadoCreacionImagen;
import es.uclm.esi.iso2.radiodiagnostico.informe.ServicioImagenes;
import java.util.Objects;

/**
 * Controlador del servidor para el CdU17 · Crear imagen.
 * Recibe las peticiones del cliente, llama al modelo y convierte sus
 * excepciones en respuestas con un mensaje que el cliente puede mostrar.
 */
public class ControladorImagenes {

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
        }
    }
}
