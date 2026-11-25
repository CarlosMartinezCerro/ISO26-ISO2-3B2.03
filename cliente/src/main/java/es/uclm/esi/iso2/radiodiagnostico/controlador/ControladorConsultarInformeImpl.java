package es.uclm.esi.iso2.radiodiagnostico.controlador;

import es.uclm.esi.iso2.radiodiagnostico.informe.ClienteInformes;
import es.uclm.esi.iso2.radiodiagnostico.informe.ErrorComunicacionException;
import es.uclm.esi.iso2.radiodiagnostico.informe.InformeRespuesta;
import es.uclm.esi.iso2.radiodiagnostico.informe.InformeResumen;
import es.uclm.esi.iso2.radiodiagnostico.vista.VistaConsultarInforme;
import java.util.List;
import java.util.Objects;

public class ControladorConsultarInformeImpl
        implements ControladorConsultarInforme {

    private final VistaConsultarInforme vista;
    private final ClienteInformes cliente;

    public ControladorConsultarInformeImpl(
            VistaConsultarInforme vista, ClienteInformes cliente) {
        this.vista = Objects.requireNonNull(vista, "La vista es obligatoria");
        this.cliente = Objects.requireNonNull(cliente, "El cliente es obligatorio");
    }

    @Override
    public void listarInformes() {
        String dniPaciente = vista.obtenerDniPaciente();
        if (dniPaciente == null || dniPaciente.isBlank()) {
            vista.mostrarError("El DNI del paciente es obligatorio.");
            return;
        }

        try {
            List<InformeResumen> informes =
                    cliente.listarInformes(dniPaciente.trim());
            vista.mostrarInformes(informes);
            if (informes.isEmpty()) {
                vista.mostrarMensaje("No hay informes para este paciente.");
            }
        } catch (ErrorComunicacionException | IllegalArgumentException excepcion) {
            vista.mostrarError(excepcion.getMessage());
        }
    }

    @Override
    public void obtenerInforme() {
        InformeResumen resumen = vista.obtenerInformeSeleccionado();
        if (resumen == null) {
            vista.mostrarError("Seleccione un informe.");
            return;
        }

        try {
            InformeRespuesta informe = cliente.obtenerInforme(resumen.id());
            if (informe == null) {
                vista.mostrarError("El informe seleccionado ya no está disponible.");
                return;
            }
            vista.mostrarInforme(informe);
        } catch (ErrorComunicacionException | IllegalArgumentException excepcion) {
            vista.mostrarError(excepcion.getMessage());
        }
    }
}
