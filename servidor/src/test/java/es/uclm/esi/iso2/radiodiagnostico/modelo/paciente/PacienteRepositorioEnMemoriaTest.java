package es.uclm.esi.iso2.radiodiagnostico.modelo.paciente;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class PacienteRepositorioEnMemoriaTest {

    private final PacienteRepositorio repositorio = new PacienteRepositorioEnMemoria();

    @Test
    void devuelveElPacienteGuardado() {
        Paciente paciente = new Paciente("12345678Z", "Alex", "Ejemplo",
                LocalDate.of(1990, 5, 12), null);

        repositorio.guardar(paciente);

        assertEquals(Optional.of(paciente), repositorio.buscarPorDni("12345678Z"));
    }

    @Test
    void dniDesconocidoDevuelveVacio() {
        assertEquals(Optional.empty(), repositorio.buscarPorDni("00000000T"));
    }
}
