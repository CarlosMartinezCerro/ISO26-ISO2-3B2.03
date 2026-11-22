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
import java.util.List;
import org.junit.jupiter.api.Test;

/** Prueba cómo el controlador del CdU11 convierte la respuesta del servicio en un resultado. */
class ControladorConsultaSeguimientosTest {

    private static final SeguimientoPeriodico SEGUIMIENTO = new SeguimientoPeriodico("SEG-1",
            "12345678Z", "00000001R", "TAC", "Indicación ficticia", PrioridadSolicitud.URGENTE,
            "Cada 2 semanas", LocalDate.of(2026, 11, 20), LocalDate.of(2026, 12, 4), 2, 0,
            LocalDate.of(2026, 11, 20), SeguimientoPeriodico.EN_CURSO, List.of());

    @Test
    void conSeguimientosDevuelveCorrectoYLosCuenta() {
        ResultadoConsultaSeguimientos resultado = new ControladorConsultaSeguimientos(
                dni -> List.of(SEGUIMIENTO, SEGUIMIENTO)).consultarSeguimientos("12345678Z");

        assertTrue(resultado.correcto());
        assertEquals("Se han encontrado 2 pruebas de seguimiento periódicas.", resultado.mensaje());
        assertEquals(List.of(SEGUIMIENTO, SEGUIMIENTO), resultado.seguimientos());
    }

    @Test
    void conUnSeguimientoUsaElSingular() {
        ResultadoConsultaSeguimientos resultado = new ControladorConsultaSeguimientos(
                dni -> List.of(SEGUIMIENTO)).consultarSeguimientos("12345678Z");

        assertEquals("Se ha encontrado 1 prueba de seguimiento periódica.", resultado.mensaje());
    }

    @Test
    void sinSeguimientosEsCorrectoConMensajeInformativo() {
        ResultadoConsultaSeguimientos resultado = new ControladorConsultaSeguimientos(
                dni -> List.of()).consultarSeguimientos(" 12345678Z ");

        assertTrue(resultado.correcto());
        assertEquals("El paciente 12345678Z no tiene pruebas de seguimiento periódicas.",
                resultado.mensaje());
        assertTrue(resultado.seguimientos().isEmpty());
    }

    @Test
    void dniInvalidoDevuelveErrorConElMensajeDelModelo() {
        ResultadoConsultaSeguimientos resultado = new ControladorConsultaSeguimientos(dni -> {
            throw new DniPacienteInvalidoException("El DNI del paciente es obligatorio.");
        }).consultarSeguimientos("");

        assertFalse(resultado.correcto());
        assertEquals("El DNI del paciente es obligatorio.", resultado.mensaje());
        assertTrue(resultado.seguimientos().isEmpty());
    }

    @Test
    void falloInternoNoMuestraDetallesAlCliente() {
        ResultadoConsultaSeguimientos resultado = new ControladorConsultaSeguimientos(dni -> {
            throw new IllegalStateException("Conexión MySQL perdida");
        }).consultarSeguimientos("12345678Z");

        assertFalse(resultado.correcto());
        assertEquals(ControladorConsultaSeguimientos.ERROR_INTERNO, resultado.mensaje());
    }

    @Test
    void elServicioEsObligatorio() {
        assertThrows(NullPointerException.class, () -> new ControladorConsultaSeguimientos(null));
    }
}
