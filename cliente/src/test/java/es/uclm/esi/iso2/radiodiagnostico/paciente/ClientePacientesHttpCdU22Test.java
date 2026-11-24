package es.uclm.esi.iso2.radiodiagnostico.paciente;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * CdU22, unidades CH y CU, con un {@code HttpClient} falso. Diseño: docs/testing/CdU22-diseno-pruebas.md.
 * Con dos variables, pairwise coincide con el producto cartesiano y contiene a each use (6.4).
 */
@DisplayName("CdU22 · ClientePacientesHttp")
class ClientePacientesHttpCdU22Test {

    private static final String URL_BASE = "http://localhost:7070";
    private static final URI URL_PACIENTES = URI.create("http://localhost:7070/pacientes");
    private static final DatosAltaPaciente Y1 = new DatosAltaPaciente("12345678Z", "Alex",
            "Ejemplo", LocalDate.of(1990, 5, 12), "600000001");
    private static final DatosAltaPaciente Y3 = new DatosAltaPaciente("12345678Z", "Alex",
            "Ejemplo", null, null);
    private static final String JSON_Y1 = "{\"dni\":\"12345678Z\",\"nombre\":\"Alex\","
            + "\"apellidos\":\"Ejemplo\",\"fechaNacimiento\":\"1990-05-12\",\"telefono\":\"600000001\"}";
    private static final String JSON_Y3 = "{\"dni\":\"12345678Z\",\"nombre\":\"Alex\","
            + "\"apellidos\":\"Ejemplo\",\"fechaNacimiento\":null,\"telefono\":null}";

    private final HttpClientFalso http = new HttpClientFalso();

    /** Evita que la marca de interrupción de H8 llegue al test siguiente si este falla. */
    @AfterEach
    void limpiarInterrupcion() {
        Thread.interrupted();
    }

    @Nested
    @DisplayName("CH y CU · Each use")
    class EachUse {

        @ParameterizedTest(name = "{0}")
        @DisplayName("CH: darDeAlta")
        @CsvSource({"CH-PW-08, Y1, H8, EC", "CH-PW-12, Y2, H4, EC", "CH-PW-22, Y3, H6, EC"})
        void darDeAltaEachUse(String id, String datos, String respuesta, String esperado) {
            comprobar(datos, respuesta, esperado);
        }

        @Test
        @DisplayName("CH-PW-05 · Posible fallo PF-5: el servidor responde null")
        void posibleFalloPf5EachUse() {
            comprobar("Y1", "H5", "EC");
        }

        @ParameterizedTest(name = "{0}")
        @DisplayName("CU: URL válidas")
        @CsvSource({"CU-PW-01, http://localhost:7070", "CU-PW-03, http://localhost:7070///"})
        void urlValidaEachUse(String id, String url) {
            comprobarUrl(url);
        }

        @Test
        @DisplayName("CU-PW-04: la URL es obligatoria")
        void urlNulaEachUse() {
            assertThrows(NullPointerException.class, () -> new ClientePacientesHttp(null, http));
        }

        @Test
        @DisplayName("CU-PW-05: la URL no es una URI")
        void urlNoValidaEachUse() {
            assertThrows(IllegalArgumentException.class,
                    () -> new ClientePacientesHttp("http://servidor malo", http));
        }

        @Test
        @DisplayName("CU-PW-06: URL sin esquema")
        void urlSinEsquemaEachUse() {
            assertDoesNotThrow(() -> new ClientePacientesHttp("localhost:7070", http));
        }

        @Test
        @DisplayName("CU-PW-07: el cliente HTTP es obligatorio")
        void httpNuloEachUse() {
            assertThrows(NullPointerException.class, () -> new ClientePacientesHttp(URL_BASE, null));
        }
    }

    @Nested
    @DisplayName("CH y CU · Pairwise")
    class Pairwise {

        @ParameterizedTest(name = "{0}")
        @DisplayName("CH: darDeAlta")
        @CsvSource({
                "CH-PW-04, Y1, H4, EC", "CH-PW-06, Y1, H6, EC",
                "CH-PW-09, Y2, H1, ALTA", "CH-PW-10, Y2, H2, RECHAZO", "CH-PW-11, Y2, H3, EC",
                "CH-PW-14, Y2, H6, EC", "CH-PW-15, Y2, H7, EC", "CH-PW-16, Y2, H8, EC",
                "CH-PW-17, Y3, H1, ALTA", "CH-PW-18, Y3, H2, RECHAZO", "CH-PW-19, Y3, H3, EC",
                "CH-PW-20, Y3, H4, EC", "CH-PW-23, Y3, H7, EC", "CH-PW-24, Y3, H8, EC"})
        void darDeAltaPairwise(String id, String datos, String respuesta, String esperado) {
            comprobar(datos, respuesta, esperado);
        }

