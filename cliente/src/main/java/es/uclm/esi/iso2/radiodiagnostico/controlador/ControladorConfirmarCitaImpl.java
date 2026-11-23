package es.uclm.esi.iso2.radiodiagnostico.controlador;

import es.uclm.esi.iso2.radiodiagnostico.modelo.cita.ClienteConfirmacionCitas;
import es.uclm.esi.iso2.radiodiagnostico.modelo.cita.DatosConfirmacionCita;
import es.uclm.esi.iso2.radiodiagnostico.modelo.cita.ErrorComunicacionException;
import es.uclm.esi.iso2.radiodiagnostico.modelo.cita.ResultadoConfirmacion;
import es.uclm.esi.iso2.radiodiagnostico.vista.VistaConfirmarCita;
import java.util.Objects;

public class ControladorConfirmarCitaImpl implements ControladorConfirmarCita {

    static final String MENSAJE_SIN_CONEXION =
            "No se ha podido conectar con el servidor. Inténtelo de nuevo más tarde.";
    static final String MENSAJE_DATOS_INCOMPLETOS = "Indique el código de la cita y su DNI.";

    private final VistaConfirmarCita vista;
    private final ClienteConfirmacionCitas cliente;

    public ControladorConfirmarCitaImpl(VistaConfirmarCita vista, ClienteConfirmacionCitas cliente) {
        this.vista = Objects.requireNonNull(vista, "La vista es obligatoria");
        this.cliente = Objects.requireNonNull(cliente, "El cliente es obligatorio");
    }

    @Override
    public void confirmarCita() {
        DatosConfirmacionCita datos = vista.obtenerDatos();
        if (datos == null) {
            vista.mostrarError(MENSAJE_DATOS_INCOMPLETOS);
            return;
        }
        try {
            ResultadoConfirmacion resultado = cliente.confirmarCita(datos);
            if (resultado.correcto()) {
                vista.mostrarExito(resultado.mensaje());
            } else {
                vista.mostrarError(resultado.mensaje());
            }
        } catch (ErrorComunicacionException e) {
            vista.mostrarError(MENSAJE_SIN_CONEXION);
        }
    }
}
