package es.uclm.esi.iso2.radiodiagnostico.modelo.prueba;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.uclm.esi.iso2.radiodiagnostico.modelo.solicitud.PrioridadSolicitud;
import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * CdU13, unidades SS, SC, SI y SP. Diseño: docs/testing/CdU13-diseno-pruebas.md.
 * Solo están los casos que no cubre (o cubre en parte) {@link ServicioSolicitudSeguimientoImplTest}.
 * Los casos de decisiones y MC/DC repiten los de each use (apartados 8 y 9).
 * Columnas de SS: id, entradas, fecha límite (si la hay) y resultado esperado.
 */
@DisplayName("CdU13 · ServicioSolicitudSeguimientoImpl")
class ServicioSolicitudSeguimientoImplCdU13Test {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/uuuu");
    private static final SolicitudSeguimiento SEG_1 = new SolicitudSeguimiento("SEG-1", "12345678Z",
            "00000001R", "TAC", "Indicación ficticia", "Justificación ficticia",
            PrioridadSolicitud.URGENTE, new PeriodicidadSeguimiento(1, UnidadPeriodicidad.MESES),
            LocalDate.of(2026, 12, 1), 1, null, List.of());

    @Nested
    @DisplayName("SS, SC, SI y SP · Each use")
    class EachUse {

        @ParameterizedTest(name = "{0}")
        @DisplayName("SS: solicitudes válidas")
        @CsvSource(delimiter = '|', value = {
                "SS-EU-01 | D1 M1 G1 T1 C1 I1 J1 P3 N1 U3 F1 K2 E1 B1 |  | OK(4): 01/12/2026 → 01/09/2027",
                "SS-EU-02 | D2 M2 G1 T2 C1 I2 J2 P2 N2 U3 F3 K2 E2 B1 |  | OK(1): 18/11/2026",
                "SS-EU-03 | D1 M1 G1 T1 C1 I1 J1 P3 N3 U2 F3 K2 E15 B1 | 2026-12-16 | OK(3): 18/11/2026 → 16/12/2026",
                "SS-EU-04 | D1 M1 G1 T1 C1 I1 J1 P3 N1 U3 F3 K2 E5 B1 |  | OK(100): 18/11/2026 → 18/08/2051",
                "SS-EU-05 | D1 M1 G1 T1 C1 I1 J1 P3 N2 U1 F3 K2 E18 B1 | 2027-02-25 | OK(100): 18/11/2026 → 25/02/2027",
                "SS-EU-06 | D1 M1 G1 T1 C1 I3 J3 P1 N2 U1 F2 K1 E3 B1 |  | OK(2): 17/11/2026 → 18/11/2026",
                "SS-EU-07 | D1 M1 G1 T1 C1 I8 J8 P3 N3 U2 F4 K3 E4 B1 |  | OK(99): 19/11/2026 → 22/08/2030",
                "SS-EU-08 | D1 M1 G1 T1 C1 I1 J1 P3 N2 U3 F7 K2 E16 B1 | 2027-03-30 | OK(2): 31/01/2027 → 28/02/2027",
                "SS-EU-09 | D1 M1 G1 T1 C1 I1 J1 P3 N1 U3 F3 K2 E11 B1 | 2026-11-19 | OK(1): 18/11/2026",
                "SS-EU-10 | D1 M1 G1 T1 C1 I1 J1 P3 N2 U1 F3 K2 E12 B1 | 2026-11-20 | OK(3): 18/11/2026 → 20/11/2026",
                "SS-EU-11 | D1 M1 G1 T1 C1 I1 J1 P3 N2 U1 F3 K2 E17 B1 | 2027-02-24 | OK(99): 18/11/2026 → 24/02/2027",
                "SS-EU-12 | D1 M1 G1 T1 C1 I1 J1 P3 N2 U2 F3 K2 E19 B1 | 2028-10-17 | OK(100): 18/11/2026 → 11/10/2028",
                "SS-EU-13 | D1 M1 G1 T1 C1 I1 J1 P3 N7 U1 F3 K2 E3 B1 |  | OK(2): 18/11/2026 → +5881637-05-28"
        })
        void solicitudesValidasEachUse(String id, String entradas, LocalDate limite, String esperado) {
            comprobar(id, entradas, limite, esperado);
        }

