package es.uclm.esi.iso2.radiodiagnostico.modelo.prueba;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import es.uclm.esi.iso2.radiodiagnostico.modelo.solicitud.PrioridadSolicitud;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** CdU13, unidad SO. Diseño: docs/testing/CdU13-diseno-pruebas.md. Con una variable no hay pares. */
@DisplayName("CdU13 · SolicitudSeguimiento")
class SolicitudSeguimientoCdU13Test {

    private static final List<RepeticionSeguimiento> DOS = List.of(
            new RepeticionSeguimiento("SEG-1-1", "SEG-1", 1, LocalDate.of(2026, 12, 1),
                    EstadoRepeticionSeguimiento.PENDIENTE_CITA),
            new RepeticionSeguimiento("SEG-1-2", "SEG-1", 2, LocalDate.of(2027, 1, 1),
                    EstadoRepeticionSeguimiento.PENDIENTE_CITA));

    @Nested
    @DisplayName("SO · Each use")
    class EachUse {

        @Test
        @DisplayName("SO-EU-01: lista con elementos")
        void listaConElementosEachUse() {
            List<RepeticionSeguimiento> original = new ArrayList<>(DOS);
            SolicitudSeguimiento solicitud = con(original);
            original.clear();

            assertEquals(DOS, solicitud.repeticiones());
            assertThrows(UnsupportedOperationException.class, () -> solicitud.repeticiones().clear());
        }

        @Test
        @DisplayName("SO-EU-02: lista vacía")
        void listaVaciaEachUse() {
            assertEquals(List.of(), con(List.of()).repeticiones());
        }

        @Test
        @DisplayName("SO-EU-03: lista null")
        void listaNulaEachUse() {
            assertThrows(NullPointerException.class, () -> con(null));
        }

        @Test
        @DisplayName("SO-EU-04: lista con un null")
        void listaConNuloEachUse() {
            List<RepeticionSeguimiento> conNulo = Collections.singletonList(null);

            assertThrows(NullPointerException.class, () -> con(conNulo));
        }
    }

    private static SolicitudSeguimiento con(List<RepeticionSeguimiento> repeticiones) {
        return new SolicitudSeguimiento("SEG-1", "12345678Z", "00000001R", "TAC", "Indicación ficticia",
                "Justificación ficticia", PrioridadSolicitud.URGENTE,
                new PeriodicidadSeguimiento(1, UnidadPeriodicidad.MESES), LocalDate.of(2026, 12, 1),
                2, null, repeticiones);
    }
}
