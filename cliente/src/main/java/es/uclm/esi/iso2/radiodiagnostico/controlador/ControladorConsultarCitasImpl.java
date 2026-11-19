
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
        String dni = vista.obtenerDniPaciente();

        if (dni == null || dni.isBlank()) {
            vista.mostrarError("Introduzca el DNI del paciente.");
            return;
        }

        try {
            List<CitaPaciente> citas =
                    cliente.consultarCitas(dni.trim());

            vista.mostrarCitas(citas);

            if (citas.isEmpty()) {
                vista.mostrarMensaje(
                        "El paciente no tiene citas programadas.");
            }

        } catch (ErrorComunicacionException e) {
            vista.mostrarError(
                    "No se ha podido conectar con el servidor. "
                    + "Inténtelo de nuevo más tarde.");
        }
    }
}

