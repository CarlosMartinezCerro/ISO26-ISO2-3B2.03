package es.uclm.esi.iso2.radiodiagnostico.modelo.prueba;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import es.uclm.esi.iso2.radiodiagnostico.modelo.solicitud.PrioridadSolicitud;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * CdU13, unidades MG, MI y MP. Diseño: docs/testing/CdU13-diseno-pruebas.md.
 * Con dos variables, pairwise coincide con el producto cartesiano y contiene a each use (6.7).
 */
@DisplayName("CdU13 · SolicitudSeguimientoRepositorioEnMemoria")
class SolicitudSeguimientoRepositorioEnMemoriaCdU13Test {

    private static final SolicitudSeguimiento SEG_1 = solicitud("SEG-1", "12345678Z", "TAC");
    private static final SolicitudSeguimiento SEG_1_NUEVA = solicitud("SEG-1", "12345678Z", "Mamografía");
    private static final SolicitudSeguimiento SEG_2 = solicitud("SEG-2", "12345678Z", "TAC");
    private static final SolicitudSeguimiento SEG_3 = solicitud("SEG-3", "87654321X", "TAC");

    @Nested
    @DisplayName("MG, MI y MP · Each use")
    class EachUse {

        @ParameterizedTest(name = "{0}")
        @DisplayName("MG: guardar")
        @CsvSource({"MG-PW-01, N1, Q1", "MG-PW-02, N1, Q2", "MG-PW-03, N1, Q3", "MG-PW-04, N2, Q1"})
        void guardarEachUse(String id, String solicitud, String mapa) {
            comprobarGuardar(id, solicitud, mapa);
        }

        @ParameterizedTest(name = "{0}")
        @DisplayName("MI: buscarPorId")
        @CsvSource({"MI-PW-02, SEG-1, Q2, true", "MI-PW-03, SEG-9, Q1, false", "MI-PW-06, , Q2, false"})
        void buscarPorIdEachUse(String id, String clave, String mapa, boolean esperada) {
            comprobarBuscarPorId(id, clave, mapa, esperada);
        }

        @ParameterizedTest(name = "{0}")
        @DisplayName("MP: buscarPorPaciente")
        @CsvSource({"MP-PW-02, 12345678Z, L2, 1", "MP-PW-06, 12345678z, L3, 2",
                "MP-PW-07, 00000000T, L1, 0", "MP-PW-12, , L3, 0"})
        void buscarPorPacienteEachUse(String id, String dni, String mapa, int cuantas) {
            comprobarBuscarPorPaciente(id, dni, mapa, cuantas);
        }
    }

    @Nested
    @DisplayName("MG, MI y MP · Pairwise")
    class Pairwise {

        @ParameterizedTest(name = "{0}")
        @DisplayName("MG: guardar null")
        @CsvSource({"MG-PW-05, N2, Q2", "MG-PW-06, N2, Q3"})
        void guardarPairwise(String id, String solicitud, String mapa) {
            comprobarGuardar(id, solicitud, mapa);
        }

        @ParameterizedTest(name = "{0}")
        @DisplayName("MI: buscarPorId")
        @CsvSource({"MI-PW-01, SEG-1, Q1, false", "MI-PW-04, SEG-9, Q2, false", "MI-PW-05, , Q1, false"})
        void buscarPorIdPairwise(String id, String clave, String mapa, boolean esperada) {
            comprobarBuscarPorId(id, clave, mapa, esperada);
        }

        @ParameterizedTest(name = "{0}")
        @DisplayName("MP: buscarPorPaciente")
        @CsvSource({"MP-PW-01, 12345678Z, L1, 0", "MP-PW-03, 12345678Z, L3, 2",
                "MP-PW-04, 12345678z, L1, 0", "MP-PW-05, 12345678z, L2, 1",
                "MP-PW-08, 00000000T, L2, 0", "MP-PW-09, 00000000T, L3, 0",
                "MP-PW-10, , L1, 0", "MP-PW-11, , L2, 0"})
        void buscarPorPacientePairwise(String id, String dni, String mapa, int cuantas) {
            comprobarBuscarPorPaciente(id, dni, mapa, cuantas);
        }
    }

    private static void comprobarGuardar(String id, String codigoSolicitud, String mapa) {
        List<SolicitudSeguimiento> antes = contenido(mapa);
        SolicitudSeguimientoRepositorio repositorio = con(antes);

        if (codigoSolicitud.equals("N2")) {
            assertThrows(NullPointerException.class, () -> repositorio.guardar(null), id);
            assertEquals(antes, repositorio.buscarPorPaciente("12345678Z"), id + ": sin cambios");
            return;
        }
        repositorio.guardar(SEG_1_NUEVA);

        assertEquals(Optional.of(SEG_1_NUEVA), repositorio.buscarPorId("SEG-1"), id);
        assertEquals(mapa.equals("Q3") ? List.of(SEG_2, SEG_1_NUEVA) : List.of(SEG_1_NUEVA),
                repositorio.buscarPorPaciente("12345678Z"), id);
    }

    private static void comprobarBuscarPorId(String id, String clave, String mapa, boolean esperada) {
        List<SolicitudSeguimiento> antes = contenido(mapa);
        SolicitudSeguimientoRepositorio repositorio = con(antes);

        assertEquals(esperada ? Optional.of(SEG_1) : Optional.empty(), repositorio.buscarPorId(clave), id);

        assertEquals(antes, repositorio.buscarPorPaciente("12345678Z"), id + ": sin cambios");
    }

    private static void comprobarBuscarPorPaciente(String id, String dni, String mapa, int cuantas) {
        List<SolicitudSeguimiento> antes = contenido(mapa);
        SolicitudSeguimientoRepositorio repositorio = con(antes);

        assertEquals(List.of(SEG_1, SEG_2).subList(0, cuantas), repositorio.buscarPorPaciente(dni), id);

        for (SolicitudSeguimiento solicitud : antes) {
            assertEquals(Optional.of(solicitud), repositorio.buscarPorId(solicitud.id()), id);
        }
    }

    private static List<SolicitudSeguimiento> contenido(String mapa) {
        return switch (mapa) {
            case "Q1", "L1" -> List.of();
            case "Q2", "L2" -> List.of(SEG_1);
            case "Q3" -> List.of(SEG_2);
            case "L3" -> List.of(SEG_1, SEG_3, SEG_2);
            default -> throw new IllegalArgumentException(mapa);
        };
    }

    private static SolicitudSeguimientoRepositorio con(List<SolicitudSeguimiento> solicitudes) {
        SolicitudSeguimientoRepositorio repositorio = new SolicitudSeguimientoRepositorioEnMemoria();
        solicitudes.forEach(repositorio::guardar);
        return repositorio;
    }

    private static SolicitudSeguimiento solicitud(String id, String dniPaciente, String tipo) {
        return new SolicitudSeguimiento(id, dniPaciente, "00000001R", tipo, "Indicación ficticia",
                "Justificación ficticia", PrioridadSolicitud.ORDINARIO,
                new PeriodicidadSeguimiento(1, UnidadPeriodicidad.MESES), LocalDate.of(2026, 12, 1),
                1, null, List.of());
    }
}
