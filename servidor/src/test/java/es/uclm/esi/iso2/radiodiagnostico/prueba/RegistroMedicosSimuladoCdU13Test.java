package es.uclm.esi.iso2.radiodiagnostico.prueba;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * CdU13, unidades RM y RC. Diseño: docs/testing/CdU13-diseno-pruebas.md.
 * Con dos variables, pairwise coincide con el producto cartesiano y contiene a each use (6.7).
 */
@DisplayName("CdU13 · RegistroMedicosSimulado")
class RegistroMedicosSimuladoCdU13Test {

    @Nested
    @DisplayName("RM y RC · Each use")
    class EachUse {

        @Test
        @DisplayName("RM-PW-11: DNI vacío, registro vacío")
        void dniVacioEachUse() {
            assertFalse(registro("S2").esMedico(""));
        }

        @Disabled("PX-7: pendiente de decisión del equipo, ver apartado 11")
        @Test
        @DisplayName("RM-PW-03 · Posible fallo PX-7: médico registrado en minúscula")
        void posibleFalloPx7EachUse() {
            assertTrue(registro("S3").esMedico("00000001R"));
        }

        @Test
        @DisplayName("RC-EU-01: conjunto presente")
        void conjuntoPresenteEachUse() {
            Set<String> medicos = new HashSet<>(Set.of("11111111H"));
            RegistroMedicos registro = new RegistroMedicosSimulado(medicos);
            medicos.clear();

            assertTrue(registro.esMedico("11111111H"));
            assertFalse(registro.esMedico("00000001R"));
        }

        @Test
        @DisplayName("RC-EU-02: conjunto null")
        void conjuntoNuloEachUse() {
            assertThrows(NullPointerException.class, () -> new RegistroMedicosSimulado(null));
        }

        @Test
        @DisplayName("RC-EU-03: conjunto con un null")
        void conjuntoConNuloEachUse() {
            Set<String> conNulo = new HashSet<>(Collections.singleton(null));

            assertThrows(NullPointerException.class, () -> new RegistroMedicosSimulado(conNulo));
        }
    }

    @Nested
    @DisplayName("RM y RC · Pairwise")
    class Pairwise {

        @ParameterizedTest(name = "{0}")
        @DisplayName("RM: esMedico")
        @CsvSource({"RM-PW-02, 00000001R, S2", "RM-PW-05, ' 00000001r ', S2",
                "RM-PW-08, 99999999R, S2", "RM-PW-09, 99999999R, S3", "RM-PW-10, '', S1",
                "RM-PW-12, '', S3", "RM-PW-14, , S2", "RM-PW-15, , S3"})
        void noEsMedicoPairwise(String id, String dni, String conjunto) {
            assertFalse(registro(conjunto).esMedico(dni), id);
        }

        @Disabled("PX-7: pendiente de decisión del equipo, ver apartado 11")
        @Test
        @DisplayName("RM-PW-06 · Posible fallo PX-7: sin normalizar y registrado en minúscula")
        void posibleFalloPx7Pairwise() {
            assertTrue(registro("S3").esMedico(" 00000001r "));
        }
    }

    private static RegistroMedicos registro(String conjunto) {
        return switch (conjunto) {
            case "S1" -> new RegistroMedicosSimulado();
            case "S2" -> new RegistroMedicosSimulado(Set.of());
            case "S3" -> new RegistroMedicosSimulado(Set.of("00000001r"));
            default -> throw new IllegalArgumentException(conjunto);
        };
    }
}
