package es.uclm.esi.iso2.radiodiagnostico.controlador;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.uclm.esi.iso2.radiodiagnostico.paciente.DatosAltaPaciente;
import es.uclm.esi.iso2.radiodiagnostico.paciente.Paciente;
import es.uclm.esi.iso2.radiodiagnostico.paciente.PacienteRepositorio;
import es.uclm.esi.iso2.radiodiagnostico.paciente.PacienteRepositorioEnMemoria;
import es.uclm.esi.iso2.radiodiagnostico.paciente.ResultadoAlta;
import es.uclm.esi.iso2.radiodiagnostico.paciente.ServicioPacientesImpl;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ControladorPacientesTest {

    private static final DatosAltaPaciente DATOS = new DatosAltaPaciente("12345678Z", "Alex",
            "Ejemplo", LocalDate.of(1990, 5, 12), "600000001");

    private ControladorPacientes controlador;

    @BeforeEach
    void crearControlador() {
        controlador = new ControladorPacientes(
                new ServicioPacientesImpl(new PacienteRepositorioEnMemoria()));
    }

    @Test
    void altaCorrectaDevuelveResultadoCorrecto() {
        ResultadoAlta resultado = controlador.darDeAlta(DATOS);

        assertTrue(resultado.correcto());
        assertTrue(resultado.mensaje().contains("12345678Z"));
    }

    @Test
    void datosInvalidosDevuelvenErrorConMensaje() {
        ResultadoAlta resultado = controlador.darDeAlta(new DatosAltaPaciente("12345678Z", "",
                "Ejemplo", LocalDate.of(1990, 5, 12), null));

        assertEquals(new ResultadoAlta(false, "El campo nombre es obligatorio."), resultado);
    }

    @Test
    void datosNulosDevuelvenError() {
        assertFalse(controlador.darDeAlta(null).correcto());
    }

    @Test
    void dniDuplicadoDevuelveError() {
        controlador.darDeAlta(DATOS);

        ResultadoAlta resultado = controlador.darDeAlta(DATOS);

        assertFalse(resultado.correcto());
        assertTrue(resultado.mensaje().contains("12345678Z"));
    }

    @Test
    void falloDePersistenciaDevuelveErrorGenerico() {
        PacienteRepositorio repositorioCaido = new PacienteRepositorio() {
            @Override
            public void guardar(Paciente paciente) {
                throw new IllegalStateException("No se pudo guardar el paciente.");
            }

            @Override
            public Optional<Paciente> buscarPorDni(String dni) {
                return Optional.empty();
            }
        };
        controlador = new ControladorPacientes(new ServicioPacientesImpl(repositorioCaido));

        ResultadoAlta resultado = controlador.darDeAlta(DATOS);

        assertEquals(new ResultadoAlta(false, ControladorPacientes.ERROR_INTERNO), resultado);
    }
}
