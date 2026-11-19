
package es.uclm.esi.iso2.radiodiagnostico.controlador;

import es.uclm.esi.iso2.radiodiagnostico.cita.ClienteCitas;
import es.uclm.esi.iso2.radiodiagnostico.cita.CitaPaciente;
import es.uclm.esi.iso2.radiodiagnostico.cita.ErrorComunicacionException;
import es.uclm.esi.iso2.radiodiagnostico.vista.VistaConsultarCitas;
import java.util.List;
import java.util.Objects;

public class ControladorConsultarCitasImpl
        implements ControladorConsultarCitas {

    private final VistaConsultarCitas vista;
    private final ClienteCitas cliente;

    public ControladorConsultarCitasImpl(
            VistaConsultarCitas vista, ClienteCitas cliente) {
        this.vista = Objects.requireNonNull(vista);
        this.cliente = Objects.requireNonNull(cliente);
    }

    @Override
    public void consultarCitas() {
        try {
            List<CitaPaciente> citas = cliente.consultarCitas();

            vista.mostrarCitas(citas);

            if (citas.isEmpty()) {
                vista.mostrarMensaje(
                        "No tienes citas programadas.");
            }

        } catch (ErrorComunicacionException e) {
            vista.mostrarError(
                    "No se ha podido realizar la consulta. "
                    + "Comprueba que has iniciado sesión e inténtalo de nuevo.");
        }
    }
}

