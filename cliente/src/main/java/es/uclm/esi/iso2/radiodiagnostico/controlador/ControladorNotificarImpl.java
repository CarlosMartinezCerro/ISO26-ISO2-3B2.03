package es.uclm.esi.iso2.radiodiagnostico.controlador;

import es.uclm.esi.iso2.radiodiagnostico.notificacion.ClienteNotificaciones;
import es.uclm.esi.iso2.radiodiagnostico.notificacion.DatosNotificacion;
import es.uclm.esi.iso2.radiodiagnostico.notificacion.ErrorComunicacionException;
import es.uclm.esi.iso2.radiodiagnostico.notificacion.RespuestaListadoNotificaciones;
import es.uclm.esi.iso2.radiodiagnostico.notificacion.ResultadoNotificacion;
import es.uclm.esi.iso2.radiodiagnostico.vista.VistaNotificar;
import java.util.List;
import java.util.Objects;

public class ControladorNotificarImpl implements ControladorNotificar {

    static final String MENSAJE_SIN_CONEXION =
            "No se ha podido conectar con el servidor. Inténtelo de nuevo más tarde.";
    static final String MENSAJE_SIN_CANAL = "Elija el canal por el que se envía la notificación.";
    static final String MENSAJE_SIN_DNI = "Indique el DNI del paciente.";

    private final VistaNotificar vista;
    private final ClienteNotificaciones cliente;

    public ControladorNotificarImpl(VistaNotificar vista, ClienteNotificaciones cliente) {
        this.vista = Objects.requireNonNull(vista, "La vista es obligatoria");
        this.cliente = Objects.requireNonNull(cliente, "El cliente es obligatorio");
    }

    @Override
    public void notificar() {
        DatosNotificacion datos = vista.obtenerDatos();
        if (datos == null) {
            vista.mostrarError(MENSAJE_SIN_CANAL);
            return;
        }
        try {
            ResultadoNotificacion resultado = cliente.notificar(datos);
            if (resultado.correcto()) {
                vista.mostrarExito(resultado.mensaje());
                vista.limpiarMensaje();
                verHistorial();
            } else {
                vista.mostrarError(resultado.mensaje());
            }
        } catch (ErrorComunicacionException e) {
            vista.mostrarError(MENSAJE_SIN_CONEXION);
        }
    }

    @Override
    public void verHistorial() {
        String dni = vista.obtenerDniPaciente();
        if (dni == null || dni.isBlank()) {
            vista.mostrarError(MENSAJE_SIN_DNI);
            return;
        }
        try {
            RespuestaListadoNotificaciones respuesta = cliente.historialPaciente(dni.trim());
            if (respuesta.correcto()) {
                vista.mostrarHistorial(respuesta.notificaciones(), respuesta.mensaje());
            } else {
                vista.mostrarHistorial(List.of(), "");
                vista.mostrarError(respuesta.mensaje());
            }
        } catch (ErrorComunicacionException e) {
            vista.mostrarError(MENSAJE_SIN_CONEXION);
        }
    }
}
