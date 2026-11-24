package es.uclm.esi.iso2.radiodiagnostico.modelo.prueba;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/** CdU13, unidad UT. Diseño: docs/testing/CdU13-diseno-pruebas.md. Con una variable no hay pares. */
@DisplayName("CdU13 · UnidadPeriodicidad")
class UnidadPeriodicidadCdU13Test {

    @Nested
    @DisplayName("UT · Each use")
    class EachUse {

        @ParameterizedTest(name = "{0}")
        @DisplayName("UT: texto que ve el usuario")
        @CsvSource({"UT-EU-01, DIAS, días", "UT-EU-02, SEMANAS, semanas", "UT-EU-03, MESES, meses"})
        void textoEachUse(String id, UnidadPeriodicidad unidad, String esperado) {
            assertEquals(esperado, unidad.toString(), id);
        }
    }
}
