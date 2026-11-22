package es.uclm.esi.iso2.radiodiagnostico.controlador;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.uclm.esi.iso2.radiodiagnostico.informe.ErrorComunicacionException;
import es.uclm.esi.iso2.radiodiagnostico.prueba.ClienteSeguimientos;
import es.uclm.esi.iso2.radiodiagnostico.prueba.ResultadoConsultaSeguimientos;
import es.uclm.esi.iso2.radiodiagnostico.prueba.SeguimientoPeriodico;
import es.uclm.esi.iso2.radiodiagnostico.vista.VistaConsultarSeguimientos;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Prueba el controlador del cliente del CdU11 con una vista y un cliente falsos. */
class ControladorConsultarSeguimientosImplTest {

    private static final SeguimientoPeriodico SEGUIMIENTO = new SeguimientoPeriodico("SEG-1",
            "12345678Z", "00000001R", "TAC", "Indicación ficticia", "URGENTE", "Cada 2 semanas",
            LocalDate.of(2026, 11, 20), LocalDate.of(2026, 12, 4), 2, 0,
            LocalDate.of(2026, 11, 20), "EN_CURSO", List.of());

    private final VistaFalsa vista = new VistaFalsa();

    @Test
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

    @Test
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

    @Test
    void dniVacioNoLlamaAlServidor() {
        vista.dni = "   ";
        ClienteFalso cliente = new ClienteFalso(null);

        new ControladorConsultarSeguimientosImpl(vista, cliente).consultarSeguimientos();

        assertNull(cliente.dniRecibido);
        assertEquals(List.of(ControladorConsultarSeguimientosImpl.MENSAJE_DNI_VACIO), vista.errores);
    }

    @Test
    void rechazoDelServidorVaciaLaTablaYMuestraElError() {
        vista.dni = "12345678Z";
        vista.seguimientos = List.of(SEGUIMIENTO);

        new ControladorConsultarSeguimientosImpl(vista, new ClienteFalso(
                new ResultadoConsultaSeguimientos(false, "El DNI del paciente es obligatorio.", List.of())))
                .consultarSeguimientos();

        assertEquals(List.of(), vista.seguimientos);
        assertEquals(List.of("El DNI del paciente es obligatorio."), vista.errores);
    }

    @Test
    void sinConexionMuestraElMismoAvisoQueElCdU10() {
        vista.dni = "12345678Z";
        ClienteSeguimientos sinConexion = dni -> {
            throw new ErrorComunicacionException("Servidor parado", null);
        };

        new ControladorConsultarSeguimientosImpl(vista, sinConexion).consultarSeguimientos();

        assertEquals(List.of(ControladorHistorialPruebasImpl.MENSAJE_SIN_CONEXION), vista.errores);
    }

    @Test
    void vistaYClienteSonObligatorios() {
        ClienteFalso cliente = new ClienteFalso(null);
        assertThrows(NullPointerException.class,
                () -> new ControladorConsultarSeguimientosImpl(null, cliente));
        assertThrows(NullPointerException.class,
                () -> new ControladorConsultarSeguimientosImpl(vista, null));
    }

    private static final class ClienteFalso implements ClienteSeguimientos {

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