        @ParameterizedTest(name = "{0}")
        @DisplayName("SS: un solo dato inválido o un solo fallo")
        @CsvSource(delimiter = '|', value = {
                "SS-EU-14 | D3 M1 G1 T1 C1 I1 J1 P3 N1 U3 F3 K2 E1 B1 |  | DI(dniPaciente)",
                "SS-EU-15 | D4 M1 G1 T1 C1 I1 J1 P3 N1 U3 F3 K2 E1 B1 |  | DI(dniPaciente)",
                "SS-EU-16 | D5 M1 G1 T1 C1 I1 J1 P3 N1 U3 F3 K2 E1 B1 |  | DI(dniPaciente)",
                "SS-EU-20 | D1 M3 G1 T1 C1 I1 J1 P3 N1 U3 F3 K2 E1 B1 |  | DI(dniMedico)",
                "SS-EU-21 | D1 M4 G1 T1 C1 I1 J1 P3 N1 U3 F3 K2 E1 B1 |  | DI(dniMedico)",
                "SS-EU-22 | D1 M5 G1 T1 C1 I1 J1 P3 N1 U3 F3 K2 E1 B1 |  | DI(dniMedico)",
                "SS-EU-24 | D1 M1 G3 T1 C1 I1 J1 P3 N1 U3 F3 K2 E1 B1 |  | ISE",
                "SS-EU-25 | D1 M1 G1 T3 C1 I1 J1 P3 N1 U3 F3 K2 E1 B1 |  | DI(tipoPrueba)",
                "SS-EU-26 | D1 M1 G1 T4 C1 I1 J1 P3 N1 U3 F3 K2 E1 B1 |  | DI(tipoPrueba)",
                "SS-EU-27 | D1 M1 G1 T5 C1 I1 J1 P3 N1 U3 F3 K2 E1 B1 |  | DI(tipoPrueba)",
                "SS-EU-28 | D1 M1 G1 T1 C2 I1 J1 P3 N1 U3 F3 K2 E1 B1 |  | DI(tipoPrueba)",
                "SS-EU-29 | D1 M1 G1 T1 C3 I1 J1 P3 N1 U3 F3 K2 E1 B1 |  | TSS",
                "SS-EU-30 | D1 M1 G1 T1 C4 I1 J1 P3 N1 U3 F3 K2 E1 B1 |  | ISE",
                "SS-EU-31 | D1 M1 G1 T1 C1 I4 J1 P3 N1 U3 F3 K2 E1 B1 |  | DI(indicacionClinica)",
                "SS-EU-32 | D1 M1 G1 T1 C1 I5 J1 P3 N1 U3 F3 K2 E1 B1 |  | DI(indicacionClinica)",
                "SS-EU-33 | D1 M1 G1 T1 C1 I6 J1 P3 N1 U3 F3 K2 E1 B1 |  | DI(indicacionClinica)",
                "SS-EU-35 | D1 M1 G1 T1 C1 I1 J4 P3 N1 U3 F3 K2 E1 B1 |  | DI(justificacion)",
                "SS-EU-36 | D1 M1 G1 T1 C1 I1 J5 P3 N1 U3 F3 K2 E1 B1 |  | DI(justificacion)",
                "SS-EU-37 | D1 M1 G1 T1 C1 I1 J6 P3 N1 U3 F3 K2 E1 B1 |  | DI(justificacion)",
                "SS-EU-39 | D1 M1 G1 T1 C1 I1 J1 P4 N1 U3 F3 K2 E1 B1 |  | DI(prioridad)",
                "SS-EU-40 | D1 M1 G1 T1 C1 I1 J1 P3 N4 U1 F3 K2 E1 B1 |  | DI(cada)",
                "SS-EU-41 | D1 M1 G1 T1 C1 I1 J1 P3 N5 U2 F3 K2 E1 B1 |  | DI(cada)",
                "SS-EU-42 | D1 M1 G1 T1 C1 I1 J1 P3 N6 U3 F3 K2 E1 B1 |  | DI(cada)",
                "SS-EU-43 | D1 M1 G1 T1 C1 I1 J1 P3 N8 U3 F3 K2 E1 B1 |  | DI(cada)",
                "SS-EU-44 | D1 M1 G1 T1 C1 I1 J1 P3 N1 U4 F3 K2 E1 B1 |  | DI(unidad)",
                "SS-EU-45 | D1 M1 G1 T1 C1 I1 J1 P3 N1 U3 F5 K2 E1 B1 |  | DI(fechaInicio)",
                "SS-EU-46 | D1 M1 G1 T1 C1 I1 J1 P3 N1 U3 F2 K2 E1 B1 |  | DI(fechaInicio)",
                "SS-EU-47 | D1 M1 G1 T1 C1 I1 J1 P3 N1 U3 F3 K3 E1 B1 |  | DI(fechaInicio)",
                "SS-EU-48 | D1 M1 G1 T1 C1 I1 J1 P3 N1 U3 F3 K2 E6 B1 |  | DI(fin)",
                "SS-EU-49 | D1 M1 G1 T1 C1 I1 J1 P3 N1 U3 F3 K2 E7 B1 |  | DI(fin)",
                "SS-EU-50 | D1 M1 G1 T1 C1 I1 J1 P3 N1 U3 F3 K2 E8 B1 |  | DI(fin)",
                "SS-EU-51 | D1 M1 G1 T1 C1 I1 J1 P3 N1 U3 F3 K2 E9 B1 |  | DI(fin)",
                "SS-EU-52 | D1 M1 G1 T1 C1 I1 J1 P3 N1 U3 F3 K2 E10 B1 | 2027-11-18 | DI(fin)",
                "SS-EU-53 | D1 M1 G1 T1 C1 I1 J1 P3 N1 U3 F3 K2 E13 B1 | 2026-11-18 | DI(fin)",
                "SS-EU-54 | D1 M1 G1 T1 C1 I1 J1 P3 N1 U3 F3 K2 E14 B1 | 2026-11-17 | DI(fin)",
                "SS-EU-55 | D1 M1 G1 T1 C1 I1 J1 P3 N1 U3 F3 K2 E20 B1 | 2051-11-18 | DI(fin)",
                "SS-EU-56 | D1 M1 G1 T1 C1 I1 J1 P3 N1 U3 F3 K2 E21 B1 | +999999999-12-31 | DI(fin)",
                "SS-EU-60 | D1 M1 G1 T1 C1 I1 J1 P3 N1 U3 F3 K2 E1 B2 |  | ISE"
        })
        void unDatoInvalidoEachUse(String id, String entradas, LocalDate limite, String esperado) {
            comprobar(id, entradas, limite, esperado);
        }

