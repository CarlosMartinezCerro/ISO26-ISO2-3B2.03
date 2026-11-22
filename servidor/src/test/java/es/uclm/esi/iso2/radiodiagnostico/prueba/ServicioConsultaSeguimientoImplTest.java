package es.uclm.esi.iso2.radiodiagnostico.prueba;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.uclm.esi.iso2.radiodiagnostico.informe.DniPacienteInvalidoException;
import es.uclm.esi.iso2.radiodiagnostico.solicitud.PrioridadSolicitud;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Pruebas del CdU11. Los seguimientos se crean con el servicio real del CdU13, igual que en la
 * aplicación, y se consultan con un reloj fijo para saber qué repetición es la próxima.
 * Todos los DNI son ficticios.
 */
class ServicioConsultaSeguimientoImplTest {

    private static final ZoneId ZONA = ZoneId.of("Europe/Madrid");
    private static final LocalDate HOY = LocalDate.of(2026, 11, 18);
    private static final String PACIENTE = "12345678Z";
    private static final String MEDICO = "00000001R";

    private SolicitudSeguimientoRepositorio repositorio;
    private ServicioSolicitudSeguimiento solicitudes;

    @BeforeEach
    void preparar() {
        repositorio = new SolicitudSeguimientoRepositorioEnMemoria();
        solicitudes = new ServicioSolicitudSeguimientoImpl(repositorio,
                new CatalogoTiposPruebaSimulado(), new RegistroMedicosSimulado(), reloj(HOY));
    }

    @Test
    void dniNuloOVacioSeRechazaComoEnElCdU10() {
        ServicioConsultaSeguimiento consulta = consultaEn(HOY);

        assertThrows(DniPacienteInvalidoException.class, () -> consulta.consultarSeguimientos(null));
        DniPacienteInvalidoException e = assertThrows(DniPacienteInvalidoException.class,
                () -> consulta.consultarSeguimientos("   "));
        assertEquals(ServicioConsultaSeguimientoImpl.DNI_OBLIGATORIO, e.getMessage());
    }

    @Test
    void pacienteSinSeguimientosDevuelveListaVacia() {
        solicitar("87654321X", "TAC", 1, UnidadPeriodicidad.MESES, HOY, 2);

        assertTrue(consultaEn(HOY).consultarSeguimientos(PACIENTE).isEmpty());
    }

    @Test
    void resumeElSeguimientoConPeriodicidadFechasYProximaRepeticion() {
        SolicitudSeguimiento creada = solicitar(PACIENTE, "Mamografía", 3, UnidadPeriodicidad.MESES,
                LocalDate.of(2026, 11, 20), 3);

        List<SeguimientoPeriodico> seguimientos = consultaEn(LocalDate.of(2026, 12, 1))
                .consultarSeguimientos(PACIENTE);

        assertEquals(1, seguimientos.size());
        SeguimientoPeriodico s = seguimientos.get(0);
        assertEquals(creada.id(), s.idSolicitud());
        assertEquals(PACIENTE, s.dniPaciente());
        assertEquals(MEDICO, s.dniMedico());
        assertEquals("Mamografía", s.tipoPrueba());
        assertEquals(PrioridadSolicitud.ORDINARIO, s.prioridad());
        assertEquals("Cada 3 meses", s.periodicidad());
        assertEquals(LocalDate.of(2026, 11, 20), s.fechaInicio());
        assertEquals(LocalDate.of(2027, 5, 20), s.fechaFin());
        assertEquals(3, s.repeticionesTotales());
        assertEquals(0, s.repeticionesCitadas());
        // La del 20/11 ya ha pasado el 01/12: la próxima es la del 20/02.
        assertEquals(LocalDate.of(2027, 2, 20), s.proximaFecha());
        assertEquals(SeguimientoPeriodico.EN_CURSO, s.estado());
        assertEquals(creada.repeticiones(), s.repeticiones());
    }

    @Test
    void laRepeticionDeHoyCuentaComoProxima() {
        solicitar(PACIENTE, "TAC", 2, UnidadPeriodicidad.SEMANAS, HOY, 2);

        SeguimientoPeriodico s = consultaEn(HOY).consultarSeguimientos(PACIENTE).get(0);

        assertEquals(HOY, s.proximaFecha());
    }

    @Test
    void seguimientoConTodasLasRepeticionesPasadasEstaFinalizado() {
        solicitar(PACIENTE, "Ecografía", 1, UnidadPeriodicidad.DIAS, HOY, 2);

        SeguimientoPeriodico s = consultaEn(HOY.plusYears(1)).consultarSeguimientos(PACIENTE).get(0);

        assertNull(s.proximaFecha());
        assertEquals(SeguimientoPeriodico.FINALIZADO, s.estado());
        assertEquals(HOY.plusDays(1), s.fechaFin());
    }