        @ParameterizedTest(name = "{0}")
        @DisplayName("Posible fallo PF-5: el servidor responde null")
        @CsvSource({"CH-PW-13, Y2, H5, EC", "CH-PW-21, Y3, H5, EC"})
        void posibleFalloPf5Pairwise(String id, String datos, String respuesta, String esperado) {
            comprobar(datos, respuesta, esperado);
        }

        @ParameterizedTest(name = "{0}")
        @DisplayName("CU: http null")
        @CsvSource({"CU-PW-08, http://localhost:7070/", "CU-PW-09, http://localhost:7070///",
                "CU-PW-10,", "CU-PW-12, localhost:7070"})
        void httpNuloPairwise(String id, String url) {
            assertThrows(NullPointerException.class, () -> new ClientePacientesHttp(url, null));
        }

        @Test
        @DisplayName("CU-PW-11: http null y la URL no es una URI")
        void httpNuloYUrlNoValidaPairwise() {
            assertThrows(IllegalArgumentException.class,
                    () -> new ClientePacientesHttp("http://servidor malo", null));
        }
    }

    @Nested
    @DisplayName("CH y CU · Posibles fallos")
    class PosiblesFallos {

        @Test
        @DisplayName("CU-PF-01 · Posible fallo PF-7: enviar con una URL sin esquema")
        void posibleFalloPf7() {
            ClientePacientesHttp cliente = new ClientePacientesHttp("localhost:7070", http);

            assertThrows(ErrorComunicacionException.class, () -> cliente.darDeAlta(Y1));

            assertEquals(0, http.peticiones.size());
        }
    }

    private void comprobarUrl(String url) {
        http.cuerpo = "{\"correcto\":true,\"mensaje\":\"Paciente dado de alta.\"}";

        new ClientePacientesHttp(url, http).darDeAlta(Y1);

        assertEquals(URL_PACIENTES, http.peticiones.get(0).uri());
    }

    private void comprobar(String codigoDatos, String codigoRespuesta, String esperado) {
        DatosAltaPaciente datos = switch (codigoDatos) {
            case "Y1" -> Y1;
            case "Y2" -> null;
            case "Y3" -> Y3;
            default -> throw new IllegalArgumentException(codigoDatos);
        };
        prepararRespuesta(codigoRespuesta);
        ClientePacientesHttp cliente = new ClientePacientesHttp(URL_BASE, http);

        switch (esperado) {
            case "ALTA" -> assertEquals(new ResultadoAlta(true, "Paciente dado de alta."),
                    cliente.darDeAlta(datos));
            case "RECHAZO" -> assertEquals(
                    new ResultadoAlta(false, "La letra del campo dni no es correcta."),
                    cliente.darDeAlta(datos));
            case "EC" -> {
                ErrorComunicacionException excepcion = assertThrows(ErrorComunicacionException.class,
                        () -> cliente.darDeAlta(datos));
                if (codigoRespuesta.equals("H7")) {
                    assertSame(http.falloRed, excepcion.getCause());
                }
                if (codigoRespuesta.equals("H8")) {
                    assertTrue(Thread.interrupted(), "El hilo debe quedar marcado como interrumpido");
                }
            }
            default -> throw new IllegalArgumentException(esperado);
        }

        assertEquals(1, http.peticiones.size());
        assertEquals("POST", http.peticiones.get(0).method());
        assertEquals(URL_PACIENTES, http.peticiones.get(0).uri());
        String jsonEsperado = switch (codigoDatos) {
            case "Y1" -> JSON_Y1;
            case "Y2" -> "null";
            default -> JSON_Y3;
        };
        assertEquals(jsonEsperado, http.cuerpos.get(0));
    }

    private void prepararRespuesta(String codigoRespuesta) {
        switch (codigoRespuesta) {
            case "H1" -> {
                http.codigo = 201;
                http.cuerpo = "{\"correcto\":true,\"mensaje\":\"Paciente dado de alta.\"}";
            }
            case "H2" -> {
                http.codigo = 400;
                http.cuerpo = "{\"correcto\":false,\"mensaje\":\"La letra del campo dni no es correcta.\"}";
            }
            case "H3" -> {
                http.codigo = 500;
                http.cuerpo = "Internal server error";
            }
            case "H4" -> http.cuerpo = "";
            case "H5" -> http.cuerpo = "null";
            case "H6" -> http.cuerpo = "{\"otro\":1}";
            case "H7" -> http.falloRed = new IOException("Conexión rechazada");
            case "H8" -> http.interrupcion = new InterruptedException("Interrumpido");
            default -> throw new IllegalArgumentException(codigoRespuesta);
        }
    }
}
