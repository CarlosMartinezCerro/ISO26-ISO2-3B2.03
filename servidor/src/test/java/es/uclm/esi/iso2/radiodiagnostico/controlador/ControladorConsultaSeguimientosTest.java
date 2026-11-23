package es.uclm.esi.iso2.radiodiagnostico.controlador;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.uclm.esi.iso2.radiodiagnostico.informe.DniPacienteInvalidoException;
import es.uclm.esi.iso2.radiodiagnostico.prueba.ResultadoConsultaSeguimientos;
import es.uclm.esi.iso2.radiodiagnostico.prueba.SeguimientoPeriodico;
import es.uclm.esi.iso2.radiodiagnostico.solicitud.PrioridadSolicitud;
import java.time.LocalDate;
import java.util.Collections;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Pruebas del controlador del servidor del CdU11: cómo convierte la respuesta del servicio en un
 * resultado con mensaje para el cliente.
 */
@DisplayName("CdU11 · Controlador del servidor")
class ControladorConsultaSeguimientosTest {

    private static final SeguimientoPeriodico SEGUIMIENTO = new SeguimientoPeriodico("SEG-1",
            "12345678Z", "00000001R", "TAC", "Indicación ficticia", PrioridadSolicitud.URGENTE,
            "Cada 2 semanas", LocalDate.of(2026, 11, 20), LocalDate.of(2026, 12, 4), 2, 0,
            LocalDate.of(2026, 11, 20), SeguimientoPeriodico.EN_CURSO, java.util.List.of());

    // CP-C01 · Número de seguimientos: 0 (A2), 1 (límite, singular) y varios (plural)
    @ParameterizedTest(name = "{0} seguimiento(s) → {1}")
    @CsvSource(delimiter = '|', value = {
        "0 | El paciente 12345678Z no tiene pruebas de seguimiento periódicas.",
        "1 | Se ha encontrado 1 prueba de seguimiento periódica.",
        "2 | Se han encontrado 2 pruebas de seguimiento periódicas.",
        "5 | Se han encontrado 5 pruebas de seguimiento periódicas."
    })
    @DisplayName("El mensaje depende del número de seguimientos encontrados")
    void mensajeSegunNumeroDeSeguimientos(int cantidad, String mensajeEsperado) {
        ResultadoConsultaSeguimientos resultado = new ControladorConsultaSeguimientos(
                dni -> Collections.nCopies(cantidad, SEGUIMIENTO)).consultarSeguimientos(" 12345678Z ");

        assertTrue(resultado.correcto());
        assertEquals(mensajeEsperado, resultado.mensaje());
        assertEquals(cantidad, resultado.seguimientos().size());
    }

    // CP-C02 · DNI rechazado por el modelo (A1)
    @Test
    @DisplayName("Un DNI no válido devuelve error con el mensaje del modelo")
    void dniNoValidoDevuelveError() {
        ResultadoConsultaSeguimientos resultado = new ControladorConsultaSeguimientos(dni -> {
            throw new DniPacienteInvalidoException("El DNI del paciente es obligatorio.");
        }).consultarSeguimientos("");

        assertFalse(resultado.correcto());
        assertEquals("El DNI del paciente es obligatorio.", resultado.mensaje());
        assertTrue(resultado.seguimientos().isEmpty());
    }

    // CP-C03 · Conjetura de errores: fallo interno de persistencia
    @Test
    @DisplayName("Un fallo interno no muestra detalles técnicos al cliente")
    void falloInternoNoMuestraDetalles() {
        ResultadoConsultaSeguimientos resultado = new ControladorConsultaSeguimientos(dni -> {
            throw new IllegalStateException("Conexión MySQL perdida");
        }).consultarSeguimientos("12345678Z");

        assertFalse(resultado.correcto());
        assertEquals(ControladorConsultaSeguimientos.ERROR_INTERNO, resultado.mensaje());
    }

    // CP-C04 · Conjetura de errores: dependencia obligatoria
    @Test
    @DisplayName("El servicio es obligatorio")
    void elServicioEsObligatorio() {
        assertThrows(NullPointerException.class, () -> new ControladorConsultaSeguimientos(null));
    }
}
