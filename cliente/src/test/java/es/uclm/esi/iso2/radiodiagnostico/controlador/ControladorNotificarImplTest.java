package es.uclm.esi.iso2.radiodiagnostico.controlador;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.uclm.esi.iso2.radiodiagnostico.notificacion.ClienteNotificaciones;
import es.uclm.esi.iso2.radiodiagnostico.notificacion.DatosNotificacion;
import es.uclm.esi.iso2.radiodiagnostico.notificacion.ErrorComunicacionException;
import es.uclm.esi.iso2.radiodiagnostico.notificacion.NotificacionEnviada;
import es.uclm.esi.iso2.radiodiagnostico.notificacion.RespuestaListadoNotificaciones;
import es.uclm.esi.iso2.radiodiagnostico.notificacion.ResultadoNotificacion;
import es.uclm.esi.iso2.radiodiagnostico.vista.VistaNotificar;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ControladorNotificarImplTest {

    private static final DatosNotificacion DATOS =
            new DatosNotificacion("12345678Z", "SMS", "Recordatorio", "Traiga la tarjeta.");
    private static final List<NotificacionEnviada> HISTORIAL = List.of(new NotificacionEnviada("NOT-001",
            "12345678Z", "GENERAL", "SMS", "612345678", "Recordatorio", "Traiga la tarjeta.",
            LocalDateTime.of(2026, 11, 21, 10, 0)));

    private VistaFalsa vista;
    private ClienteFalso cliente;
    private ControladorNotificar controlador;

    @BeforeEach
    void crearControlador() {
        vista = new VistaFalsa();
        cliente = new ClienteFalso();
        controlador = new ControladorNotificarImpl(vista, cliente);
    }

    @Test
    void notificacionCorrectaMuestraExitoLimpiaYRecargaElHistorial() {
        vista.datos = DATOS;
        vista.dni = "12345678Z";
        cliente.resultado = new ResultadoNotificacion(true, "Notificación NOT-001 enviada.", "NOT-001");

        controlador.notificar();

        assertEquals(DATOS, cliente.datosRecibidos);
        assertEquals("Notificación NOT-001 enviada.", vista.exito);
        assertTrue(vista.limpiado);
        assertEquals(HISTORIAL, vista.historial);
        assertNull(vista.error);
    }

    @Test
    void notificacionRechazadaMuestraElMotivoSinLimpiar() {
        vista.datos = DATOS;
        cliente.resultado = new ResultadoNotificacion(false, "No existe ningún paciente.", null);

        controlador.notificar();

        assertEquals("No existe ningún paciente.", vista.error);
        assertFalse(vista.limpiado);
        assertNull(vista.exito);
    }

    @Test
    void sinCanalNoLlamaAlServidor() {
        controlador.notificar();

        assertEquals(ControladorNotificarImpl.MENSAJE_SIN_CANAL, vista.error);
        assertNull(cliente.datosRecibidos);
    }

    @Test
    void sinConexionAlNotificarMuestraError() {
        vista.datos = DATOS;
        cliente.sinConexion = true;

        controlador.notificar();

        assertEquals(ControladorNotificarImpl.MENSAJE_SIN_CONEXION, vista.error);
    }

    @Test
    void verHistorialMuestraLasNotificacionesYElMensaje() {
        vista.dni = " 12345678Z ";

        controlador.verHistorial();

        assertEquals("12345678Z", cliente.dniRecibido);
        assertEquals(HISTORIAL, vista.historial);
        assertEquals("1 notificación(es) encontradas.", vista.mensajeHistorial);
    }

    @Test
    void verHistorialSinDniPideElDni() {
        vista.dni = "  ";

        controlador.verHistorial();

        assertEquals(ControladorNotificarImpl.MENSAJE_SIN_DNI, vista.error);
        assertNull(cliente.dniRecibido);
    }

    @Test
    void verHistorialConDniNuloPideElDni() {
        vista.dni = null;

        controlador.verHistorial();

        assertEquals(ControladorNotificarImpl.MENSAJE_SIN_DNI, vista.error);
    }

    @Test
    void historialRechazadoVaciaLaTablaYMuestraElError() {
        vista.dni = "12345678Z";
        vista.historial = HISTORIAL;
        cliente.respuestaHistorial = new RespuestaListadoNotificaciones(false, "No disponible.", List.of());

        controlador.verHistorial();

        assertTrue(vista.historial.isEmpty());
        assertEquals("No disponible.", vista.error);
    }

    @Test
    void sinConexionAlVerHistorialMuestraError() {
        vista.dni = "12345678Z";
        cliente.sinConexion = true;

        controlador.verHistorial();

        assertEquals(ControladorNotificarImpl.MENSAJE_SIN_CONEXION, vista.error);
    }

    @Test
    void dependenciasObligatorias() {
        assertThrows(NullPointerException.class, () -> new ControladorNotificarImpl(null, cliente));
        assertThrows(NullPointerException.class, () -> new ControladorNotificarImpl(vista, null));
    }

    private static final class VistaFalsa implements VistaNotificar {
        DatosNotificacion datos;
        String dni;
        List<NotificacionEnviada> historial;
        String mensajeHistorial;
        String exito;
        String error;
        boolean limpiado;

        @Override
        public DatosNotificacion obtenerDatos() {
            return datos;
        }

        @Override
        public String obtenerDniPaciente() {
            return dni;
        }

        @Override
        public void mostrarHistorial(List<NotificacionEnviada> notificaciones, String mensaje) {
            historial = notificaciones;
            mensajeHistorial = mensaje;
        }

        @Override
        public void mostrarExito(String mensaje) {
            exito = mensaje;
        }

        @Override
        public void mostrarError(String mensaje) {
            error = mensaje;
        }

        @Override
        public void limpiarMensaje() {
            limpiado = true;
        }
    }

    private static final class ClienteFalso implements ClienteNotificaciones {
        ResultadoNotificacion resultado;
        RespuestaListadoNotificaciones respuestaHistorial =
                new RespuestaListadoNotificaciones(true, "1 notificación(es) encontradas.", HISTORIAL);
        boolean sinConexion;
        DatosNotificacion datosRecibidos;
        String dniRecibido;

        @Override
        public ResultadoNotificacion notificar(DatosNotificacion datos) {
            datosRecibidos = datos;
            comprobarConexion();
            return resultado;
        }

        @Override
        public RespuestaListadoNotificaciones historialPaciente(String dniPaciente) {
            dniRecibido = dniPaciente;
            comprobarConexion();
            return respuestaHistorial;
        }

        private void comprobarConexion() {
            if (sinConexion) {
                throw new ErrorComunicacionException("Servidor caído", new IOException());
            }
        }
    }
}