        @ParameterizedTest(name = "{0}")
        @DisplayName("Posible fallo PX-2: textos con solo caracteres de control")
        @CsvSource(delimiter = '|', value = {
                "SS-EU-17 | D6 M1 G1 T1 C1 I1 J1 P3 N1 U3 F3 K2 E1 B1 |  | DI(dniPaciente)",
                "SS-EU-34 | D1 M1 G1 T1 C1 I7 J1 P3 N1 U3 F3 K2 E1 B1 |  | DI(indicacionClinica)",
                "SS-EU-38 | D1 M1 G1 T1 C1 I1 J7 P3 N1 U3 F3 K2 E1 B1 |  | DI(justificacion)"
        })
        void posibleFalloPx2EachUse(String id, String entradas, LocalDate limite, String esperado) {
            comprobar(id, entradas, limite, esperado);
        }

        @ParameterizedTest(name = "{0}")
        @DisplayName("Posible fallo PX-5: la fecha se desborda")
        @CsvSource(delimiter = '|', value = {
                "SS-EU-57 | D1 M1 G1 T1 C1 I1 J1 P3 N1 U3 F6 K2 E2 B1 |  | OK(1): +999999999-12-31",
                "SS-EU-58 | D1 M1 G1 T1 C1 I1 J1 P3 N1 U3 F6 K2 E1 B1 |  | DI(fechas)",
                "SS-EU-59 | D1 M1 G1 T1 C1 I1 J1 P3 N7 U3 F3 K2 E5 B1 |  | DI(fechas)"
        })
        void posibleFalloPx5EachUse(String id, String entradas, LocalDate limite, String esperado) {
            comprobar(id, entradas, limite, esperado);
        }

        @Disabled("PX-6: pendiente de decisión del equipo, ver apartado 11")
        @ParameterizedTest(name = "{0}")
        @DisplayName("Posible fallo PX-6: DNI del paciente con formato o letra incorrectos")
        @CsvSource(delimiter = '|', value = {
                "SS-EU-18 | D7 M1 G1 T1 C1 I1 J1 P3 N1 U3 F3 K2 E1 B1 |  | DI(dniPaciente)",
                "SS-EU-19 | D8 M1 G1 T1 C1 I1 J1 P3 N1 U3 F3 K2 E1 B1 |  | DI(dniPaciente)"
        })
        void posibleFalloPx6EachUse(String id, String entradas, LocalDate limite, String esperado) {
            comprobar(id, entradas, limite, esperado);
        }

