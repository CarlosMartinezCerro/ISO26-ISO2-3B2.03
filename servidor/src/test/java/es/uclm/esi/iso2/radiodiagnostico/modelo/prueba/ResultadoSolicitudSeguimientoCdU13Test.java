package es.uclm.esi.iso2.radiodiagnostico.modelo.prueba;

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
 * CdU13, unidades RS y RE. Diseño: docs/testing/CdU13-diseno-pruebas.md.
 * Con una variable no hay pares. Los casos de decisiones repiten los de each use.
 */
@DisplayName("CdU13 · ResultadoSolicitudSeguimiento (servidor)")
class ResultadoSolicitudSeguimientoCdU13Test {

    private static final List<RepeticionSeguimiento> DOS = List.of(
            new RepeticionSeguimiento("SEG-1-1", "SEG-1", 1, LocalDate.of(2026, 12, 1),
                    EstadoRepeticionSeguimiento.PENDIENTE_CITA),
            new RepeticionSeguimiento("SEG-1-2", "SEG-1", 2, LocalDate.of(2027, 1, 1),
                    EstadoRepeticionSeguimiento.PENDIENTE_CITA));

    @Nested
    @DisplayName("RS y RE · Each use")
    class EachUse {

        @Test
        @DisplayName("RS-EU-01: lista con elementos")
        void listaConElementosEachUse() {
            List<RepeticionSeguimiento> original = new ArrayList<>(DOS);
            ResultadoSolicitudSeguimiento resultado = con(original);
            original.clear();

            assertEquals(DOS, resultado.repeticiones());
            assertThrows(UnsupportedOperationException.class, () -> resultado.repeticiones().clear());
        }

        @Test
        @DisplayName("RS-EU-02: lista vacía")
        void listaVaciaEachUse() {
            assertEquals(List.of(), con(List.of()).repeticiones());
        }

        @Test
        @DisplayName("RS-EU-03: lista null")
        void listaNulaEachUse() {
            assertEquals(List.of(), con(null).repeticiones());
        }

        @Test
        @DisplayName("RS-EU-04: lista con un null")
        void listaConNuloEachUse() {
            List<RepeticionSeguimiento> conNulo = Collections.singletonList(null);

            assertThrows(NullPointerException.class, () -> con(conNulo));
        }

        @Test
        @DisplayName("RE-EU-01: error con mensaje")
        void errorConMensajeEachUse() {
            assertEquals(new ResultadoSolicitudSeguimiento(false, "Error ficticio", null, List.of()),
                    ResultadoSolicitudSeguimiento.error("Error ficticio"));
        }

        @Test
        @DisplayName("RE-EU-02: error sin mensaje")
        void errorSinMensajeEachUse() {
            assertEquals(new ResultadoSolicitudSeguimiento(false, null, null, List.of()),
                    ResultadoSolicitudSeguimiento.error(null));
        }
    }

    private static ResultadoSolicitudSeguimiento con(List<RepeticionSeguimiento> repeticiones) {
        return new ResultadoSolicitudSeguimiento(true, "Solicitud registrada.", "SEG-1", repeticiones);
    }
}
