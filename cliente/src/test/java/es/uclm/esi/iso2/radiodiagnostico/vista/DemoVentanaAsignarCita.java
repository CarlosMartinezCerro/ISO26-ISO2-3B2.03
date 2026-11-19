package es.uclm.esi.iso2.radiodiagnostico.vista;

import es.uclm.esi.iso2.radiodiagnostico.cita.ClienteCitas;
import es.uclm.esi.iso2.radiodiagnostico.cita.DatosAsignacionCita;
import es.uclm.esi.iso2.radiodiagnostico.cita.ResultadoAsignacion;
import es.uclm.esi.iso2.radiodiagnostico.cita.SolicitudPendiente;
import es.uclm.esi.iso2.radiodiagnostico.controlador.ControladorAsignarCita;
import es.uclm.esi.iso2.radiodiagnostico.controlador.ControladorAsignarCitaImpl;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingUtilities;

/**
 * Demo para ver la ventana del CdU25 sin servidor.
 * Usa un ClienteCitas falso con datos ficticios (RGPD). No es código de producción.
 */
public final class DemoVentanaAsignarCita {

    private DemoVentanaAsignarCita() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            VentanaAsignarCita ventana = new VentanaAsignarCita();
            ControladorAsignarCita controlador =
                    new ControladorAsignarCitaImpl(ventana.panel(), new ClienteCitasFalso());
            ventana.panel().alPulsarAsignar(controlador::asignarCita);
            ventana.panel().alPulsarRecargar(controlador::cargarSolicitudes);
            controlador.cargarSolicitudes();
            ventana.setVisible(true);
        });
    }

    /** Simula el servidor en memoria con reglas básicas. */
    private static final class ClienteCitasFalso implements ClienteCitas {

        private final List<SolicitudPendiente> pendientes = new ArrayList<>(List.of(
                new SolicitudPendiente("SOL-002", "FICTICIO-002", "TAC", "URGENTE"),
                new SolicitudPendiente("SOL-003", "FICTICIO-003", "Ecografía", "PREFERENTE"),
                new SolicitudPendiente("SOL-001", "FICTICIO-001", "Radiografía", "ORDINARIO")));
        private final List<String> huecosOcupados = new ArrayList<>();

        @Override
        public List<SolicitudPendiente> obtenerSolicitudesPendientes() {
            return List.copyOf(pendientes);
        }

        @Override
        public ResultadoAsignacion asignarCita(DatosAsignacionCita datos) {
            if (datos.sala().isBlank()) {
                return new ResultadoAsignacion(false, "El campo sala es obligatorio.");
            }
            if (!datos.fechaHora().isAfter(LocalDateTime.now())) {
                return new ResultadoAsignacion(false, "El campo fechaHora debe ser una fecha futura.");
            }
            String hueco = datos.sala() + "@" + datos.fechaHora();
            if (huecosOcupados.contains(hueco)) {
                return new ResultadoAsignacion(false, "La sala " + datos.sala()
                        + " ya está ocupada el " + datos.fechaHora() + ".");
            }
            huecosOcupados.add(hueco);
            pendientes.removeIf(s -> s.id().equals(datos.idSolicitud()));
            return new ResultadoAsignacion(true, "Cita asignada para la solicitud "
                    + datos.idSolicitud() + " el " + datos.fechaHora() + " en " + datos.sala() + ".");
        }
    }
}
