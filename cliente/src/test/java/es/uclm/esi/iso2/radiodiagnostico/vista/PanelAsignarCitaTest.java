package es.uclm.esi.iso2.radiodiagnostico.vista;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import es.uclm.esi.iso2.radiodiagnostico.cita.DatosAsignacionCita;
import es.uclm.esi.iso2.radiodiagnostico.cita.SolicitudPendiente;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tests de la vista del CdU25. No abren ninguna ventana: funcionan también sin pantalla. */
class PanelAsignarCitaTest {

    private static final List<SolicitudPendiente> SOLICITUDES = List.of(
            new SolicitudPendiente("SOL-002", "FICTICIO-002", "TAC", "URGENTE"),
            new SolicitudPendiente("SOL-001", "FICTICIO-001", "Radiografía", "ORDINARIO"));

    private AvisosFalsos avisos;
    private PanelAsignarCita panel;

    @BeforeEach
    void crearPanel() {
        avisos = new AvisosFalsos();
        panel = new PanelAsignarCita(avisos);
        panel.mostrarSolicitudes(SOLICITUDES);
    }

    @Test
    void muestraLasSolicitudesEnLaTabla() {
        assertEquals(2, panel.modeloTabla.getRowCount());
        assertEquals("SOL-002", panel.modeloTabla.getValueAt(0, 0));
        assertEquals("FICTICIO-002", panel.modeloTabla.getValueAt(0, 1));
        assertEquals("TAC", panel.modeloTabla.getValueAt(0, 2));
        assertEquals("URGENTE", panel.modeloTabla.getValueAt(0, 3));
    }

    @Test
    void recargarSustituyeLasFilasAnteriores() {
        panel.mostrarSolicitudes(SOLICITUDES.subList(1, 2));

        assertEquals(1, panel.modeloTabla.getRowCount());
    }

    @Test
    void laTablaNoSePuedeEditar() {
        assertFalse(panel.modeloTabla.isCellEditable(0, 0));
    }

    @Test
    void devuelveLosDatosDeLaSolicitudSeleccionada() {
        panel.tablaSolicitudes.setRowSelectionInterval(1, 1);
        rellenar(" 2026-12-01 ", "09:30", " SALA-RX-1 ");

        assertEquals(new DatosAsignacionCita("SOL-001", LocalDateTime.of(2026, 12, 1, 9, 30), "SALA-RX-1"),
                panel.obtenerDatos());
    }

    @Test
    void sinSolicitudSeleccionadaNoHayDatos() {
        rellenar("2026-12-01", "09:30", "SALA-RX-1");

        assertNull(panel.obtenerDatos());
    }

    @Test
    void conFechaMalEscritaNoHayDatos() {
        panel.tablaSolicitudes.setRowSelectionInterval(0, 0);
        rellenar("01/12/2026", "09:30", "SALA-RX-1");

        assertNull(panel.obtenerDatos());
    }

    @Test
    void conHoraMalEscritaNoHayDatos() {
        panel.tablaSolicitudes.setRowSelectionInterval(0, 0);
        rellenar("2026-12-01", "9 y media", "SALA-RX-1");

        assertNull(panel.obtenerDatos());
    }

    @Test
    void elExitoSeMuestraComoAvisoDeInformacion() {
        panel.mostrarExito("Cita asignada para la solicitud SOL-001.");

        assertEquals("Cita asignada", avisos.tituloInformacion);
        assertEquals("Cita asignada para la solicitud SOL-001.", avisos.informacion);
    }

    @Test
    void losErroresSeMuestranComoAvisoDeError() {
        panel.mostrarError("La sala ya está ocupada.");

        assertEquals("No se pudo asignar", avisos.tituloError);
        assertEquals("La sala ya está ocupada.", avisos.error);
    }

    @Test
    void losBotonesLlamanAlControlador() {
        int[] asignar = {0};
        int[] recargar = {0};
        panel.alPulsarAsignar(() -> asignar[0]++);
        panel.alPulsarRecargar(() -> recargar[0]++);

        panel.botonAsignar.doClick();
        panel.botonRecargar.doClick();
        panel.botonRecargar.doClick();

        assertEquals(1, asignar[0]);
        assertEquals(2, recargar[0]);
    }

    private void rellenar(String fecha, String hora, String sala) {
        panel.campoFecha.setText(fecha);
        panel.campoHora.setText(hora);
        panel.campoSala.setText(sala);
    }
}
