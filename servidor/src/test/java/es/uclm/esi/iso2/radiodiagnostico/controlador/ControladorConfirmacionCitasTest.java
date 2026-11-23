package es.uclm.esi.iso2.radiodiagnostico.controlador;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.uclm.esi.iso2.radiodiagnostico.modelo.cita.Cita;
import es.uclm.esi.iso2.radiodiagnostico.modelo.cita.CitaRepositorioEnMemoria;
import es.uclm.esi.iso2.radiodiagnostico.modelo.cita.EstadoCita;
import es.uclm.esi.iso2.radiodiagnostico.modelo.cita.ResultadoConfirmacion;
import es.uclm.esi.iso2.radiodiagnostico.modelo.cita.ServicioConfirmacionCitasImpl;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ControladorConfirmacionCitasTest {

    private ControladorConfirmacionCitas controlador;

    @BeforeEach
    void crearControlador() {
        CitaRepositorioEnMemoria citas = new CitaRepositorioEnMemoria();
        citas.guardar(new Cita("CITA-SOL-001", "SOL-001", "FICTICIO-001", "Radiografía",
                LocalDateTime.of(2026, 11, 18, 9, 30), "SALA-RX-1", EstadoCita.ASIGNADA));
        controlador = new ControladorConfirmacionCitas(new ServicioConfirmacionCitasImpl(citas));
    }

    @Test
    void confirmacionCorrectaDevuelveResultadoCorrecto() {
        ResultadoConfirmacion resultado = controlador.confirmarCita("CITA-SOL-001", "FICTICIO-001");

        assertTrue(resultado.correcto());
        assertTrue(resultado.mensaje().contains("CITA-SOL-001"));
    }

    @Test
    void citaYaConfirmadaDevuelveError() {
        controlador.confirmarCita("CITA-SOL-001", "FICTICIO-001");

        ResultadoConfirmacion resultado = controlador.confirmarCita("CITA-SOL-001", "FICTICIO-001");

        assertFalse(resultado.correcto());
    }

    @Test
    void datosInvalidosDevuelvenErrorConMensaje() {
        ResultadoConfirmacion resultado = controlador.confirmarCita("", "FICTICIO-001");

        assertEquals(new ResultadoConfirmacion(false, "El campo cita es obligatorio."), resultado);
    }
}
