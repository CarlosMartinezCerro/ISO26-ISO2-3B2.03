package es.uclm.esi.iso2.radiodiagnostico.controlador;

import es.uclm.esi.iso2.radiodiagnostico.modelo.cita.ClienteCitas;
import es.uclm.esi.iso2.radiodiagnostico.modelo.cita.DatosAsignacionCita;
import es.uclm.esi.iso2.radiodiagnostico.modelo.cita.ErrorComunicacionException;
import es.uclm.esi.iso2.radiodiagnostico.modelo.cita.ResultadoAsignacion;
import es.uclm.esi.iso2.radiodiagnostico.vista.VistaAsignarCita;
import java.util.Objects;

public class ControladorAsignarCitaImpl implements ControladorAsignarCita {

    static final String MENSAJE_SIN_CONEXION =
            "No se ha podido conectar con el servidor. Inténtelo de nuevo más tarde.";

    private final VistaAsignarCita vista;
    private final ClienteCitas cliente;

    public ControladorAsignarCitaImpl(VistaAsignarCita vista, ClienteCitas cliente) {
        this.vista = Objects.requireNonNull(vista, "La vista es obligatoria");
        this.cliente = Objects.requireNonNull(cliente, "El cliente es obligatorio");
    }

    @Override
    public void cargarSolicitudes() {
        try {
            vista.mostrarSolicitudes(cliente.obtenerSolicitudesPendientes());
        } catch (ErrorComunicacionException e) {
            vista.mostrarError(MENSAJE_SIN_CONEXION);
        }
    }

    @Override
    public void asignarCita() {
        DatosAsignacionCita datos = vista.obtenerDatos();
        if (datos == null) {
            vista.mostrarError("Seleccione una solicitud e indique fecha, hora y sala.");
            return;
        }
        try {
            ResultadoAsignacion resultado = cliente.asignarCita(datos);
            if (resultado.correcto()) {
                vista.mostrarExito(resultado.mensaje());
                cargarSolicitudes();
            } else {
                vista.mostrarError(resultado.mensaje());
            }
        } catch (ErrorComunicacionException e) {
            vista.mostrarError(MENSAJE_SIN_CONEXION);
        }
    }
}
