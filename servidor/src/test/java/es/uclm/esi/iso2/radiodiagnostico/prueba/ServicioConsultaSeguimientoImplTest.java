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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Pruebas del servicio del CdU11 · Consultar prueba de seguimiento periódica.
 *
 * <p>Los seguimientos se crean con el servicio real del CdU13, igual que en la aplicación, y se
 * consultan con un reloj fijo para saber qué repetición es la próxima. Los casos salen de la tabla
 * de diseño de pruebas del CdU11 (clases de equivalencia, valores límite y conjetura de errores).
 * Todos los DNI son ficticios.
 */
@DisplayName("CdU11 · Servicio de consulta de seguimientos")
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

    // CP-S01 · DNI: clase no válida (nulo, vacío o en blanco)
    @ParameterizedTest(name = "DNI no válido: [{0}]")
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t"})
    @DisplayName("Un DNI nulo, vacío o en blanco se rechaza igual que en el CdU10")
    void dniNoValidoSeRechaza(String dni) {
        ServicioConsultaSeguimiento consulta = consultaEn(HOY);

        DniPacienteInvalidoException e = assertThrows(DniPacienteInvalidoException.class,
                () -> consulta.consultarSeguimientos(dni));

        assertEquals(ServicioConsultaSeguimientoImpl.DNI_OBLIGATORIO, e.getMessage());
    }

    // CP-S02 · DNI: clase válida, con distintas formas de escribirlo
    @ParameterizedTest(name = "DNI válido escrito como [{0}]")
    @ValueSource(strings = {"12345678Z", "12345678z", "  12345678Z  "})
    @DisplayName("El DNI se encuentra sin importar mayúsculas ni espacios")
    void dniValidoSeNormaliza(String dni) {
        solicitar(PACIENTE, "TAC", 1, UnidadPeriodicidad.MESES, HOY, 1);

        assertEquals(1, consultaEn(HOY).consultarSeguimientos(dni).size());
    }

    // CP-S03 · Paciente sin seguimientos (flujo alternativo A2)
    @Test
    @DisplayName("Un paciente sin seguimientos obtiene una lista vacía")
    void pacienteSinSeguimientosDevuelveListaVacia() {
        solicitar("87654321X", "TAC", 1, UnidadPeriodicidad.MESES, HOY, 2);

        assertTrue(consultaEn(HOY).consultarSeguimientos(PACIENTE).isEmpty());
    }

    // CP-S04 · Flujo principal: resumen completo del seguimiento
    @Test
    @DisplayName("Resume el seguimiento con periodicidad, fechas y próxima repetición")
    void resumeElSeguimiento() {
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

    // CP-S05 · Valores límite alrededor de la última repetición
    @ParameterizedTest(name = "consultado {0} día(s) respecto a la última repetición → {1}")
    @CsvSource({
        "-1, EN_CURSO",
        "0,  EN_CURSO",
        "1,  FINALIZADO"
    })
    @DisplayName("El estado pasa a FINALIZADO justo el día después de la última repetición")
    void estadoEnLosLimitesDeLaUltimaRepeticion(int dias, String estadoEsperado) {
        // Dos repeticiones diarias: HOY y HOY + 1 (la última).
        solicitar(PACIENTE, "Ecografía", 1, UnidadPeriodicidad.DIAS, HOY, 2);
        LocalDate ultima = HOY.plusDays(1);
        LocalDate diaConsulta = ultima.plusDays(dias);

        SeguimientoPeriodico s = consultaEn(diaConsulta).consultarSeguimientos(PACIENTE).get(0);

        assertEquals(estadoEsperado, s.estado());
        assertEquals(ultima, s.fechaFin());
        if (SeguimientoPeriodico.FINALIZADO.equals(estadoEsperado)) {
            assertNull(s.proximaFecha());
        } else {
            // Una repetición prevista para el mismo día de la consulta cuenta como próxima.
            assertEquals(diaConsulta, s.proximaFecha());
        }
    }

    // CP-S06 · Repeticiones citadas frente a pendientes
    @Test
    @DisplayName("Cuenta las repeticiones que ya tienen cita")
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

    // CP-S07 · Orden de la lista
    @Test
    @DisplayName("Ordena primero los seguimientos en curso por próxima fecha y después los finalizados")
    void ordenaEnCursoAntesQueFinalizados() {
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

    // CP-S08 · Texto de la periodicidad: singular (cada = 1, valor límite) y plural (cada > 1)
    @ParameterizedTest(name = "cada {0} {1} → {2}")
    @CsvSource({
        "1,  DIAS,    Cada día",
        "2,  DIAS,    Cada 2 días",
        "10, DIAS,    Cada 10 días",
        "1,  SEMANAS, Cada semana",
        "2,  SEMANAS, Cada 2 semanas",
        "1,  MESES,   Cada mes",
        "6,  MESES,   Cada 6 meses"
    })
    @DisplayName("Describe la periodicidad en singular y en plural")
    void describeLaPeriodicidad(int cada, UnidadPeriodicidad unidad, String esperado) {
        assertEquals(esperado,
                ServicioConsultaSeguimientoImpl.describir(new PeriodicidadSeguimiento(cada, unidad)));
    }

    // CP-S09 · Conjetura de errores: dependencia obligatoria
    @Test
    @DisplayName("El servicio del CdU13 es obligatorio")
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
