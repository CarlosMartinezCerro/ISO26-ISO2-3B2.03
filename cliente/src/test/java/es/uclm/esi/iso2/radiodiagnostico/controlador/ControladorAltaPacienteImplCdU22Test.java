package es.uclm.esi.iso2.radiodiagnostico.controlador;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import es.uclm.esi.iso2.radiodiagnostico.paciente.ClientePacientes;
import es.uclm.esi.iso2.radiodiagnostico.paciente.DatosAltaPaciente;
import es.uclm.esi.iso2.radiodiagnostico.paciente.ErrorComunicacionException;
import es.uclm.esi.iso2.radiodiagnostico.paciente.ResultadoAlta;
import es.uclm.esi.iso2.radiodiagnostico.vista.VistaAltaPaciente;
import java.io.IOException;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * CdU22, unidades CA y CC, con vista y cliente falsos. Diseño: docs/testing/CdU22-diseno-pruebas.md.
 * Con dos variables, pairwise coincide con el producto cartesiano y contiene a each use (6.4).
 */
@DisplayName("CdU22 · ControladorAltaPacienteImpl")
class ControladorAltaPacienteImplCdU22Test {

    private static final DatosAltaPaciente V1 = new DatosAltaPaciente("12345678Z", "Alex",
            "Ejemplo", LocalDate.of(1990, 5, 12), "600000001");

    private final VistaFalsa vista = new VistaFalsa();
    private final ClienteFalso cliente = new ClienteFalso();
    private final ControladorAltaPaciente controlador = new ControladorAltaPacienteImpl(vista, cliente);

    @Nested
    @DisplayName("CA y CC · Each use")
    class EachUse {

        @Test
        @DisplayName("CA-PW-04 · Posible fallo PF-5: respuesta nula del cliente")
        void posibleFalloPf5EachUse() {
            vista.datos = V1;
            cliente.codigo = "C4";

            assertDoesNotThrow(controlador::darDeAlta);

            assertNotNull(vista.error, "Debe mostrarse un error");
            assertNull(vista.exito);
            assertFalse(vista.limpiada);
        }

        @Test
        @DisplayName("CA-PW-10: fecha mal escrita (cliente C5)")
        void fechaMalEscritaEachUse() {
            comprobarFechaMalEscrita("C5");
        }

        @Test
        @DisplayName("CC-PW-02: la vista es obligatoria")
        void vistaNulaEachUse() {
            assertThrows(NullPointerException.class,
                    () -> new ControladorAltaPacienteImpl(null, cliente));
        }

        @Test
        @DisplayName("CC-PW-03: el cliente es obligatorio")
        void clienteNuloEachUse() {
            assertThrows(NullPointerException.class,
                    () -> new ControladorAltaPacienteImpl(vista, null));
        }
    }

    @Nested
    @DisplayName("CA y CC · Pairwise")
    class Pairwise {

        @Test
        @DisplayName("CA-PW-05: fallo no previsto del cliente")
        void falloNoPrevistoPairwise() {
            vista.datos = V1;
            cliente.codigo = "C5";

            assertThrows(IllegalArgumentException.class, controlador::darDeAlta);

            assertNull(vista.exito);
            assertFalse(vista.limpiada);
        }

        @ParameterizedTest(name = "{0}")
        @DisplayName("CA: fecha mal escrita")
        @CsvSource({"CA-PW-06, C1", "CA-PW-07, C2", "CA-PW-08, C3"})
        void fechaMalEscritaPairwise(String id, String codigoCliente) {
            comprobarFechaMalEscrita(codigoCliente);
        }

        @Test
        @DisplayName("CC-PW-04: ambos son obligatorios")
        void ambosNulosPairwise() {
            assertThrows(NullPointerException.class,
                    () -> new ControladorAltaPacienteImpl(null, null));
        }
    }

    private void comprobarFechaMalEscrita(String codigoCliente) {
        vista.datos = null;
        cliente.codigo = codigoCliente;

        controlador.darDeAlta();

        assertEquals(ControladorAltaPacienteImpl.MENSAJE_FECHA_INCORRECTA, vista.error);
        assertNull(vista.exito);
        assertFalse(vista.limpiada);
        assertEquals(0, cliente.llamadas, "No debe llamar al cliente");
    }

    private static final class VistaFalsa implements VistaAltaPaciente {
        DatosAltaPaciente datos;
        String exito;
        String error;
        boolean limpiada;

        @Override
        public DatosAltaPaciente obtenerDatos() {
            return datos;
        }

        @Override
        public void mostrarExito(String mensaje) {
            exito = mensaje;
        }

        @Override
        public void mostrarError(String mensaje) {
            error = mensaje;
        }

        @Override
        public void limpiar() {
            limpiada = true;
        }
    }

    private static final class ClienteFalso implements ClientePacientes {
        String codigo;
        int llamadas;

        @Override
        public ResultadoAlta darDeAlta(DatosAltaPaciente datos) {
            llamadas++;
            return switch (codigo) {
                case "C1" -> new ResultadoAlta(true, "Paciente dado de alta.");
                case "C2" -> new ResultadoAlta(false, "Ya existe un paciente con el DNI 12345678Z.");
                case "C3" -> throw new ErrorComunicacionException("Servidor no disponible",
                        new IOException("Conexión rechazada"));
                case "C4" -> null;
                case "C5" -> throw new IllegalArgumentException("fallo no previsto");
                default -> throw new IllegalStateException(codigo);
            };
        }
    }
}
