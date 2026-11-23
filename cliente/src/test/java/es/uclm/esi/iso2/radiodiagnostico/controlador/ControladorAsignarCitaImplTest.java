package es.uclm.esi.iso2.radiodiagnostico.controlador;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import es.uclm.esi.iso2.radiodiagnostico.modelo.cita.CitaPaciente;
import es.uclm.esi.iso2.radiodiagnostico.modelo.cita.ClienteCitas;
import es.uclm.esi.iso2.radiodiagnostico.modelo.cita.DatosAsignacionCita;
import es.uclm.esi.iso2.radiodiagnostico.modelo.cita.ErrorComunicacionException;
import es.uclm.esi.iso2.radiodiagnostico.modelo.cita.ResultadoAsignacion;
import es.uclm.esi.iso2.radiodiagnostico.modelo.cita.SolicitudPendiente;
import es.uclm.esi.iso2.radiodiagnostico.vista.VistaAsignarCita;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ControladorAsignarCitaImplTest {

    private static final DatosAsignacionCita DATOS = new DatosAsignacionCita("SOL-001",
            LocalDateTime.of(2026, 11, 17, 9, 30), "SALA-RX-1");
    private static final List<SolicitudPendiente> PENDIENTES = List.of(
            new SolicitudPendiente("SOL-001", "FICTICIO-001", "Radiografía", "URGENTE"));

    private VistaFalsa vista;
    private ClienteFalso cliente;
    private ControladorAsignarCita controlador;

    @BeforeEach
    void crearControlador() {
        vista = new VistaFalsa();
        cliente = new ClienteFalso();
        controlador = new ControladorAsignarCitaImpl(vista, cliente);
    }

    @Test
    void cargaLasSolicitudesPendientesEnLaVista() {
        controlador.cargarSolicitudes();

        assertEquals(PENDIENTES, vista.solicitudesMostradas);
    }

    @Test
    void sinConexionAlCargarMuestraError() {
        cliente.sinConexion = true;

        controlador.cargarSolicitudes();

        assertEquals(ControladorAsignarCitaImpl.MENSAJE_SIN_CONEXION, vista.error);
    }

    @Test
    void asignacionCorrectaMuestraExitoYRecarga() {
        vista.datos = DATOS;
        cliente.resultado = new ResultadoAsignacion(true, "Cita asignada.");

        controlador.asignarCita();

        assertEquals(DATOS, cliente.datosRecibidos);
        assertEquals("Cita asignada.", vista.exito);
        assertNull(vista.error);
        assertEquals(PENDIENTES, vista.solicitudesMostradas);
    }

    @Test
    void asignacionRechazadaMuestraElMensajeDelServidor() {
        vista.datos = DATOS;
        cliente.resultado = new ResultadoAsignacion(false, "La sala ya está ocupada.");

        controlador.asignarCita();

        assertEquals("La sala ya está ocupada.", vista.error);
        assertNull(vista.exito);
    }

    @Test
    void sinDatosNoLlamaAlServidor() {
        vista.datos = null;

        controlador.asignarCita();

        assertNull(cliente.datosRecibidos);
        assertEquals("Seleccione una solicitud e indique fecha, hora y sala.", vista.error);
    }

    @Test
    void sinConexionAlAsignarMuestraError() {
        vista.datos = DATOS;
        cliente.sinConexion = true;

        controlador.asignarCita();

        assertEquals(ControladorAsignarCitaImpl.MENSAJE_SIN_CONEXION, vista.error);
        assertNull(vista.exito);
    }

    private static final class VistaFalsa implements VistaAsignarCita {
        DatosAsignacionCita datos;
        List<SolicitudPendiente> solicitudesMostradas = new ArrayList<>();
        String exito;
        String error;

        @Override
        public void mostrarSolicitudes(List<SolicitudPendiente> solicitudes) {
            solicitudesMostradas = solicitudes;
        }

        @Override
        public DatosAsignacionCita obtenerDatos() {
            return datos;
        }

        @Override
        public void mostrarExito(String mensaje) {
            exito = mensaje;
        }

        @Override
        public void mostrarError(String mensaje) {
            error = mensaje;
        }
    }

    private static final class ClienteFalso implements ClienteCitas {
        boolean sinConexion;
        ResultadoAsignacion resultado;
        DatosAsignacionCita datosRecibidos;

        @Override
        public List<CitaPaciente> consultarCitas() {
            return List.of();
        }
        
        @Override
        public List<SolicitudPendiente> obtenerSolicitudesPendientes() {
            comprobarConexion();
            return PENDIENTES;
        }

        @Override
        public ResultadoAsignacion asignarCita(DatosAsignacionCita datos) {
            comprobarConexion();
            datosRecibidos = datos;
            return resultado;
        }

        private void comprobarConexion() {
            if (sinConexion) {
                throw new ErrorComunicacionException("Servidor no disponible",
                        new java.io.IOException("Conexión rechazada"));
            }
        }
    }
}
