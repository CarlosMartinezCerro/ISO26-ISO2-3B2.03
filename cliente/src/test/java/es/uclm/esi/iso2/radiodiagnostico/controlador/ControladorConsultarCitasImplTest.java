
package es.uclm.esi.iso2.radiodiagnostico.controlador;

import static org.junit.jupiter.api.Assertions.*;

import es.uclm.esi.iso2.radiodiagnostico.cita.*;
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
        vista.dni = "12345678A";
        cliente.citas = List.of(new CitaPaciente(
                "CITA-1", "SOL-1", "12345678A",
                "Radiografía", LocalDateTime.now(),
                "SALA-1", "ASIGNADA"));

        controlador.consultarCitas();

        assertEquals(1, vista.citas.size());
        assertEquals("12345678A", cliente.dniRecibido);
    }

    @Test
    void sinCitasMuestraMensaje() {
        vista.dni = "12345678A";

        controlador.consultarCitas();

        assertTrue(vista.citas.isEmpty());
        assertEquals(
                "El paciente no tiene citas programadas.",
                vista.mensaje);
    }

    @Test
    void sinDniMuestraError() {
        vista.dni = " ";

        controlador.consultarCitas();

        assertEquals(
                "Introduzca el DNI del paciente.",
                vista.error);
        assertNull(cliente.dniRecibido);
    }

    @Test
    void errorDeComunicacionSeMuestraEnLaVista() {
        vista.dni = "12345678A";
        cliente.fallar = true;

        controlador.consultarCitas();

        assertNotNull(vista.error);
        assertTrue(vista.error.contains("conectar con el servidor"));
    }

    private static class VistaFalsa implements VistaConsultarCitas {
        String dni;
        String mensaje;
        String error;
        List<CitaPaciente> citas = List.of();

        @Override
        public String obtenerDniPaciente() {
            return dni;
        }

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
        String dniRecibido;
        boolean fallar;
        List<CitaPaciente> citas = List.of();

        @Override
        public List<CitaPaciente> consultarCitas(String dni) {
            if (fallar) {
                throw new ErrorComunicacionException(
                        "Error de conexión", null);
            }
            dniRecibido = dni;
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

