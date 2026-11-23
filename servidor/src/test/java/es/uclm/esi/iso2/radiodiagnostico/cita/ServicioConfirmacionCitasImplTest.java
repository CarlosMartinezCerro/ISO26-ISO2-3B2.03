package es.uclm.esi.iso2.radiodiagnostico.cita;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ServicioConfirmacionCitasImplTest {

    private static final LocalDateTime MANANA = LocalDateTime.of(2026, 11, 18, 9, 30);

    private CitaRepositorioEnMemoria citas;
    private ServicioConfirmacionCitasImpl servicio;

    @BeforeEach
    void crearServicio() {
        citas = new CitaRepositorioEnMemoria();
        citas.guardar(new Cita("CITA-SOL-001", "SOL-001", "FICTICIO-001", "Radiografía",
                MANANA, "SALA-RX-1", EstadoCita.ASIGNADA));
        servicio = new ServicioConfirmacionCitasImpl(citas);
    }

    @Test
    void confirmaLaCitaYLaGuardaComoConfirmada() {
        Cita confirmada = servicio.confirmarCita("CITA-SOL-001", "FICTICIO-001");

        assertEquals(EstadoCita.CONFIRMADA, confirmada.estado());
        assertEquals(EstadoCita.CONFIRMADA, citas.buscarPorId("CITA-SOL-001").orElseThrow().estado());
    }

    @Test
    void noSePuedeConfirmarDosVeces() {
        servicio.confirmarCita("CITA-SOL-001", "FICTICIO-001");

        assertThrows(CitaNoConfirmableException.class,
                () -> servicio.confirmarCita("CITA-SOL-001", "FICTICIO-001"));
    }

    @Test
    void citaInexistenteLanzaNoEncontrada() {
        assertThrows(CitaNoEncontradaException.class,
                () -> servicio.confirmarCita("CITA-SOL-999", "FICTICIO-001"));
    }

    @Test
    void citaDeOtroPacienteSeTrataComoNoEncontrada() {
        assertThrows(CitaNoEncontradaException.class,
                () -> servicio.confirmarCita("CITA-SOL-001", "FICTICIO-002"));
        assertEquals(EstadoCita.ASIGNADA, citas.buscarPorId("CITA-SOL-001").orElseThrow().estado());
    }

    @Test
    void sinDniLanzaDatosInvalidos() {
        DatosConfirmacionInvalidosException error = assertThrows(
                DatosConfirmacionInvalidosException.class,
                () -> servicio.confirmarCita("CITA-SOL-001", " "));

        assertEquals("El campo DNI del paciente es obligatorio.", error.getMessage());
    }
}
