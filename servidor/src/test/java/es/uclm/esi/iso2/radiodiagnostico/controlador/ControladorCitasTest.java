package es.uclm.esi.iso2.radiodiagnostico.controlador;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.uclm.esi.iso2.radiodiagnostico.modelo.cita.CitaRepositorioEnMemoria;
import es.uclm.esi.iso2.radiodiagnostico.modelo.cita.DatosAsignacionCita;
import es.uclm.esi.iso2.radiodiagnostico.modelo.cita.ResultadoAsignacion;
import es.uclm.esi.iso2.radiodiagnostico.modelo.cita.ServicioCitasImpl;
import es.uclm.esi.iso2.radiodiagnostico.modelo.solicitud.EstadoSolicitud;
import es.uclm.esi.iso2.radiodiagnostico.modelo.solicitud.PrioridadSolicitud;
import es.uclm.esi.iso2.radiodiagnostico.modelo.solicitud.SolicitudPrueba;
import es.uclm.esi.iso2.radiodiagnostico.modelo.solicitud.SolicitudRepositorioEnMemoria;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ControladorCitasTest {

    private static final ZoneId ZONA = ZoneId.of("Europe/Madrid");
    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 11, 16, 10, 0);
    private static final LocalDateTime MANANA = AHORA.plusDays(1);

    private ControladorCitas controlador;

    @BeforeEach
    void crearControlador() {
        SolicitudRepositorioEnMemoria solicitudes = new SolicitudRepositorioEnMemoria();
        solicitudes.guardar(new SolicitudPrueba("SOL-001", "FICTICIO-001", "Radiografía",
                PrioridadSolicitud.URGENTE, EstadoSolicitud.PENDIENTE));
        Clock reloj = Clock.fixed(AHORA.atZone(ZONA).toInstant(), ZONA);
        controlador = new ControladorCitas(
                new ServicioCitasImpl(solicitudes, new CitaRepositorioEnMemoria(), reloj));
    }

    @Test
    void devuelveLasSolicitudesPendientes() {
        assertEquals(1, controlador.solicitudesPendientes().size());
    }

    @Test
    void asignacionCorrectaDevuelveResultadoCorrecto() {
        ResultadoAsignacion resultado = controlador.asignarCita(
                new DatosAsignacionCita("SOL-001", MANANA, "SALA-RX-1"));

        assertTrue(resultado.correcto());
        assertTrue(resultado.mensaje().contains("SOL-001"));
    }

    @Test
    void falloDeLaBaseDeDatosDevuelveUnMensajeSinDetallesInternos() {
        SolicitudRepositorioEnMemoria solicitudes = new SolicitudRepositorioEnMemoria();
        solicitudes.guardar(new SolicitudPrueba("SOL-001", "FICTICIO-001", "Radiografía",
                PrioridadSolicitud.URGENTE, EstadoSolicitud.PENDIENTE));
        CitaRepositorioEnMemoria citasCaidas = new CitaRepositorioEnMemoria() {
            @Override
            public void guardar(es.uclm.esi.iso2.radiodiagnostico.modelo.cita.Cita cita) {
                throw new IllegalStateException("fk_cita_paciente");
            }
        };
        Clock reloj = Clock.fixed(AHORA.atZone(ZONA).toInstant(), ZONA);
        ControladorCitas conMySqlFallando = new ControladorCitas(
                new ServicioCitasImpl(solicitudes, citasCaidas, reloj));

        ResultadoAsignacion resultado = conMySqlFallando.asignarCita(
                new DatosAsignacionCita("SOL-001", MANANA, "SALA-RX-1"));

        assertEquals(new ResultadoAsignacion(false, ControladorCitas.ERROR_GUARDAR_CITA), resultado);
    }

    @Test
    void datosInvalidosDevuelvenErrorConMensaje() {
        ResultadoAsignacion resultado = controlador.asignarCita(
                new DatosAsignacionCita("SOL-001", MANANA, ""));

        assertEquals(new ResultadoAsignacion(false, "El campo sala es obligatorio."), resultado);
    }

    @Test
    void solicitudInexistenteDevuelveError() {
        ResultadoAsignacion resultado = controlador.asignarCita(
                new DatosAsignacionCita("SOL-999", MANANA, "SALA-RX-1"));

        assertEquals(false, resultado.correcto());
    }

    @Test
    void solicitudYaCitadaDevuelveError() {
        controlador.asignarCita(new DatosAsignacionCita("SOL-001", MANANA, "SALA-RX-1"));

        ResultadoAsignacion resultado = controlador.asignarCita(
                new DatosAsignacionCita("SOL-001", MANANA.plusHours(1), "SALA-RX-2"));

        assertEquals(false, resultado.correcto());
    }
}
