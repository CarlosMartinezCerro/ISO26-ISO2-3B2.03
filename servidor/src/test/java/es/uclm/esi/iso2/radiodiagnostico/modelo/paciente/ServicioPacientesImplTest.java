package es.uclm.esi.iso2.radiodiagnostico.modelo.paciente;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ServicioPacientesImplTest {

    private static final ZoneId ZONA = ZoneId.of("Europe/Madrid");
    private static final LocalDate HOY = LocalDate.of(2026, 11, 18);
    private static final String DNI = "12345678Z";
    private static final LocalDate NACIMIENTO = LocalDate.of(1990, 5, 12);

    private ServicioPacientes servicio;

    @BeforeEach
    void crearServicio() {
        Clock reloj = Clock.fixed(HOY.atStartOfDay(ZONA).toInstant(), ZONA);
        servicio = new ServicioPacientesImpl(new PacienteRepositorioEnMemoria(), reloj);
    }

    @Test
    void daDeAltaPacienteConDatosValidos() {
        Paciente paciente = servicio.darDeAlta(datosValidos());

        assertEquals(new Paciente(DNI, "Alex", "Ejemplo", NACIMIENTO, "600000001"), paciente);
    }

    @Test
    void normalizaDniEnMinusculasYConEspacios() {
        Paciente paciente = servicio.darDeAlta(new DatosAltaPaciente(" 12345678z ", "Alex",
                "Ejemplo", NACIMIENTO, "600000001"));

        assertEquals(DNI, paciente.dni());
        assertEquals(Optional.of(paciente), servicio.buscarPorDni("12345678z"));
    }

    @Test
    void permiteTelefonoVacioYLoGuardaComoNulo() {
        Paciente paciente = servicio.darDeAlta(new DatosAltaPaciente(DNI, "Alex", "Ejemplo",
                NACIMIENTO, " "));

        assertNull(paciente.telefono());
    }

    @Test
    void permiteNacidoHoy() {
        Paciente paciente = servicio.darDeAlta(new DatosAltaPaciente(DNI, "Alex", "Ejemplo",
                HOY, null));

        assertEquals(HOY, paciente.fechaNacimiento());
    }

    @Test
    void rechazaAltaConDatosNulos() {
        assertThrows(DatosInvalidosException.class, () -> servicio.darDeAlta(null));
    }

    @Test
    void rechazaDniVacioConMensajeClaro() {
        assertDatoInvalido(new DatosAltaPaciente("", "Alex", "Ejemplo", NACIMIENTO, null), "dni");
    }

    @Test
    void rechazaDniNuloConMensajeClaro() {
        assertDatoInvalido(new DatosAltaPaciente(null, "Alex", "Ejemplo", NACIMIENTO, null), "dni");
    }

    @Test
    void rechazaDniConFormatoIncorrecto() {
        assertDatoInvalido(new DatosAltaPaciente("1234567Z", "Alex", "Ejemplo", NACIMIENTO, null),
                "dni");
        assertDatoInvalido(new DatosAltaPaciente("123456789", "Alex", "Ejemplo", NACIMIENTO, null),
                "dni");
    }

    @Test
    void rechazaDniConLetraIncorrecta() {
        DatosInvalidosException excepcion = assertThrows(DatosInvalidosException.class,
                () -> servicio.darDeAlta(new DatosAltaPaciente("12345678A", "Alex", "Ejemplo",
                        NACIMIENTO, null)));

        assertEquals("La letra del campo dni no es correcta.", excepcion.getMessage());
    }

    @Test
    void rechazaNombreVacioConMensajeClaro() {
        assertDatoInvalido(new DatosAltaPaciente(DNI, " ", "Ejemplo", NACIMIENTO, null), "nombre");
    }

    @Test
    void rechazaNombreNuloConMensajeClaro() {
        assertDatoInvalido(new DatosAltaPaciente(DNI, null, "Ejemplo", NACIMIENTO, null), "nombre");
    }

    @Test
    void rechazaApellidosVaciosConMensajeClaro() {
        assertDatoInvalido(new DatosAltaPaciente(DNI, "Alex", "", NACIMIENTO, null), "apellidos");
    }

    @Test
    void rechazaApellidosNulosConMensajeClaro() {
        assertDatoInvalido(new DatosAltaPaciente(DNI, "Alex", null, NACIMIENTO, null), "apellidos");
    }

    @Test
    void rechazaFechaNacimientoNulaConMensajeClaro() {
        assertDatoInvalido(new DatosAltaPaciente(DNI, "Alex", "Ejemplo", null, null),
                "fechaNacimiento");
    }

    @Test
    void rechazaFechaNacimientoFutura() {
        assertDatoInvalido(new DatosAltaPaciente(DNI, "Alex", "Ejemplo", HOY.plusDays(1), null),
                "fechaNacimiento");
    }

    @Test
    void rechazaTelefonoConFormatoIncorrecto() {
        assertDatoInvalido(new DatosAltaPaciente(DNI, "Alex", "Ejemplo", NACIMIENTO, "60000"),
                "telefono");
        assertDatoInvalido(new DatosAltaPaciente(DNI, "Alex", "Ejemplo", NACIMIENTO, "60000000A"),
                "telefono");
    }

    @Test
    void rechazaAltaConDniDuplicado() {
        servicio.darDeAlta(datosValidos());

        PacienteDuplicadoException excepcion = assertThrows(PacienteDuplicadoException.class,
                () -> servicio.darDeAlta(datosValidos()));

        assertTrue(excepcion.getMessage().contains(DNI));
    }

    @Test
    void altaDuplicadaNoModificaElPacienteOriginal() {
        Paciente original = servicio.darDeAlta(datosValidos());
        DatosAltaPaciente duplicado = new DatosAltaPaciente(DNI, "Sam", "Prueba",
                LocalDate.of(1992, 8, 24), "600000002");

        assertThrows(PacienteDuplicadoException.class, () -> servicio.darDeAlta(duplicado));

        assertEquals(Optional.of(original), servicio.buscarPorDni(DNI));
    }

    @Test
    void altaConDatosInvalidosNoGuardaElPaciente() {
        DatosAltaPaciente datosInvalidos = new DatosAltaPaciente("00000000T", "", "Ejemplo",
                NACIMIENTO, "600000001");

        assertThrows(DatosInvalidosException.class, () -> servicio.darDeAlta(datosInvalidos));

        assertEquals(Optional.empty(), servicio.buscarPorDni("00000000T"));
    }

    @Test
    void permiteRecuperarPacientePorDniTrasElAlta() {
        Paciente paciente = servicio.darDeAlta(datosValidos());

        assertEquals(Optional.of(paciente), servicio.buscarPorDni(DNI));
    }

    @Test
    void buscarConDniNuloDevuelveVacio() {
        assertEquals(Optional.empty(), servicio.buscarPorDni(null));
    }

    private void assertDatoInvalido(DatosAltaPaciente datos, String campo) {
        DatosInvalidosException excepcion = assertThrows(DatosInvalidosException.class,
                () -> servicio.darDeAlta(datos));

        assertTrue(excepcion.getMessage().contains(campo),
                "El mensaje debe identificar el campo inválido: " + campo);
    }

    private DatosAltaPaciente datosValidos() {
        return new DatosAltaPaciente(DNI, "Alex", "Ejemplo", NACIMIENTO, "600000001");
    }
}
