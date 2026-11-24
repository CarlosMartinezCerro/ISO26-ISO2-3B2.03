package es.uclm.esi.iso2.radiodiagnostico.paciente;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.time.Clock;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * CdU22, unidades SP y SC. Diseño: docs/testing/CdU22-diseno-pruebas.md.
 * Solo están los casos que no cubre (o cubre en parte) {@link ServicioPacientesImplTest}.
 */
@DisplayName("CdU22 · ServicioPacientesImpl")
class ServicioPacientesImplCdU22Test {

    @Nested
    @DisplayName("SP y SC · Each use")
    class EachUse {

        @ParameterizedTest(name = "{0}")
        @DisplayName("SP-EU-02 a SP-EU-05: altas válidas")
        @CsvSource(delimiter = '|', value = {
                "SP-EU-02 | D2 N2 A2 F2 K1 T2 R1 | OK",
                "SP-EU-03 | D3 N3 A3 F3 K2 T3 R1 | OK",
                "SP-EU-04 | D4 N4 A4 F4 K3 T4 R1 | OK",
                "SP-EU-05 | D5 N5 A5 F2 K3 T5 R1 | OK"})
        void altasValidasEachUse(String id, String entradas, String esperado) {
            comprobar(id, entradas, esperado);
        }

        @ParameterizedTest(name = "{0}")
        @DisplayName("SP-EU-06 a SP-EU-32 y SP-EU-37: un solo dato inválido")
        @CsvSource(delimiter = '|', value = {
                "SP-EU-06 | D6 N1 A1 F1 K2 T2 R1 | DI(dni)",
                "SP-EU-07 | D7 N1 A1 F1 K2 T2 R1 | DI(dni)",
                "SP-EU-08 | D8 N1 A1 F1 K2 T2 R1 | DI(dni)",
                "SP-EU-09 = SP-DC-05 | D9 N1 A1 F1 K2 T2 R1 | DI(dni)",
                "SP-EU-10 | D10 N1 A1 F1 K2 T2 R1 | DI(dni)",
                "SP-EU-11 | D11 N1 A1 F1 K2 T2 R1 | DI(dni)",
                "SP-EU-12 | D12 N1 A1 F1 K2 T2 R1 | DI(dni)",
                "SP-EU-13 = SP-DC-06 | D13 N1 A1 F1 K2 T2 R1 | DI(dni)",
                "SP-EU-14 | D14 N1 A1 F1 K2 T2 R1 | DI(dni)",
                "SP-EU-16 = SP-DC-03 = SP-MC-01 | D1 N7 A1 F1 K2 T2 R1 | DI(nombre)",
                "SP-EU-17 | D3 N8 A1 F1 K2 T1 R1 | DI(nombre)",
                "SP-EU-18 = SP-MC-02 | D1 N9 A1 F1 K2 T2 R1 | DI(nombre)",
                "SP-EU-21 | D1 N1 A7 F1 K2 T2 R1 | DI(apellidos)",
                "SP-EU-22 | D1 N1 A8 F1 K2 T2 R1 | DI(apellidos)",
                "SP-EU-23 | D1 N1 A9 F1 K2 T2 R1 | DI(apellidos)",
                "SP-EU-25 = SP-DC-04 | D1 N1 A1 F5 K2 T2 R1 | DI(fechaNacimiento)",
                "SP-EU-27 = SP-DC-07 | D1 N1 A1 F4 K2 T2 R1 | DI(fechaNacimiento)",
                "SP-EU-28 | D1 N1 A1 F1 K2 T6 R1 | DI(telefono)",
                "SP-EU-29 | D1 N1 A1 F1 K2 T7 R1 | DI(telefono)",
                "SP-EU-30 = SP-DC-08 | D1 N1 A1 F1 K2 T8 R1 | DI(telefono)",
                "SP-EU-31 | D1 N1 A1 F1 K2 T9 R1 | DI(telefono)",
                "SP-EU-32 | D1 N1 A1 F1 K2 T10 R1 | DI(telefono)",
                "SP-EU-37 | D1 N1 A1 F3 K1 T2 R1 | DI(fechaNacimiento)"})
        void unDatoInvalidoEachUse(String id, String entradas, String esperado) {
            comprobar(id, entradas, esperado);
        }

        @ParameterizedTest(name = "{0}")
        @DisplayName("SP-EU-34 a SP-EU-36: fallos del repositorio")
        @CsvSource(delimiter = '|', value = {
                "SP-EU-34 | D1 N1 A1 F1 K2 T1 R3 | ISE",
                "SP-EU-35 | D1 N1 A1 F1 K2 T1 R4 | ISE",
                "SP-EU-36 | D1 N1 A1 F1 K2 T1 R5 | DUP"})
        void fallosDelRepositorioEachUse(String id, String entradas, String esperado) {
            comprobar(id, entradas, esperado);
        }

        @ParameterizedTest(name = "{0}")
        @DisplayName("Posible fallo PF-2: textos con solo caracteres de control")
        @CsvSource(delimiter = '|', value = {
                "SP-EU-19 | D1 N10 A1 F1 K2 T2 R1 | DI(nombre)",
                "SP-EU-24 | D1 N1 A10 F1 K2 T2 R1 | DI(apellidos)"})
        void posibleFalloPf2EachUse(String id, String entradas, String esperado) {
            comprobar(id, entradas, esperado);
        }

