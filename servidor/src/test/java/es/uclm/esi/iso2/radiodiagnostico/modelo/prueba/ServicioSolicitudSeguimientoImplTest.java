package es.uclm.esi.iso2.radiodiagnostico.modelo.prueba;

import static es.uclm.esi.iso2.radiodiagnostico.modelo.prueba.DatosSeguimientoPrueba.DNI_MEDICO;
import static es.uclm.esi.iso2.radiodiagnostico.modelo.prueba.DatosSeguimientoPrueba.DNI_NO_MEDICO;
import static es.uclm.esi.iso2.radiodiagnostico.modelo.prueba.DatosSeguimientoPrueba.DNI_PACIENTE;
import static es.uclm.esi.iso2.radiodiagnostico.modelo.prueba.DatosSeguimientoPrueba.HOY;
import static es.uclm.esi.iso2.radiodiagnostico.modelo.prueba.DatosSeguimientoPrueba.validos;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.uclm.esi.iso2.radiodiagnostico.modelo.solicitud.PrioridadSolicitud;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ServicioSolicitudSeguimientoImplTest {

    private static final ZoneId ZONA = ZoneId.of("Europe/Madrid");

    private SolicitudSeguimientoRepositorio repositorio;
    private ServicioSolicitudSeguimiento servicio;

    @BeforeEach
    void crearServicio() {
        Clock reloj = Clock.fixed(HOY.atStartOfDay(ZONA).toInstant(), ZONA);
        repositorio = new SolicitudSeguimientoRepositorioEnMemoria();
        servicio = new ServicioSolicitudSeguimientoImpl(repositorio,
                new CatalogoTiposPruebaSimulado(), new RegistroMedicosSimulado(), reloj);
    }

    // --- Casos correctos ---

    @Test
    void conNumeroDeRepeticionesGeneraUnaPendientePorRepeticion() {
        SolicitudSeguimiento solicitud = servicio.solicitar(validos()
                .inicio(LocalDate.of(2026, 12, 1)).cada(3, UnidadPeriodicidad.MESES)
                .repeticiones(4).crear());

        assertEquals(List.of(LocalDate.of(2026, 12, 1), LocalDate.of(2027, 3, 1),
                LocalDate.of(2027, 6, 1), LocalDate.of(2027, 9, 1)), fechas(solicitud));
        assertTrue(solicitud.repeticiones().stream()
                .allMatch(r -> r.estado() == EstadoRepeticionSeguimiento.PENDIENTE_CITA));
        assertTrue(solicitud.id().startsWith("SEG-"));
        RepeticionSeguimiento primera = solicitud.repeticiones().get(0);
        assertEquals(solicitud.id() + "-1", primera.id());
        assertEquals(solicitud.id(), primera.idSolicitudSeguimiento());
        assertEquals(1, primera.numero());
        assertEquals(4, solicitud.numeroRepeticiones());
        assertNull(solicitud.fechaLimite());
        assertEquals(Optional.of(solicitud), servicio.buscarPorId(solicitud.id()));
    }

    @Test
    void conFechaLimiteIncluyeLaFechaLimiteSiCoincide() {
        SolicitudSeguimiento solicitud = servicio.solicitar(validos()
                .cada(2, UnidadPeriodicidad.SEMANAS).repeticiones(null)
                .limite(LocalDate.of(2026, 12, 16)).crear());

        assertEquals(List.of(HOY, LocalDate.of(2026, 12, 2), LocalDate.of(2026, 12, 16)),
                fechas(solicitud));
        assertNull(solicitud.numeroRepeticiones());
        assertEquals(LocalDate.of(2026, 12, 16), solicitud.fechaLimite());
    }

    @Test
    void conFechaLimiteNoGeneraRepeticionesPosteriores() {
        SolicitudSeguimiento solicitud = servicio.solicitar(validos()
                .cada(10, UnidadPeriodicidad.DIAS).repeticiones(null)
                .limite(HOY.plusDays(15)).crear());

        assertEquals(List.of(HOY, HOY.plusDays(10)), fechas(solicitud));
    }

    @Test
    void guardaLosDatosDeLaSolicitudNormalizados() {
        SolicitudSeguimiento solicitud = servicio.solicitar(new DatosSolicitudSeguimiento(
                " 12345678z ", " 00000001r ", " Mamografía ", " Cribado ", " Programa ",
                PrioridadSolicitud.PREFERENTE, 1, UnidadPeriodicidad.MESES, HOY, 1, null));

        assertEquals(DNI_PACIENTE, solicitud.dniPaciente());
        assertEquals(DNI_MEDICO, solicitud.dniMedico());
        assertEquals("Mamografía", solicitud.tipoPrueba());
        assertEquals("Cribado", solicitud.indicacionClinica());
        assertEquals("Programa", solicitud.justificacion());
        assertEquals(PrioridadSolicitud.PREFERENTE, solicitud.prioridad());
        assertEquals(new PeriodicidadSeguimiento(1, UnidadPeriodicidad.MESES), solicitud.periodicidad());
        assertEquals(List.of(solicitud), servicio.buscarPorPaciente("12345678z"));
    }

    @Test
    void permiteExactamenteElMaximoDeRepeticiones() {
        SolicitudSeguimiento porNumero = servicio.solicitar(validos()
                .repeticiones(ServicioSolicitudSeguimiento.MAXIMO_REPETICIONES).crear());
        SolicitudSeguimiento porFecha = servicio.solicitar(validos()
                .cada(1, UnidadPeriodicidad.DIAS).repeticiones(null)
                .limite(HOY.plusDays(ServicioSolicitudSeguimiento.MAXIMO_REPETICIONES - 1)).crear());

        assertEquals(ServicioSolicitudSeguimiento.MAXIMO_REPETICIONES, porNumero.repeticiones().size());
        assertEquals(ServicioSolicitudSeguimiento.MAXIMO_REPETICIONES, porFecha.repeticiones().size());
    }

    // --- Usuario y tipo de prueba ---

    @Test
    void rechazaUsuarioQueNoEsMedico() {
        UsuarioNoMedicoException error = assertThrows(UsuarioNoMedicoException.class,
                () -> servicio.solicitar(validos().medico(DNI_NO_MEDICO).crear()));

        assertEquals("Solo un médico puede solicitar una prueba de seguimiento periódica.",
                error.getMessage());
        assertTrue(servicio.buscarPorPaciente(DNI_PACIENTE).isEmpty());
    }

    @Test
    void rechazaTipoDePruebaQueNoAdmiteSeguimiento() {
        TipoPruebaSinSeguimientoException error = assertThrows(
                TipoPruebaSinSeguimientoException.class,
                () -> servicio.solicitar(validos().tipo("Radiografía").crear()));

        assertEquals("El tipo de prueba Radiografía no admite seguimiento periódico.",
                error.getMessage());
    }

    @Test
    void rechazaTipoDePruebaQueNoExiste() {
        assertError("El tipo de prueba Gammagrafía no existe en el catálogo.",
                validos().tipo("Gammagrafía"));
    }

    // --- Campos obligatorios ---

    @Test
    void rechazaDatosNulos() {
        assertThrows(DatosSeguimientoInvalidosException.class, () -> servicio.solicitar(null));
    }

    @ParameterizedTest
    @ValueSource(strings = {"dniPaciente", "dniMedico", "tipoPrueba", "indicacionClinica",
            "justificacion", "prioridad", "cada", "unidad", "fechaInicio"})
    void rechazaCampoObligatorioNulo(String campo) {
        assertError("El campo " + campo + " es obligatorio.", validos().sin(campo, false));
    }

    @ParameterizedTest
    @ValueSource(strings = {"dniPaciente", "dniMedico", "tipoPrueba", "indicacionClinica",
            "justificacion"})
    void rechazaCampoObligatorioEnBlanco(String campo) {
        assertError("El campo " + campo + " es obligatorio.", validos().sin(campo, true));
    }

    // --- Criterio de fin ---

    @Test
    void rechazaAmbosCriteriosDeFin() {
        assertError("Indique exactamente un criterio de fin del seguimiento: "
                + "numeroRepeticiones o fechaLimite.", validos().repeticiones(3).limite(HOY.plusYears(1)));
    }

    @Test
    void rechazaNingunCriterioDeFin() {
        assertError("Indique exactamente un criterio de fin del seguimiento: "
                + "numeroRepeticiones o fechaLimite.", validos().repeticiones(null));
    }

    @Test
    void rechazaNumeroDeRepeticionesMenorQueUno() {
        assertError("El campo numeroRepeticiones debe ser mayor o igual que 1.",
                validos().repeticiones(0));
    }

    @Test
    void rechazaNumeroDeRepeticionesMayorQueElMaximo() {
        assertError("El campo numeroRepeticiones no puede ser mayor que 100.",
                validos().repeticiones(ServicioSolicitudSeguimiento.MAXIMO_REPETICIONES + 1));
    }

    @Test
    void rechazaFechaLimiteQueGeneraMasRepeticionesQueElMaximo() {
        assertError("El seguimiento generaría más de 100 repeticiones. Amplíe la periodicidad "
                + "o reduzca el fin del seguimiento.",
                validos().cada(1, UnidadPeriodicidad.DIAS).repeticiones(null).limite(HOY.plusYears(5)));
        assertTrue(servicio.buscarPorPaciente(DNI_PACIENTE).isEmpty());
    }

    // --- Periodicidad y fechas ---

    @Test
    void rechazaPeriodicidadCero() {
        assertError("El campo cada debe ser mayor que 0.", validos().cada(0, UnidadPeriodicidad.DIAS));
    }

    @Test
    void rechazaPeriodicidadNegativa() {
        assertError("El campo cada debe ser mayor que 0.",
                validos().cada(-2, UnidadPeriodicidad.SEMANAS));
    }

    @Test
    void rechazaFechaDeInicioAnteriorAHoy() {
        assertError("El campo fechaInicio no puede ser anterior a hoy.",
                validos().inicio(HOY.minusDays(1)));
    }

    @Test
    void rechazaFechaLimiteIgualALaDeInicio() {
        assertError("El campo fechaLimite debe ser posterior a fechaInicio.",
                validos().repeticiones(null).limite(HOY));
    }

    @Test
    void rechazaFechaLimiteAnteriorALaDeInicio() {
        assertError("El campo fechaLimite debe ser posterior a fechaInicio.",
                validos().inicio(HOY.plusDays(10)).repeticiones(null).limite(HOY.plusDays(5)));
    }

    @Test
    void constructorRechazaDependenciasNulas() {
        CatalogoTiposPrueba catalogo = new CatalogoTiposPruebaSimulado();
        RegistroMedicos medicos = new RegistroMedicosSimulado();
        assertThrows(NullPointerException.class,
                () -> new ServicioSolicitudSeguimientoImpl(null, catalogo, medicos));
        assertThrows(NullPointerException.class,
                () -> new ServicioSolicitudSeguimientoImpl(repositorio, null, medicos));
        assertThrows(NullPointerException.class,
                () -> new ServicioSolicitudSeguimientoImpl(repositorio, catalogo, null));
        assertThrows(NullPointerException.class,
                () -> new ServicioSolicitudSeguimientoImpl(repositorio, catalogo, medicos, null));
    }

    private void assertError(String mensaje, DatosSeguimientoPrueba datos) {
        DatosSeguimientoInvalidosException error = assertThrows(
                DatosSeguimientoInvalidosException.class, () -> servicio.solicitar(datos.crear()));
        assertEquals(mensaje, error.getMessage());
    }

    private static List<LocalDate> fechas(SolicitudSeguimiento solicitud) {
        return solicitud.repeticiones().stream().map(RepeticionSeguimiento::fechaPrevista).toList();
    }
}
