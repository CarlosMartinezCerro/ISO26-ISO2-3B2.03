package es.uclm.esi.iso2.radiodiagnostico.controlador;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.uclm.esi.iso2.radiodiagnostico.prueba.ClienteConsultaSeguimientos;
import es.uclm.esi.iso2.radiodiagnostico.prueba.ErrorComunicacionException;
import es.uclm.esi.iso2.radiodiagnostico.prueba.PrioridadSeguimiento;
import es.uclm.esi.iso2.radiodiagnostico.prueba.ResultadoConsultaSeguimientos;
import es.uclm.esi.iso2.radiodiagnostico.prueba.SeguimientoPeriodico;
import es.uclm.esi.iso2.radiodiagnostico.vista.VistaConsultarSeguimientos;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

/** Prueba el controlador del cliente del CdU11 con una vista y un cliente falsos. */
@DisplayName("CdU11 · Controlador del cliente")
class ControladorConsultarSeguimientosImplTest {

    private static final SeguimientoPeriodico SEGUIMIENTO = new SeguimientoPeriodico("SEG-1",
            "12345678Z", "00000001R", "TAC", "Indicación ficticia", PrioridadSeguimiento.URGENTE,
            "Cada 2 semanas",
            LocalDate.of(2026, 11, 20), LocalDate.of(2026, 12, 4), 2, 0,
            LocalDate.of(2026, 11, 20), "EN_CURSO", List.of());

    private final VistaFalsa vista = new VistaFalsa();

    // CP-K01 · Flujo principal
    @Test
    @DisplayName("Muestra en la vista los seguimientos del paciente")
    void muestraLosSeguimientosDelPaciente() {
        vista.dni = " 12345678Z ";
        ClienteFalso cliente = new ClienteFalso(new ResultadoConsultaSeguimientos(true,
                "Se ha encontrado 1 prueba de seguimiento periódica.", List.of(SEGUIMIENTO)));

        new ControladorConsultarSeguimientosImpl(vista, cliente).consultarSeguimientos();

        assertEquals("12345678Z", cliente.dniRecibido);
        assertEquals(List.of(SEGUIMIENTO), vista.seguimientos);
        assertTrue(vista.mensajes.isEmpty());
        assertTrue(vista.errores.isEmpty());
    }

    // CP-K02 · Paciente sin seguimientos (A2)
    @Test
    @DisplayName("Sin seguimientos muestra el mensaje informativo del servidor")
    void sinSeguimientosMuestraElMensajeDelServidor() {
        vista.dni = "12345678Z";
        String mensaje = "El paciente 12345678Z no tiene pruebas de seguimiento periódicas.";

        new ControladorConsultarSeguimientosImpl(vista,
                new ClienteFalso(new ResultadoConsultaSeguimientos(true, mensaje, List.of())))
                .consultarSeguimientos();

        assertEquals(List.of(), vista.seguimientos);
        assertEquals(List.of(mensaje), vista.mensajes);
        assertTrue(vista.errores.isEmpty());
    }

    // CP-K03 · DNI vacío en la vista (A1): clase no válida
    @ParameterizedTest(name = "DNI en la vista: [{0}]")
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    @DisplayName("Con el DNI vacío avisa sin llamar al servidor")
    void dniVacioNoLlamaAlServidor(String dni) {
        vista.dni = dni;
        ClienteFalso cliente = new ClienteFalso(null);

        new ControladorConsultarSeguimientosImpl(vista, cliente).consultarSeguimientos();

        assertNull(cliente.dniRecibido);
        assertEquals(List.of(ControladorConsultarSeguimientosImpl.MENSAJE_DNI_VACIO), vista.errores);
    }

    // CP-K04 · Rechazo del servidor (A1)
    @Test
    @DisplayName("Si el servidor rechaza la consulta vacía la tabla y muestra el error")
    void rechazoDelServidorVaciaLaTablaYMuestraElError() {
        vista.dni = "12345678Z";
        vista.seguimientos = List.of(SEGUIMIENTO);

        new ControladorConsultarSeguimientosImpl(vista, new ClienteFalso(
                new ResultadoConsultaSeguimientos(false, "El DNI del paciente es obligatorio.", List.of())))
                .consultarSeguimientos();

        assertEquals(List.of(), vista.seguimientos);
        assertEquals(List.of("El DNI del paciente es obligatorio."), vista.errores);
    }

    // CP-K05 · Servidor no disponible (A3)
    @Test
    @DisplayName("Sin conexión muestra el mismo aviso que el CdU10")
    void sinConexionMuestraElMismoAvisoQueElCdU10() {
        vista.dni = "12345678Z";
        ClienteConsultaSeguimientos sinConexion = dni -> {
            throw new ErrorComunicacionException("Servidor parado", null);
        };

        new ControladorConsultarSeguimientosImpl(vista, sinConexion).consultarSeguimientos();

        assertEquals(List.of(ControladorHistorialPruebasImpl.MENSAJE_SIN_CONEXION), vista.errores);
    }

    // CP-K06 · Conjetura de errores: dependencias obligatorias
    @Test
    @DisplayName("La vista y el cliente son obligatorios")
    void vistaYClienteSonObligatorios() {
        ClienteFalso cliente = new ClienteFalso(null);
        assertThrows(NullPointerException.class,
                () -> new ControladorConsultarSeguimientosImpl(null, cliente));
        assertThrows(NullPointerException.class,
                () -> new ControladorConsultarSeguimientosImpl(vista, null));
    }

    private static final class ClienteFalso implements ClienteConsultaSeguimientos {

        private final ResultadoConsultaSeguimientos respuesta;
        private String dniRecibido;

        ClienteFalso(ResultadoConsultaSeguimientos respuesta) {
            this.respuesta = respuesta;
        }

        @Override
        public ResultadoConsultaSeguimientos consultarSeguimientos(String dniPaciente) {
            dniRecibido = dniPaciente;
            return respuesta;
        }
    }

    private static final class VistaFalsa implements VistaConsultarSeguimientos {

        private String dni;
        private List<SeguimientoPeriodico> seguimientos;
        private final List<String> mensajes = new ArrayList<>();
        private final List<String> errores = new ArrayList<>();

        @Override
        public String obtenerDniPaciente() {
            return dni;
        }

        @Override
        public void mostrarSeguimientos(List<SeguimientoPeriodico> seguimientos) {
            this.seguimientos = seguimientos;
        }

        @Override
        public void mostrarMensaje(String mensaje) {
            mensajes.add(mensaje);
        }

        @Override
        public void mostrarError(String mensaje) {
            errores.add(mensaje);
        }

        @Override
        public void limpiar() {
            dni = "";
            seguimientos = List.of();
        }
    }
}
