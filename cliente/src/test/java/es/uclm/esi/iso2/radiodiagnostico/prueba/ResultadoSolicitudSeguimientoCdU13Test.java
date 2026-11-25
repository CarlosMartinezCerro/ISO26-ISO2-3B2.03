package es.uclm.esi.iso2.radiodiagnostico.prueba;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * CdU13, unidad RL. Diseño: docs/testing/CdU13-diseno-pruebas.md.
 * Con una variable no hay pares. Los casos de decisiones repiten los de each use.
 */
@DisplayName("CdU13 · ResultadoSolicitudSeguimiento (cliente)")
class ResultadoSolicitudSeguimientoCdU13Test {

    private static final List<RepeticionSeguimiento> DOS = List.of(
            new RepeticionSeguimiento("SEG-1-1", "SEG-1", 1, LocalDate.of(2026, 12, 1), "PENDIENTE_CITA"),
            new RepeticionSeguimiento("SEG-1-2", "SEG-1", 2, LocalDate.of(2027, 1, 1), "PENDIENTE_CITA"));

    @Nested
    @DisplayName("RL · Each use")
    class EachUse {

        @Test
        @DisplayName("RL-EU-01: lista con elementos")
        void listaConElementosEachUse() {
            List<RepeticionSeguimiento> original = new ArrayList<>(DOS);
            ResultadoSolicitudSeguimiento resultado = con(original);
            original.clear();

            assertEquals(DOS, resultado.repeticiones());
            assertThrows(UnsupportedOperationException.class, () -> resultado.repeticiones().clear());
        }

        @Test
        @DisplayName("RL-EU-02: lista vacía")
        void listaVaciaEachUse() {
            assertEquals(List.of(), con(List.of()).repeticiones());
        }

        @Test
        @DisplayName("RL-EU-04: lista con un null")
        void listaConNuloEachUse() {
            List<RepeticionSeguimiento> conNulo = Collections.singletonList(null);

            assertThrows(NullPointerException.class, () -> con(conNulo));
        }
    }

    private static ResultadoSolicitudSeguimiento con(List<RepeticionSeguimiento> repeticiones) {
        return new ResultadoSolicitudSeguimiento(true, "Solicitud registrada.", "SEG-1", repeticiones);
    }
}
