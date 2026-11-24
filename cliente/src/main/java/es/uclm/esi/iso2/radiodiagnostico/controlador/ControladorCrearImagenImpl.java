package es.uclm.esi.iso2.radiodiagnostico.controlador;

import es.uclm.esi.iso2.radiodiagnostico.informe.ClienteImagenes;
import es.uclm.esi.iso2.radiodiagnostico.informe.DatosImagen;
import es.uclm.esi.iso2.radiodiagnostico.informe.ErrorComunicacionException;
import es.uclm.esi.iso2.radiodiagnostico.informe.ResultadoCreacionImagen;
import es.uclm.esi.iso2.radiodiagnostico.vista.VistaCrearImagen;
import java.util.Objects;

public class ControladorCrearImagenImpl implements ControladorCrearImagen {

    static final String MENSAJE_SIN_CONEXION =
            "No se ha podido conectar con el servidor. Inténtelo de nuevo más tarde.";
    static final String MENSAJE_DATOS_INCOMPLETOS =
            "Indique informe, paciente, médico, tipo de prueba y seleccione un fichero.";

    private final VistaCrearImagen vista;
    private final ClienteImagenes cliente;

    public ControladorCrearImagenImpl(VistaCrearImagen vista, ClienteImagenes cliente) {
        this.vista = Objects.requireNonNull(vista, "La vista es obligatoria");
        this.cliente = Objects.requireNonNull(cliente, "El cliente es obligatorio");
    }

    @Override
    public void crearImagen() {
        DatosImagen datos = vista.obtenerDatos();
        if (datos == null) {
            vista.mostrarError(MENSAJE_DATOS_INCOMPLETOS);
            return;
        }
        try {
            ResultadoCreacionImagen resultado = cliente.crearImagen(datos);
            if (resultado.correcto()) {
                vista.mostrarExito(resultado.mensaje());
                vista.limpiar();
            } else {
                vista.mostrarError(resultado.mensaje());
            }
        } catch (ErrorComunicacionException e) {
            vista.mostrarError(MENSAJE_SIN_CONEXION);
        }
    }
}
