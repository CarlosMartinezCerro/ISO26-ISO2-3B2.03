package es.uclm.esi.iso2.radiodiagnostico.controlador;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.uclm.esi.iso2.radiodiagnostico.modelo.prueba.CatalogoTiposPruebaSimulado;
import es.uclm.esi.iso2.radiodiagnostico.modelo.prueba.DatosSolicitudSeguimiento;
import es.uclm.esi.iso2.radiodiagnostico.modelo.prueba.RegistroMedicosSimulado;
import es.uclm.esi.iso2.radiodiagnostico.modelo.prueba.ResultadoSolicitudSeguimiento;
import es.uclm.esi.iso2.radiodiagnostico.modelo.prueba.ServicioSolicitudSeguimientoImpl;
import es.uclm.esi.iso2.radiodiagnostico.modelo.prueba.SolicitudSeguimiento;
import es.uclm.esi.iso2.radiodiagnostico.modelo.prueba.SolicitudSeguimientoRepositorio;
import es.uclm.esi.iso2.radiodiagnostico.modelo.prueba.SolicitudSeguimientoRepositorioEnMemoria;
import es.uclm.esi.iso2.radiodiagnostico.modelo.prueba.UnidadPeriodicidad;
import es.uclm.esi.iso2.radiodiagnostico.modelo.solicitud.PrioridadSolicitud;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ControladorSolicitudSeguimientoTest {

    private static final ZoneId ZONA = ZoneId.of("Europe/Madrid");
    private static final LocalDate HOY = LocalDate.of(2026, 11, 18);

    private ControladorSolicitudSeguimiento controlador;

    @BeforeEach
    void crearControlador() {
        controlador = new ControladorSolicitudSeguimiento(servicioCon(
                new SolicitudSeguimientoRepositorioEnMemoria()));
    }

    @Test
    void solicitudConRepeticionesDevuelveResultadoCorrecto() {
        ResultadoSolicitudSeguimiento resultado = controlador.solicitar(
                datos("00000001R", "TAC", 3, null));

        assertTrue(resultado.correcto());
        assertEquals("Solicitud de seguimiento de TAC registrada con 3 solicitudes pendientes de cita.",
                resultado.mensaje());
        assertTrue(resultado.idSolicitud().startsWith("SEG-"));
        assertEquals(3, resultado.repeticiones().size());
    }

    @Test
    void solicitudConFechaLimiteDevuelveResultadoCorrecto() {
        ResultadoSolicitudSeguimiento resultado = controlador.solicitar(
                datos("00000001R", "Mamografía", null, HOY.plusMonths(2)));

        assertTrue(resultado.correcto());
        assertEquals(3, resultado.repeticiones().size());
    }

    @Test
    void usuarioNoMedicoDevuelveErrorConMensaje() {
        assertEquals(ResultadoSolicitudSeguimiento.error(
                        "Solo un médico puede solicitar una prueba de seguimiento periódica."),
                controlador.solicitar(datos("99999999R", "TAC", 3, null)));
    }

    @Test
    void tipoSinSeguimientoDevuelveErrorConMensaje() {
        assertEquals(ResultadoSolicitudSeguimiento.error(
                        "El tipo de prueba Radiografía no admite seguimiento periódico."),
                controlador.solicitar(datos("00000001R", "Radiografía", 3, null)));
    }

    @Test
    void ambosCriteriosDeFinDevuelvenError() {
        ResultadoSolicitudSeguimiento resultado = controlador.solicitar(
                datos("00000001R", "TAC", 3, HOY.plusMonths(6)));

        assertFalse(resultado.correcto());
        assertNull(resultado.idSolicitud());
        assertTrue(resultado.repeticiones().isEmpty());
    }

    @Test
    void ningunCriterioDeFinDevuelveError() {
        assertFalse(controlador.solicitar(datos("00000001R", "TAC", null, null)).correcto());
    }

    @Test
    void campoObligatorioVacioDevuelveError() {
        ResultadoSolicitudSeguimiento resultado = controlador.solicitar(new DatosSolicitudSeguimiento(
                "12345678Z", "00000001R", "TAC", "", "Justificación ficticia",
                PrioridadSolicitud.URGENTE, 1, UnidadPeriodicidad.MESES, HOY, 2, null));

        assertEquals(ResultadoSolicitudSeguimiento.error("El campo indicacionClinica es obligatorio."),
                resultado);
    }

    @Test
    void fechaDeInicioPasadaDevuelveError() {
        ResultadoSolicitudSeguimiento resultado = controlador.solicitar(new DatosSolicitudSeguimiento(
                "12345678Z", "00000001R", "TAC", "Indicación ficticia", "Justificación ficticia",
                PrioridadSolicitud.URGENTE, 1, UnidadPeriodicidad.MESES, HOY.minusDays(1), 2, null));

        assertEquals(ResultadoSolicitudSeguimiento.error(
                "El campo fechaInicio no puede ser anterior a hoy."), resultado);
    }

    @Test
    void datosNulosDevuelvenError() {
        assertFalse(controlador.solicitar(null).correcto());
    }

    @Test
    void falloDePersistenciaDevuelveMensajeGenerico() {
        ControladorSolicitudSeguimiento conFallo = new ControladorSolicitudSeguimiento(
                servicioCon(new RepositorioQueFalla()));

        assertEquals(ResultadoSolicitudSeguimiento.error(ControladorSolicitudSeguimiento.ERROR_INTERNO),
                conFallo.solicitar(datos("00000001R", "TAC", 3, null)));
    }

    private static ServicioSolicitudSeguimientoImpl servicioCon(SolicitudSeguimientoRepositorio repo) {
        Clock reloj = Clock.fixed(HOY.atStartOfDay(ZONA).toInstant(), ZONA);
        return new ServicioSolicitudSeguimientoImpl(repo, new CatalogoTiposPruebaSimulado(),
                new RegistroMedicosSimulado(), reloj);
    }

    private static DatosSolicitudSeguimiento datos(String dniMedico, String tipo,
                                                   Integer repeticiones, LocalDate limite) {
        return new DatosSolicitudSeguimiento("12345678Z", dniMedico, tipo,
                "Indicación ficticia", "Justificación ficticia", PrioridadSolicitud.PREFERENTE,
                1, UnidadPeriodicidad.MESES, HOY, repeticiones, limite);
    }

    /** Repositorio que simula una caída de la base de datos. */
    private static final class RepositorioQueFalla implements SolicitudSeguimientoRepositorio {

        @Override
        public Optional<SolicitudSeguimiento> buscarPorId(String id) {
            return Optional.empty();
        }

        @Override
        public List<SolicitudSeguimiento> buscarPorPaciente(String dniPaciente) {
            return List.of();
        }

        @Override
        public void guardar(SolicitudSeguimiento solicitud) {
            throw new IllegalStateException("Base de datos no disponible");
        }
    }
}