        @Test
        @DisplayName("SI-PW-01: la encuentra")
        void buscarPorIdLaEncuentraEachUse() {
            comprobarBuscarPorId("SEG-1", Optional.of(SEG_1));
        }

        @Test
        @DisplayName("SI-PW-03: id null")
        void buscarPorIdNuloEachUse() {
            comprobarBuscarPorId(null, Optional.empty());
        }

        @Test
        @DisplayName("SP-PW-01: tiene solicitudes")
        void buscarPorPacienteEachUse() {
            comprobarBuscarPorPaciente("12345678Z", "12345678Z", List.of(SEG_1));
        }

        @Test
        @DisplayName("SP-PW-04: DNI sin normalizar, no tiene")
        void buscarPorPacienteSinNormalizarEachUse() {
            comprobarBuscarPorPaciente(" 12345678z ", "12345678Z", List.of());
        }

        @Test
        @DisplayName("SP-PW-05: DNI null")
        void buscarPorPacienteNuloEachUse() {
            comprobarBuscarPorPaciente(null, null, List.of());
        }
    }

    @Nested
    @DisplayName("SS, SC, SI y SP · Pairwise")
    class Pairwise {

        @ParameterizedTest(name = "{0}")
        @DisplayName("SS-PW-001 a SS-PW-173")
        @CsvFileSource(resources = "/cdu13/ss-pairwise.csv", delimiter = '|')
        void paresPairwise(String id, String entradas, LocalDate limite, String esperado) {
            comprobar(id, entradas, limite, esperado);
        }

        @ParameterizedTest(name = "{0}")
        @DisplayName("SC: varias dependencias null")
        @CsvSource({"SC-PW-02, true, false, false, true", "SC-PW-03, false, true, false, true",
                "SC-PW-04, false, false, true, false"})
        void dependenciasNulasPairwise(String id, boolean solicitudes, boolean catalogo,
                                       boolean medicos, boolean reloj) {
            assertThrows(NullPointerException.class, () -> new ServicioSolicitudSeguimientoImpl(
                    solicitudes ? new SolicitudSeguimientoRepositorioEnMemoria() : null,
                    catalogo ? new CatalogoTiposPruebaSimulado() : null,
                    medicos ? new RegistroMedicosSimulado() : null,
                    reloj ? Clock.systemDefaultZone() : null), id);
        }

        @Test
        @DisplayName("SI-PW-02: no la encuentra")
        void buscarPorIdNoLaEncuentraPairwise() {
            comprobarBuscarPorId("SEG-1", Optional.empty());
        }

        @Test
        @DisplayName("SP-PW-02: no tiene solicitudes")
        void buscarPorPacienteSinSolicitudesPairwise() {
            comprobarBuscarPorPaciente("12345678Z", "12345678Z", List.of());
        }

        @Test
        @DisplayName("SP-PW-03: DNI sin normalizar, tiene solicitudes")
        void buscarPorPacienteSinNormalizarPairwise() {
            comprobarBuscarPorPaciente(" 12345678z ", "12345678Z", List.of(SEG_1));
        }
    }

    /** Comprueba el resultado y la postcondición (PC1 si es OK, PC2 en el resto). */
    private static void comprobar(String id, String entradas, LocalDate limite, String esperado) {
        CasoSeguimiento caso = new CasoSeguimiento(entradas, limite);
        ServicioSolicitudSeguimiento servicio = caso.servicio();

        if (esperado.startsWith("OK(")) {
            SolicitudSeguimiento solicitud = servicio.solicitar(caso.datos);

            comprobarSolicitud(id, caso, solicitud, esperado);
            assertEquals(List.of(solicitud), caso.repositorio.guardadas, id + ": PC1");
        } else {
            RuntimeException error = assertThrows(RuntimeException.class,
                    () -> servicio.solicitar(caso.datos), id);
            assertTrue(Arrays.stream(esperado.split(" o ")).anyMatch(e -> coincide(e, error)),
                    () -> id + ": se esperaba " + esperado + " y se ha lanzado " + error);
            assertEquals(List.of(), caso.repositorio.guardadas, id + ": PC2");
        }
    }