        @ParameterizedTest(name = "{0}")
        @DisplayName("Posible fallo PF-3: textos de más de 255 caracteres")
        @CsvSource(delimiter = '|', value = {
                "SP-EU-15 | D1 N6 A1 F1 K2 T2 R1 | DI(nombre)",
                "SP-EU-20 | D1 N1 A6 F1 K2 T2 R1 | DI(apellidos)"})
        void posibleFalloPf3EachUse(String id, String entradas, String esperado) {
            comprobar(id, entradas, esperado);
        }

        @Test
        @DisplayName("SP-EU-26 · Posible fallo PF-4: fecha de nacimiento del año 1")
        void posibleFalloPf4EachUse() {
            comprobar("SP-EU-26", "D1 N1 A1 F6 K2 T2 R1", "DI(fechaNacimiento)");
        }
    }

    @Nested
    @DisplayName("SP y SC · Pairwise")
    class Pairwise {

        @ParameterizedTest(name = "{0}")
        @DisplayName("SP-PW-001 a SP-PW-157")
        @CsvFileSource(resources = "/cdu22/sp-pairwise.csv", delimiter = '|')
        void paresPairwise(String id, String entradas, String esperado) {
            comprobar(id, entradas, esperado);
        }

        @ParameterizedTest(name = "{0}")
        @DisplayName("Pares cuyo único dato inválido es un posible fallo (PF-2, PF-3 y PF-4)")
        @CsvSource(delimiter = '|', value = {
                "SP-PW-012 (PF-4) | D2 N2 A1 F6 K2 T4 R3 | DI(fechaNacimiento)",
                "SP-PW-050 (PF-2) | D5 N10 A10 F3 K2 T1 R1 | DI(nombre, apellidos)",
                "SP-PW-156 (PF-2, PF-3 y PF-4) | D1 N10 A6 F6 K3 T5 R1"
                        + " | DI(nombre, apellidos, fechaNacimiento)"})
        void posiblesFallosPairwise(String id, String entradas, String esperado) {
            comprobar(id, entradas, esperado);
        }

        @Test
        @DisplayName("SC-PW-02: el repositorio es obligatorio")
        void repositorioNuloPairwise() {
            assertThrows(NullPointerException.class,
                    () -> new ServicioPacientesImpl(null, Clock.systemDefaultZone()));
        }

        @Test
        @DisplayName("SC-PW-03: el reloj es obligatorio")
        void relojNuloPairwise() {
            assertThrows(NullPointerException.class,
                    () -> new ServicioPacientesImpl(new PacienteRepositorioEnMemoria(), null));
        }

        @Test
        @DisplayName("SC-PW-04: ambos son obligatorios")
        void ambosNulosPairwise() {
            assertThrows(NullPointerException.class, () -> new ServicioPacientesImpl(null, null));
        }
    }

    /** El resto de casos de decisiones repite entradas de each use (apartado 8.1). */
    @Nested
    @DisplayName("SP y SC · Decisiones")
    class Decisiones {

        @Test
        @DisplayName("SP-DC-09: sin teléfono (d8) y con el DNI ya existente (d3)")
        void sinTelefonoYDuplicado() {
            comprobar("SP-DC-09", "D1 N1 A1 F1 K2 T2 R2", "DUP");
        }
    }

    /** El resto de casos MC/DC repite entradas de each use (apartado 9). */
    @Nested
    @DisplayName("SP y SC · MC/DC")
    class McDc {

        @Test
        @DisplayName("SP-MC-04: domina «teléfono null»")
        void telefonoNulo() {
            comprobar("SP-MC-04", "D1 N1 A1 F1 K2 T2 R1", "OK");
        }

        @Test
        @DisplayName("SP-MC-05: domina «teléfono en blanco»")
        void telefonoEnBlanco() {
            comprobar("SP-MC-05", "D1 N1 A1 F1 K2 T4 R1", "OK");
        }
    }

    /** Comprueba el resultado y la postcondición (PC1 si es OK, PC2 en el resto). */
    private static void comprobar(String id, String entradas, String esperado) {
        CasoAlta caso = new CasoAlta(entradas);
        Map<String, Paciente> antes = new HashMap<>(caso.repositorio.pacientes);
        ServicioPacientes servicio = new ServicioPacientesImpl(caso.repositorio, caso.reloj);

        if (esperado.equals("OK")) {
            Paciente paciente = servicio.darDeAlta(caso.datos);

            assertEquals(caso.pacienteEsperado(), paciente, id);
            antes.put(paciente.dni(), caso.pacienteEsperado());
        } else if (esperado.equals("DUP")) {
            assertThrows(PacienteDuplicadoException.class, () -> servicio.darDeAlta(caso.datos), id);
        } else if (esperado.equals("ISE")) {
            assertThrows(IllegalStateException.class, () -> servicio.darDeAlta(caso.datos), id);
        } else if (esperado.startsWith("DI(")) {
            DatosInvalidosException excepcion = assertThrows(DatosInvalidosException.class,
                    () -> servicio.darDeAlta(caso.datos), id);
            String[] campos = esperado.substring(3, esperado.length() - 1).split(", ");
            assertTrue(Arrays.stream(campos).anyMatch(excepcion.getMessage()::contains),
                    () -> id + ": el mensaje «" + excepcion.getMessage()
                            + "» debe nombrar alguno de los campos " + Arrays.toString(campos));
        } else {
            fail(id + ": resultado esperado desconocido: " + esperado);
        }
        assertEquals(antes, caso.repositorio.pacientes, id + ": postcondición del repositorio");
    }
}
