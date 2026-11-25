package es.uclm.esi.iso2.radiodiagnostico.controlador;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import es.uclm.esi.iso2.radiodiagnostico.paciente.DatosAltaPaciente;
import es.uclm.esi.iso2.radiodiagnostico.paciente.DatosInvalidosException;
import es.uclm.esi.iso2.radiodiagnostico.paciente.Paciente;
import es.uclm.esi.iso2.radiodiagnostico.paciente.PacienteDuplicadoException;
import es.uclm.esi.iso2.radiodiagnostico.paciente.ResultadoAlta;
import es.uclm.esi.iso2.radiodiagnostico.paciente.ServicioPacientes;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * CdU22, unidades CP y PC, con un servicio falso. Diseño: docs/testing/CdU22-diseno-pruebas.md.
 * Con dos variables, pairwise coincide con el producto cartesiano y contiene a each use (6.4).
 */
@DisplayName("CdU22 · ControladorPacientes")
class ControladorPacientesCdU22Test {

    private static final LocalDate NACIMIENTO = LocalDate.of(1990, 5, 12);
    private static final DatosAltaPaciente J1 = new DatosAltaPaciente("12345678Z", "Alex",
            "Ejemplo", NACIMIENTO, "600000001");
    private static final String MENSAJE_INVALIDO = "El campo nombre es obligatorio.";
    private static final String MENSAJE_DUPLICADO = "Ya existe un paciente con el DNI 12345678Z.";

    @Nested
    @DisplayName("CP y PC · Each use")
    class EachUse {

        @ParameterizedTest(name = "{0}")
        @DisplayName("CP: darDeAlta")
        @CsvSource({"CP-PW-01, J1, S1", "CP-PW-03, J1, S3", "CP-PW-10, J2, S5"})
        void darDeAltaEachUse(String id, String datos, String servicio) {
            comprobar(datos, servicio);
        }

        @Test
        @DisplayName("PC-EU-02: el servicio es obligatorio")
        void servicioNuloEachUse() {
            assertThrows(NullPointerException.class, () -> new ControladorPacientes(null));
        }
    }

    @Nested
    @DisplayName("CP y PC · Pairwise")
    class Pairwise {

        @ParameterizedTest(name = "{0}")
        @DisplayName("CP: darDeAlta")
        @CsvSource({"CP-PW-05, J1, S5", "CP-PW-06, J2, S1", "CP-PW-07, J2, S2",
                "CP-PW-08, J2, S3", "CP-PW-09, J2, S4"})
        void darDeAltaPairwise(String id, String datos, String servicio) {
            comprobar(datos, servicio);
        }
    }

    private static void comprobar(String codigoDatos, String codigoServicio) {
        DatosAltaPaciente datos = codigoDatos.equals("J1") ? J1 : null;
        ServicioFalso servicio = new ServicioFalso(codigoServicio);
        ControladorPacientes controlador = new ControladorPacientes(servicio);

        switch (codigoServicio) {
            case "S1" -> assertEquals(new ResultadoAlta(true,
                    "Paciente Alex Ejemplo (DNI 12345678Z) dado de alta."), controlador.darDeAlta(datos));
            case "S2" -> assertEquals(new ResultadoAlta(false, MENSAJE_INVALIDO),
                    controlador.darDeAlta(datos));
            case "S3" -> assertEquals(new ResultadoAlta(false, MENSAJE_DUPLICADO),
                    controlador.darDeAlta(datos));
            case "S4" -> assertEquals(new ResultadoAlta(false, ControladorPacientes.ERROR_INTERNO),
                    controlador.darDeAlta(datos));
            case "S5" -> assertThrows(NullPointerException.class, () -> controlador.darDeAlta(datos));
            default -> throw new IllegalArgumentException(codigoServicio);
        }
        assertEquals(Collections.singletonList(datos), servicio.recibidos,
                "El servicio debe recibir los datos una vez y sin cambios");
    }

    private static final class ServicioFalso implements ServicioPacientes {
        final List<DatosAltaPaciente> recibidos = new ArrayList<>();
        private final String codigo;

        ServicioFalso(String codigo) {
            this.codigo = codigo;
        }

        @Override
        public Paciente darDeAlta(DatosAltaPaciente datos) {
            recibidos.add(datos);
            return switch (codigo) {
                case "S1" -> new Paciente("12345678Z", "Alex", "Ejemplo", NACIMIENTO, "600000001");
                case "S2" -> throw new DatosInvalidosException(MENSAJE_INVALIDO);
                case "S3" -> throw new PacienteDuplicadoException(MENSAJE_DUPLICADO);
                case "S4" -> throw new IllegalStateException("detalle interno");
                case "S5" -> throw new NullPointerException("fallo no previsto");
                default -> throw new IllegalArgumentException(codigo);
            };
        }

        @Override
        public Optional<Paciente> buscarPorDni(String dni) {
            throw new UnsupportedOperationException();
        }
    }
}