    @Test
    void cuentaLasRepeticionesYaCitadas() {
        SolicitudSeguimiento creada = solicitar(PACIENTE, "TAC", 1, UnidadPeriodicidad.MESES, HOY, 3);
        List<RepeticionSeguimiento> repeticiones = List.of(
                citada(creada.repeticiones().get(0)),
                citada(creada.repeticiones().get(1)),
                creada.repeticiones().get(2));
        repositorio.guardar(new SolicitudSeguimiento(creada.id(), creada.dniPaciente(),
                creada.dniMedico(), creada.tipoPrueba(), creada.indicacionClinica(),
                creada.justificacion(), creada.prioridad(), creada.periodicidad(),
                creada.fechaInicio(), creada.numeroRepeticiones(), creada.fechaLimite(),
                repeticiones));

        SeguimientoPeriodico s = consultaEn(HOY).consultarSeguimientos(PACIENTE).get(0);

        assertEquals(3, s.repeticionesTotales());
        assertEquals(2, s.repeticionesCitadas());
    }

    @Test
    void buscaElDniSinEspaciosNiMayusculas() {
        solicitar(PACIENTE, "TAC", 1, UnidadPeriodicidad.MESES, HOY, 1);

        assertEquals(1, consultaEn(HOY).consultarSeguimientos("  12345678z ").size());
    }

    @Test
    void ordenaPrimeroLosEnCursoPorProximaFechaYDespuesLosFinalizados() {
        LocalDate consulta = HOY.plusDays(10);
        SolicitudSeguimiento finalizado =
                solicitar(PACIENTE, "Ecografía", 1, UnidadPeriodicidad.DIAS, HOY, 2);
        SolicitudSeguimiento lejano =
                solicitar(PACIENTE, "TAC", 1, UnidadPeriodicidad.MESES, HOY.plusMonths(2), 2);
        SolicitudSeguimiento cercano =
                solicitar(PACIENTE, "Mamografía", 1, UnidadPeriodicidad.SEMANAS, HOY, 4);

        List<String> orden = consultaEn(consulta).consultarSeguimientos(PACIENTE).stream()
                .map(SeguimientoPeriodico::idSolicitud)
                .toList();

        assertEquals(List.of(cercano.id(), lejano.id(), finalizado.id()), orden);
    }

    @Test
    void describeLaPeriodicidadEnSingularYPlural() {
        assertEquals("Cada día",
                ServicioConsultaSeguimientoImpl.describir(new PeriodicidadSeguimiento(1, UnidadPeriodicidad.DIAS)));
        assertEquals("Cada 10 días",
                ServicioConsultaSeguimientoImpl.describir(new PeriodicidadSeguimiento(10, UnidadPeriodicidad.DIAS)));
        assertEquals("Cada semana",
                ServicioConsultaSeguimientoImpl.describir(new PeriodicidadSeguimiento(1, UnidadPeriodicidad.SEMANAS)));
        assertEquals("Cada 2 semanas",
                ServicioConsultaSeguimientoImpl.describir(new PeriodicidadSeguimiento(2, UnidadPeriodicidad.SEMANAS)));
        assertEquals("Cada mes",
                ServicioConsultaSeguimientoImpl.describir(new PeriodicidadSeguimiento(1, UnidadPeriodicidad.MESES)));
        assertEquals("Cada 6 meses",
                ServicioConsultaSeguimientoImpl.describir(new PeriodicidadSeguimiento(6, UnidadPeriodicidad.MESES)));
    }

    @Test
    void elServicioDelCdU13EsObligatorio() {
        assertThrows(NullPointerException.class, () -> new ServicioConsultaSeguimientoImpl(null));
    }

    private SolicitudSeguimiento solicitar(String dniPaciente, String tipoPrueba, int cada,
                                           UnidadPeriodicidad unidad, LocalDate inicio,
                                           int repeticiones) {
        return solicitudes.solicitar(new DatosSolicitudSeguimiento(dniPaciente, MEDICO, tipoPrueba,
                "Indicación ficticia", "Justificación ficticia", PrioridadSolicitud.ORDINARIO,
                cada, unidad, inicio, repeticiones, null));
    }

    private ServicioConsultaSeguimiento consultaEn(LocalDate dia) {
        return new ServicioConsultaSeguimientoImpl(solicitudes, reloj(dia));
    }

    private static RepeticionSeguimiento citada(RepeticionSeguimiento r) {
        return new RepeticionSeguimiento(r.id(), r.idSolicitudSeguimiento(), r.numero(),
                r.fechaPrevista(), EstadoRepeticionSeguimiento.CITADA);
    }

    private static Clock reloj(LocalDate dia) {
        return Clock.fixed(dia.atStartOfDay(ZONA).toInstant(), ZONA);
    }
}