    /** {@code esperado} es, por ejemplo, "OK(4): 01/12/2026 → 01/09/2027". */
    private static void comprobarSolicitud(String id, CasoSeguimiento caso,
                                           SolicitudSeguimiento solicitud, String esperado) {
        int cuantas = Integer.parseInt(esperado.substring(3, esperado.indexOf(')')));
        String[] fechas = esperado.substring(esperado.indexOf(": ") + 2).split(" → ");
        List<RepeticionSeguimiento> repeticiones = solicitud.repeticiones();

        assertTrue(solicitud.id().matches("SEG-.+"), () -> id + ": id " + solicitud.id());
        assertEquals(caso.esperada(solicitud.id(), repeticiones), solicitud, id);
        assertEquals(cuantas, repeticiones.size(), id + ": repeticiones");
        assertEquals(fecha(fechas[0]), repeticiones.get(0).fechaPrevista(), id + ": primera fecha");
        assertEquals(fecha(fechas[fechas.length - 1]),
                repeticiones.get(cuantas - 1).fechaPrevista(), id + ": última fecha");
        List<RepeticionSeguimiento> esperadas = new ArrayList<>();
        for (int i = 0; i < cuantas; i++) {
            esperadas.add(new RepeticionSeguimiento(solicitud.id() + "-" + (i + 1), solicitud.id(), i + 1,
                    repeticiones.get(i).fechaPrevista(), EstadoRepeticionSeguimiento.PENDIENTE_CITA));
        }
        assertEquals(esperadas, repeticiones, id + ": números, ids y estados");
    }

    private static LocalDate fecha(String texto) {
        return texto.startsWith("+") ? LocalDate.parse(texto) : LocalDate.parse(texto, FECHA);
    }

    private static boolean coincide(String esperado, RuntimeException error) {
        return switch (esperado) {
            case "UNM" -> error instanceof UsuarioNoMedicoException;
            case "TSS" -> error instanceof TipoPruebaSinSeguimientoException;
            case "ISE" -> error instanceof IllegalStateException;
            default -> error instanceof DatosSeguimientoInvalidosException
                    && Arrays.stream(esperado.substring(3, esperado.length() - 1).split(", "))
                            .anyMatch(campo -> nombra(error.getMessage(), campo));
        };
    }

    private static boolean nombra(String mensaje, String campo) {
        return switch (campo) {
            case "tipoPrueba" -> mensaje.contains("tipoPrueba") || mensaje.contains("tipo de prueba");
            case "fin" -> mensaje.contains("numeroRepeticiones") || mensaje.contains("fechaLimite")
                    || mensaje.contains("repeticiones");
            case "fechas" -> true;
            default -> mensaje.contains(campo);
        };
    }

    private static void comprobarBuscarPorId(String id, Optional<SolicitudSeguimiento> respuesta) {
        RepositorioDeBusqueda repositorio = new RepositorioDeBusqueda(respuesta, List.of());

        assertEquals(respuesta, servicioCon(repositorio).buscarPorId(id));

        assertEquals(Arrays.asList(id), repositorio.idsRecibidos);
    }

    private static void comprobarBuscarPorPaciente(String dni, String dniEsperado,
                                                   List<SolicitudSeguimiento> respuesta) {
        RepositorioDeBusqueda repositorio = new RepositorioDeBusqueda(Optional.empty(), respuesta);

        assertEquals(respuesta, servicioCon(repositorio).buscarPorPaciente(dni));

        assertEquals(Arrays.asList(dniEsperado), repositorio.dnisRecibidos);
    }

    private static ServicioSolicitudSeguimiento servicioCon(SolicitudSeguimientoRepositorio repositorio) {
        return new ServicioSolicitudSeguimientoImpl(repositorio, new CatalogoTiposPruebaSimulado(),
                new RegistroMedicosSimulado(), Clock.systemDefaultZone());
    }

    /** Responde lo indicado y anota lo que recibe. */
    private static final class RepositorioDeBusqueda implements SolicitudSeguimientoRepositorio {
        final List<String> idsRecibidos = new ArrayList<>();
        final List<String> dnisRecibidos = new ArrayList<>();
        private final Optional<SolicitudSeguimiento> porId;
        private final List<SolicitudSeguimiento> porPaciente;

        RepositorioDeBusqueda(Optional<SolicitudSeguimiento> porId, List<SolicitudSeguimiento> porPaciente) {
            this.porId = porId;
            this.porPaciente = porPaciente;
        }

        @Override
        public Optional<SolicitudSeguimiento> buscarPorId(String id) {
            idsRecibidos.add(id);
            return porId;
        }

        @Override
        public List<SolicitudSeguimiento> buscarPorPaciente(String dniPaciente) {
            dnisRecibidos.add(dniPaciente);
            return porPaciente;
        }

        @Override
        public void guardar(SolicitudSeguimiento solicitud) {
            throw new UnsupportedOperationException();
        }
    }
}
