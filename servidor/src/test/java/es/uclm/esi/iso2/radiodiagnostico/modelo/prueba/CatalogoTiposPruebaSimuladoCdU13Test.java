package es.uclm.esi.iso2.radiodiagnostico.modelo.prueba;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * CdU13, unidades KE y KA. Diseño: docs/testing/CdU13-diseno-pruebas.md.
 * Con una variable no hay pares. Los casos de decisiones y MC/DC ya los cubre
 * {@link SimuladosSeguimientoTest}.
 */
@DisplayName("CdU13 · CatalogoTiposPruebaSimulado")
class CatalogoTiposPruebaSimuladoCdU13Test {

    private final CatalogoTiposPrueba catalogo = new CatalogoTiposPruebaSimulado();

    @Nested
    @DisplayName("KE y KA · Each use")
    class EachUse {

        @ParameterizedTest(name = "{0}")
        @DisplayName("KE: existe")
        @CsvSource({"KE-EU-01, Radiografía, true", "KE-EU-02, Ecografía, true",
                "KE-EU-03, Mamografía, true", "KE-EU-04, TAC, true",
                "KE-EU-05, Resonancia magnética, true", "KE-EU-09, Radiografia, false",
                "KE-EU-10, '', false"})
        void existeEachUse(String id, String tipo, boolean esperado) {
            assertEquals(esperado, catalogo.existe(tipo), id);
        }

        @ParameterizedTest(name = "{0}")
        @DisplayName("KA: admiteSeguimiento")
        @CsvSource({"KA-EU-04, TAC, true", "KA-EU-05, Resonancia magnética, true",
                "KA-EU-06, ' tac ', true", "KA-EU-07, RESONANCIA MAGNÉTICA, true",
                "KA-EU-09, Radiografia, false", "KA-EU-10, '', false", "KA-EU-11, , false"})
        void admiteSeguimientoEachUse(String id, String tipo, boolean esperado) {
            assertEquals(esperado, catalogo.admiteSeguimiento(tipo), id);
        }
    }
}
