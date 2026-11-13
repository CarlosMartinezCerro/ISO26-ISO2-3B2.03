package es.uclm.esi.iso2.radiodiagnostico.paciente;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ServicioPacientesImplTest {

    private ServicioPacientes servicio;

    @BeforeEach
    void crearServicio() {
        servicio = new ServicioPacientesImpl(new PacienteRepositorioEnMemoria());
    }

    @Test
    void daDeAltaPacienteConDatosValidos() {
        DatosAltaPaciente datos = datosValidos();

        Paciente paciente = servicio.darDeAlta(datos);

        assertEquals(new Paciente("FICTICIO-001", "Alex", "Ejemplo",
                LocalDate.of(1990, 5, 12), "600000001"), paciente);
    }

    @Test
    void rechazaAltaConDatosNulos() {
        assertThrows(DatosInvalidosException.class, () -> servicio.darDeAlta(null));
    }

    @Test
    void rechazaDniVacioConMensajeClaro() {
        assertDatoInvalido(new DatosAltaPaciente("", "Alex", "Ejemplo",
                LocalDate.of(1990, 5, 12), "600000001"), "dni");
    }

    @Test
    void rechazaDniNuloConMensajeClaro() {
        assertDatoInvalido(new DatosAltaPaciente(null, "Alex", "Ejemplo",
                LocalDate.of(1990, 5, 12), "600000001"), "dni");
    }

    @Test
    void rechazaNombreVacioConMensajeClaro() {
        assertDatoInvalido(new DatosAltaPaciente("FICTICIO-001", " ", "Ejemplo",
                LocalDate.of(1990, 5, 12), "600000001"), "nombre");
    }

    @Test
    void rechazaNombreNuloConMensajeClaro() {
        assertDatoInvalido(new DatosAltaPaciente("FICTICIO-001", null, "Ejemplo",
                LocalDate.of(1990, 5, 12), "600000001"), "nombre");
    }

    @Test
    void rechazaApellidosVaciosConMensajeClaro() {
        assertDatoInvalido(new DatosAltaPaciente("FICTICIO-001", "Alex", "",
                LocalDate.of(1990, 5, 12), "600000001"), "apellidos");
    }

    @Test
    void rechazaApellidosNulosConMensajeClaro() {
        assertDatoInvalido(new DatosAltaPaciente("FICTICIO-001", "Alex", null,
                LocalDate.of(1990, 5, 12), "600000001"), "apellidos");
    }

    @Test
    void rechazaFechaNacimientoNulaConMensajeClaro() {
        assertDatoInvalido(new DatosAltaPaciente("FICTICIO-001", "Alex", "Ejemplo",
                null, "600000001"), "fechaNacimiento");
    }

    @Test
    void rechazaAltaConDniDuplicado() {
        servicio.darDeAlta(datosValidos());

        PacienteDuplicadoException excepcion = assertThrows(PacienteDuplicadoException.class,
                () -> servicio.darDeAlta(datosValidos()));

        assertTrue(excepcion.getMessage().contains("FICTICIO-001"));
    }

    @Test
    void altaDuplicadaNoModificaElPacienteOriginal() {
        Paciente original = servicio.darDeAlta(datosValidos());
        DatosAltaPaciente duplicado = new DatosAltaPaciente("FICTICIO-001", "Sam",
                "Prueba", LocalDate.of(1992, 8, 24), "600000002");

        assertThrows(PacienteDuplicadoException.class, () -> servicio.darDeAlta(duplicado));

        assertEquals(Optional.of(original), servicio.buscarPorDni("FICTICIO-001"));
    }

    @Test
    void altaConDatosInvalidosNoGuardaElPaciente() {
        DatosAltaPaciente datosInvalidos = new DatosAltaPaciente("FICTICIO-002", "",
                "Ejemplo", LocalDate.of(1990, 5, 12), "600000001");

        assertThrows(DatosInvalidosException.class, () -> servicio.darDeAlta(datosInvalidos));

        assertEquals(Optional.empty(), servicio.buscarPorDni("FICTICIO-002"));
    }

    @Test
    void permiteRecuperarPacientePorDniTrasElAlta() {
        Paciente paciente = servicio.darDeAlta(datosValidos());

        assertEquals(Optional.of(paciente), servicio.buscarPorDni("FICTICIO-001"));
    }

    private void assertDatoInvalido(DatosAltaPaciente datos, String campo) {
        DatosInvalidosException excepcion = assertThrows(DatosInvalidosException.class,
                () -> servicio.darDeAlta(datos));

        assertTrue(excepcion.getMessage().contains(campo),
                "El mensaje debe identificar el campo inválido: " + campo);
    }

    private DatosAltaPaciente datosValidos() {
        return new DatosAltaPaciente("FICTICIO-001", "Alex", "Ejemplo",
                LocalDate.of(1990, 5, 12), "600000001");
    }
}
