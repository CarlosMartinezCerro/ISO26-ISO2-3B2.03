
package es.uclm.esi.iso2.radiodiagnostico.modelo.cita;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CitaRepositorioEnMemoriaTest {

    private CitaRepositorioEnMemoria repositorio;

    @BeforeEach
    void setUp() {
        repositorio = new CitaRepositorioEnMemoria();
    }

    @Test
    void buscarPorDniPacienteDevuelveSoloSusCitas() {
        Cita cita1 = new Cita("CITA-1", "SOL-1", "12345678A",
                "Radiografía", LocalDateTime.of(2026, 12, 1, 10, 0),
                "Sala 1", EstadoCita.ASIGNADA);

        Cita cita2 = new Cita("CITA-2", "SOL-2", "87654321B",
                "Resonancia", LocalDateTime.of(2026, 12, 2, 11, 0),
                "Sala 2", EstadoCita.ASIGNADA);

        Cita cita3 = new Cita("CITA-3", "SOL-3", "12345678A",
                "Ecografía", LocalDateTime.of(2026, 12, 3, 12, 0),
                "Sala 3", EstadoCita.ASIGNADA);

        repositorio.guardar(cita1);
        repositorio.guardar(cita2);
        repositorio.guardar(cita3);

        List<Cita> resultado = repositorio.buscarPorDniPaciente("12345678A");

        assertEquals(2, resultado.size());
        assertTrue(resultado.contains(cita1));
        assertTrue(resultado.contains(cita3));
        assertTrue(!resultado.contains(cita2));
    }

    @Test
    void buscarPorDniPacienteSinCitasDevuelveListaVacia() {
        List<Cita> resultado = repositorio.buscarPorDniPaciente("11111111Z");

        assertTrue(resultado.isEmpty());
    }
}

