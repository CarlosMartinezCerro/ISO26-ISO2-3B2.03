package es.uclm.esi.iso2.radiodiagnostico.modelo.prueba;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * CdU13, unidad PF. Diseño: docs/testing/CdU13-diseno-pruebas.md.
 * Los casos de decisiones repiten los de each use (apartado 8.3).
 */
@DisplayName("CdU13 · PeriodicidadSeguimiento")
class PeriodicidadSeguimientoCdU13Test {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/uuuu");
    private static final LocalDate[] INICIO = {LocalDate.of(2026, 12, 1), LocalDate.of(2027, 1, 31),
            LocalDate.of(2028, 1, 31), null, LocalDate.MAX};
    private static final int[] INDICE = {0, 1, 2, 99};
    private static final int[] CADA = {3, 1, 2_147_483_647};
    private static final UnidadPeriodicidad[] UNIDAD = {UnidadPeriodicidad.DIAS,
            UnidadPeriodicidad.SEMANAS, UnidadPeriodicidad.MESES, null};

    @Nested
    @DisplayName("PF · Each use")
    class EachUse {

        @ParameterizedTest(name = "{0}")
        @DisplayName("PF: fechaRepeticion")
        @CsvSource(delimiter = '|', value = {
                "PF-EU-01 | A1 O1 N1 U1 | 01/12/2026",
                "PF-EU-02 | A1 O4 N1 U1 | 24/09/2027",
                "PF-EU-03 | A1 O3 N1 U2 | 12/01/2027",
                "PF-EU-05 | A3 O2 N2 U3 | 29/02/2028",
                "PF-EU-06 | A4 O2 N1 U1 | NullPointerException",
                "PF-EU-07 | A5 O1 N1 U1 | +999999999-12-31",
                "PF-EU-08 | A5 O2 N1 U1 | DateTimeException",
                "PF-EU-09 | A1 O3 N3 U1 | +11761247-12-19",
                "PF-EU-10 | A1 O2 N1 U4 | NullPointerException"
        })
        void fechaRepeticionEachUse(String id, String entradas, String esperado) {
            comprobar(id, entradas, esperado);
        }
    }

    @Nested
    @DisplayName("PF · Pairwise")
    class Pairwise {

        @ParameterizedTest(name = "{0}")
        @DisplayName("PF-PW-01 a PF-PW-21")
        @CsvFileSource(resources = "/cdu13/pf-pairwise.csv", delimiter = '|')
        void paresPairwise(String id, String entradas, String esperado) {
            comprobar(id, entradas, esperado);
        }
    }

    private static void comprobar(String id, String entradas, String esperado) {
        String[] codigos = entradas.trim().split(" ");
        LocalDate inicio = INICIO[valor(codigos[0])];
        int indice = INDICE[valor(codigos[1])];
        PeriodicidadSeguimiento periodicidad =
                new PeriodicidadSeguimiento(CADA[valor(codigos[2])], UNIDAD[valor(codigos[3])]);

        switch (esperado) {
            case "NullPointerException" -> assertThrows(NullPointerException.class,
                    () -> periodicidad.fechaRepeticion(inicio, indice), id);
            case "DateTimeException" -> assertThrows(DateTimeException.class,
                    () -> periodicidad.fechaRepeticion(inicio, indice), id);
            default -> assertEquals(esperado.startsWith("+") ? LocalDate.parse(esperado)
                    : LocalDate.parse(esperado, FECHA), periodicidad.fechaRepeticion(inicio, indice), id);
        }
    }

    private static int valor(String codigo) {
        return Integer.parseInt(codigo.substring(1)) - 1;
    }
}
