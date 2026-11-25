package es.uclm.esi.iso2.radiodiagnostico.prueba;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * CdU13, unidades CH y CU, con un {@code HttpClient} falso. Diseño: docs/testing/CdU13-diseno-pruebas.md.
 * Con dos variables, pairwise coincide con el producto cartesiano y contiene a each use (6.7).
 */
@DisplayName("CdU13 · ClienteSeguimientosHttp")
class ClienteSeguimientosHttpCdU13Test {

    private static final String URL_BASE = "http://localhost:7070";
    private static final URI URL_SEGUIMIENTOS = URI.create("http://localhost:7070/seguimientos");
    private static final LocalDate INICIO = LocalDate.of(2026, 11, 20);
    private static final DatosSolicitudSeguimiento Y1 = new DatosSolicitudSeguimiento("12345678Z",
            "00000001R", "Mamografía", "Cribado (caso ficticio)", "Programa de cribado (caso ficticio)",
            PrioridadSeguimiento.PREFERENTE, 2, UnidadPeriodicidad.SEMANAS, INICIO, 3, null);
    private static final DatosSolicitudSeguimiento Y3 = new DatosSolicitudSeguimiento("12345678Z",
            "00000001R", "TAC", "Indicación ficticia", "Justificación ficticia",
            PrioridadSeguimiento.URGENTE, 1, UnidadPeriodicidad.MESES, INICIO, null,
            LocalDate.of(2027, 5, 20));
    private static final String JSON_Y1 = """
            {"dniPaciente":"12345678Z","dniMedico":"00000001R","tipoPrueba":"Mamografía",
             "indicacionClinica":"Cribado (caso ficticio)",
             "justificacion":"Programa de cribado (caso ficticio)","prioridad":"PREFERENTE","cada":2,
             "unidad":"SEMANAS","fechaInicio":"2026-11-20","numeroRepeticiones":3,"fechaLimite":null}
            """;
    private static final String JSON_Y3 = """
            {"dniPaciente":"12345678Z","dniMedico":"00000001R","tipoPrueba":"TAC",
             "indicacionClinica":"Indicación ficticia","justificacion":"Justificación ficticia",
             "prioridad":"URGENTE","cada":1,"unidad":"MESES","fechaInicio":"2026-11-20",
             "numeroRepeticiones":null,"fechaLimite":"2027-05-20"}
            """;
    private static final ResultadoSolicitudSeguimiento ACEPTADA = new ResultadoSolicitudSeguimiento(true,
            "Solicitud registrada.", "SEG-1", List.of(
                    new RepeticionSeguimiento("SEG-1-1", "SEG-1", 1, INICIO, "PENDIENTE_CITA"),
                    new RepeticionSeguimiento("SEG-1-2", "SEG-1", 2, LocalDate.of(2026, 12, 4),
                            "PENDIENTE_CITA")));
    private static final ResultadoSolicitudSeguimiento RECHAZADA = new ResultadoSolicitudSeguimiento(false,
            "El tipo de prueba Radiografía no admite seguimiento periódico.", null, List.of());
    private static final ResultadoSolicitudSeguimiento SIN_LISTA =
            new ResultadoSolicitudSeguimiento(false, "Error", null, List.of());

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
        @DisplayName("CH: solicitar")
        @CsvSource({"CH-PW-01, Y1, H1", "CH-PW-02, Y1, H2", "CH-PW-03, Y1, H3", "CH-PW-07, Y1, H7",
                "CH-PW-08, Y1, H8", "CH-PW-09, Y1, H9", "CH-PW-13, Y2, H4", "CH-PW-24, Y3, H6"})
        void solicitarEachUse(String id, String datos, String respuesta) throws Exception {
            comprobar(id, datos, respuesta);
        }

        @Test
        @DisplayName("CH-PW-05 · Posible fallo PX-3: el servidor responde null")
        void posibleFalloPx3EachUse() throws Exception {
            comprobar("CH-PW-05", "Y1", "H5");
        }

        @ParameterizedTest(name = "{0}")
        @DisplayName("CU: URL válidas")
        @CsvSource({"CU-PW-01, http://localhost:7070", "CU-PW-03, http://localhost:7070///"})
        void urlValidaEachUse(String id, String url) {
            prepararRespuesta("H1");

            new ClienteSeguimientosHttp(url, http).solicitar(Y1);

            assertEquals(URL_SEGUIMIENTOS, http.peticiones.get(0).uri(), id);
        }

        @Test
        @DisplayName("CU-PW-04: la URL es obligatoria")
        void urlNulaEachUse() {
            assertThrows(NullPointerException.class, () -> new ClienteSeguimientosHttp(null, http));
        }

        @Test
        @DisplayName("CU-PW-05: la URL no es una URI")
        void urlNoValidaEachUse() {
            assertThrows(IllegalArgumentException.class,
                    () -> new ClienteSeguimientosHttp("http://servidor malo", http));
        }

        @Test
        @DisplayName("CU-PW-06: URL sin esquema")
        void urlSinEsquemaEachUse() {
            assertDoesNotThrow(() -> new ClienteSeguimientosHttp("localhost:7070", http));
        }

