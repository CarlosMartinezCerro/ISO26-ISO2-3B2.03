
package es.uclm.esi.iso2.radiodiagnostico.controlador;

import static org.junit.jupiter.api.Assertions.*;

import es.uclm.esi.iso2.radiodiagnostico.modelo.cita.*;
import es.uclm.esi.iso2.radiodiagnostico.vista.VistaConsultarCitas;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ControladorConsultarCitasImplTest {

    private VistaFalsa vista;
    private ClienteFalso cliente;
    private ControladorConsultarCitas controlador;

    @BeforeEach
    void preparar() {
        vista = new VistaFalsa();
        cliente = new ClienteFalso();
        controlador = new ControladorConsultarCitasImpl(vista, cliente);
    }

    @Test
    void consultaCitasCorrectamente() {
        cliente.citas = List.of(new CitaPaciente(
                "CITA-1", "SOL-1", "12345678A",
                "Radiografía", LocalDateTime.now(),
                "SALA-1", "ASIGNADA"));

        controlador.consultarCitas();

        assertEquals(1, vista.citas.size());
        assertEquals(1, cliente.numeroConsultas);
    }

    @Test
    void sinCitasMuestraMensaje() {
        controlador.consultarCitas();

        assertTrue(vista.citas.isEmpty());
        assertEquals(
                "No tienes citas programadas.",
                vista.mensaje);
    }

    @Test
    void errorDeComunicacionSeMuestraEnLaVista() {
        cliente.fallar = true;

        controlador.consultarCitas();

        assertNotNull(vista.error);
        assertTrue(vista.error.contains("iniciado sesión"));
    }

    private static class VistaFalsa implements VistaConsultarCitas {
        String mensaje;
        String error;
        List<CitaPaciente> citas = List.of();

        @Override
        public void mostrarCitas(List<CitaPaciente> citas) {
            this.citas = citas;
        }

        @Override
        public void mostrarMensaje(String mensaje) {
            this.mensaje = mensaje;
        }

        @Override
        public void mostrarError(String mensaje) {
            this.error = mensaje;
        }
    }

    private static class ClienteFalso implements ClienteCitas {
        boolean fallar;
        int numeroConsultas;
        List<CitaPaciente> citas = List.of();

        @Override
        public List<CitaPaciente> consultarCitas() {
            numeroConsultas++;

            if (fallar) {
                throw new ErrorComunicacionException(
                        "Error de conexión", null);
            }

            return citas;
        }

        @Override
        public List<SolicitudPendiente> obtenerSolicitudesPendientes() {
            return List.of();
        }

        @Override
        public ResultadoAsignacion asignarCita(
                DatosAsignacionCita datos) {
            return new ResultadoAsignacion(true, "Correcto");
        }
    }
}


