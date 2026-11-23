package es.uclm.esi.iso2.radiodiagnostico.controlador;

import es.uclm.esi.iso2.radiodiagnostico.modelo.informe.ClienteConsultaImagenes;
import es.uclm.esi.iso2.radiodiagnostico.modelo.informe.ErrorComunicacionException;
import es.uclm.esi.iso2.radiodiagnostico.modelo.informe.RespuestaConsultaImagen;
import es.uclm.esi.iso2.radiodiagnostico.modelo.informe.RespuestaListadoImagenes;
import es.uclm.esi.iso2.radiodiagnostico.vista.VistaConsultarImagen;
import java.util.Objects;

public class ControladorConsultarImagenImpl implements ControladorConsultarImagen {

    static final String MENSAJE_SIN_CONEXION =
            "No se ha podido conectar con el servidor. Inténtelo de nuevo más tarde.";
    static final String MENSAJE_SIN_DNI = "Introduzca el DNI del paciente.";
    static final String MENSAJE_SIN_SELECCION = "Seleccione una imagen de la lista.";

    private final VistaConsultarImagen vista;
    private final ClienteConsultaImagenes cliente;
    private final String idMedico;

    /** @param idMedico médico que ha iniciado sesión en el cliente */
    public ControladorConsultarImagenImpl(VistaConsultarImagen vista, ClienteConsultaImagenes cliente,
                                          String idMedico) {
        this.vista = Objects.requireNonNull(vista, "La vista es obligatoria");
        this.cliente = Objects.requireNonNull(cliente, "El cliente es obligatorio");
        this.idMedico = Objects.requireNonNull(idMedico, "El médico es obligatorio");
    }

    @Override
    public void buscarImagenes() {
        String dni = vista.obtenerDniPaciente();
        if (dni == null || dni.isBlank()) {
            vista.mostrarError(MENSAJE_SIN_DNI);
            return;
        }
        try {
            RespuestaListadoImagenes respuesta = cliente.obtenerImagenesPaciente(idMedico, dni);
            if (respuesta.correcto()) {
                vista.mostrarImagenes(respuesta.imagenes(), respuesta.mensaje());
            } else {
                vista.mostrarError(respuesta.mensaje());
            }
        } catch (ErrorComunicacionException e) {
            vista.mostrarError(MENSAJE_SIN_CONEXION);
        }
    }

    @Override
    public void verImagen() {
        String idImagen = vista.obtenerImagenSeleccionada();
        if (idImagen == null) {
            vista.mostrarError(MENSAJE_SIN_SELECCION);
            return;
        }
        try {
            RespuestaConsultaImagen respuesta = cliente.obtenerImagen(idMedico, idImagen);
            if (respuesta.correcto()) {
                vista.mostrarImagen(respuesta.imagen());
            } else {
                vista.mostrarError(respuesta.mensaje());
            }
        } catch (ErrorComunicacionException e) {
            vista.mostrarError(MENSAJE_SIN_CONEXION);
        }
    }
}