        @Test
        @DisplayName("CU-PW-07: el cliente HTTP es obligatorio")
        void httpNuloEachUse() {
            assertThrows(NullPointerException.class, () -> new ClienteSeguimientosHttp(URL_BASE, null));
        }
    }

    @Nested
    @DisplayName("CH y CU · Pairwise")
    class Pairwise {

        @ParameterizedTest(name = "{0}")
        @DisplayName("CH: solicitar")
        @CsvSource({"CH-PW-04, Y1, H4", "CH-PW-06, Y1, H6", "CH-PW-10, Y2, H1", "CH-PW-11, Y2, H2",
                "CH-PW-12, Y2, H3", "CH-PW-15, Y2, H6", "CH-PW-16, Y2, H7", "CH-PW-17, Y2, H8",
                "CH-PW-18, Y2, H9", "CH-PW-19, Y3, H1", "CH-PW-20, Y3, H2", "CH-PW-21, Y3, H3",
                "CH-PW-22, Y3, H4", "CH-PW-25, Y3, H7", "CH-PW-26, Y3, H8", "CH-PW-27, Y3, H9"})
        void solicitarPairwise(String id, String datos, String respuesta) throws Exception {
            comprobar(id, datos, respuesta);
        }

        @ParameterizedTest(name = "{0}")
        @DisplayName("Posible fallo PX-3: el servidor responde null")
        @CsvSource({"CH-PW-14, Y2, H5", "CH-PW-23, Y3, H5"})
        void posibleFalloPx3Pairwise(String id, String datos, String respuesta) throws Exception {
            comprobar(id, datos, respuesta);
        }

        @ParameterizedTest(name = "{0}")
        @DisplayName("CU: http null")
        @CsvSource({"CU-PW-08, http://localhost:7070/", "CU-PW-09, http://localhost:7070///",
                "CU-PW-10,", "CU-PW-12, localhost:7070"})
        void httpNuloPairwise(String id, String url) {
            assertThrows(NullPointerException.class, () -> new ClienteSeguimientosHttp(url, null), id);
        }

        @Test
        @DisplayName("CU-PW-11: http null y la URL no es una URI")
        void httpNuloYUrlNoValidaPairwise() {
            RuntimeException error = assertThrows(RuntimeException.class,
                    () -> new ClienteSeguimientosHttp("http://servidor malo", null));

            assertTrue(error instanceof IllegalArgumentException || error instanceof NullPointerException,
                    error::toString);
        }
    }

    @Nested
    @DisplayName("CH y CU · Posibles fallos")
    class PosiblesFallos {

        @Test
        @DisplayName("CU-PX-01 · Posible fallo PX-4: enviar con una URL sin esquema")
        void posibleFalloPx4() {
            ClienteSeguimientosHttp cliente = new ClienteSeguimientosHttp("localhost:7070", http);

            assertThrows(ErrorComunicacionException.class, () -> cliente.solicitar(Y1));

            assertEquals(0, http.peticiones.size());
        }
    }

    private void comprobar(String id, String codigoDatos, String respuesta) throws JsonProcessingException {
        DatosSolicitudSeguimiento datos = switch (codigoDatos) {
            case "Y1" -> Y1;
            case "Y2" -> null;
            case "Y3" -> Y3;
            default -> throw new IllegalArgumentException(codigoDatos);
        };
        prepararRespuesta(respuesta);
        ClienteSeguimientosHttp cliente = new ClienteSeguimientosHttp(URL_BASE, http);

        switch (respuesta) {
            case "H1" -> assertEquals(ACEPTADA, cliente.solicitar(datos), id);
            case "H2" -> assertEquals(RECHAZADA, cliente.solicitar(datos), id);
            case "H9" -> assertEquals(SIN_LISTA, cliente.solicitar(datos), id);
            default -> {
                ErrorComunicacionException error = assertThrows(ErrorComunicacionException.class,
                        () -> cliente.solicitar(datos), id);
                if (respuesta.equals("H7")) {
                    assertSame(http.falloRed, error.getCause(), id);
                }
                if (respuesta.equals("H8")) {
                    assertTrue(Thread.interrupted(), id + ": el hilo debe quedar interrumpido");
                }
            }
        }

        assertEquals(1, http.peticiones.size(), id);
        assertEquals("POST", http.peticiones.get(0).method(), id);
        assertEquals(URL_SEGUIMIENTOS, http.peticiones.get(0).uri(), id);
        ObjectMapper json = new ObjectMapper();
        assertEquals(json.readTree(switch (codigoDatos) {
            case "Y1" -> JSON_Y1;
            case "Y2" -> "null";
            default -> JSON_Y3;
        }), json.readTree(http.cuerpos.get(0)), id);
    }

    private void prepararRespuesta(String codigoRespuesta) {
        switch (codigoRespuesta) {
            case "H1" -> {
                http.codigo = 201;
                http.cuerpo = """
                        {"correcto":true,"mensaje":"Solicitud registrada.","idSolicitud":"SEG-1",
                         "repeticiones":[
                          {"id":"SEG-1-1","idSolicitudSeguimiento":"SEG-1","numero":1,
                           "fechaPrevista":"2026-11-20","estado":"PENDIENTE_CITA"},
                          {"id":"SEG-1-2","idSolicitudSeguimiento":"SEG-1","numero":2,
                           "fechaPrevista":"2026-12-04","estado":"PENDIENTE_CITA"}]}
                        """;
            }
            case "H2" -> {
                http.codigo = 400;
                http.cuerpo = "{\"correcto\":false,\"mensaje\":\"El tipo de prueba Radiografía no admite "
                        + "seguimiento periódico.\",\"idSolicitud\":null,\"repeticiones\":[]}";
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
            case "H9" -> {
                http.codigo = 400;
                http.cuerpo = "{\"correcto\":false,\"mensaje\":\"Error\"}";
            }
            default -> throw new IllegalArgumentException(codigoRespuesta);
        }
    }
}
