package es.uclm.esi.iso2.radiodiagnostico.modelo.prueba;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Prueba el cliente HTTP contra un servidor falso del JDK que imita la API del CdU13. */
class ClienteSeguimientosHttpTest {

    private static final DatosSolicitudSeguimiento CON_REPETICIONES = new DatosSolicitudSeguimiento(
            "12345678Z", "00000001R", "Mamografía", "Cribado (caso ficticio)",
            "Programa de cribado (caso ficticio)", PrioridadSeguimiento.PREFERENTE, 2,
            UnidadPeriodicidad.SEMANAS, LocalDate.of(2026, 11, 20), 3, null);

    private HttpServer servidor;
    private String cuerpoRecibido;
    private String metodoRecibido;
    private int codigoRespuesta;
    private String cuerpoRespuesta;

    @BeforeEach
    void arrancarServidorFalso() throws IOException {
        servidor = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        servidor.createContext(ClienteSeguimientosHttp.RUTA_SEGUIMIENTOS, intercambio -> {
            metodoRecibido = intercambio.getRequestMethod();
            cuerpoRecibido = new String(intercambio.getRequestBody().readAllBytes(),
                    StandardCharsets.UTF_8);
            byte[] respuesta = cuerpoRespuesta.getBytes(StandardCharsets.UTF_8);
            intercambio.sendResponseHeaders(codigoRespuesta, respuesta.length);
            try (OutputStream salida = intercambio.getResponseBody()) {
                salida.write(respuesta);
            }
        });
        servidor.start();
    }

    @AfterEach
    void pararServidorFalso() {
        servidor.stop(0);
    }

    @Test
    void enviaLosDatosEnJsonConElFormatoDelServidor() throws Exception {
        codigoRespuesta = 201;
        cuerpoRespuesta = "{\"correcto\":true,\"mensaje\":\"ok\",\"idSolicitud\":\"SEG-1\","
                + "\"repeticiones\":[]}";

        cliente().solicitar(CON_REPETICIONES);

        assertEquals("POST", metodoRecibido);
        JsonNode enviado = new ObjectMapper().readTree(cuerpoRecibido);
        assertEquals("12345678Z", enviado.get("dniPaciente").asText());
        assertEquals("00000001R", enviado.get("dniMedico").asText());
        assertEquals("Mamografía", enviado.get("tipoPrueba").asText());
        assertEquals("PREFERENTE", enviado.get("prioridad").asText());
        assertEquals(2, enviado.get("cada").asInt());
        assertEquals("SEMANAS", enviado.get("unidad").asText());
        assertEquals("2026-11-20", enviado.get("fechaInicio").asText());
        assertEquals(3, enviado.get("numeroRepeticiones").asInt());
        assertTrue(enviado.get("fechaLimite").isNull());
    }

    @Test
    void enviaLaFechaLimiteComoAaaaMmDd() throws Exception {
        codigoRespuesta = 201;
        cuerpoRespuesta = "{\"correcto\":true,\"mensaje\":\"ok\",\"idSolicitud\":\"SEG-1\","
                + "\"repeticiones\":[]}";

        cliente().solicitar(new DatosSolicitudSeguimiento("12345678Z", "00000001R", "TAC",
                "Indicación ficticia", "Justificación ficticia", PrioridadSeguimiento.URGENTE, 1,
                UnidadPeriodicidad.MESES, LocalDate.of(2026, 11, 20), null, LocalDate.of(2027, 5, 20)));

        JsonNode enviado = new ObjectMapper().readTree(cuerpoRecibido);
        assertTrue(enviado.get("numeroRepeticiones").isNull());
        assertEquals("2027-05-20", enviado.get("fechaLimite").asText());
    }

    @Test
    void convierteLaRespuestaCorrectaConSusRepeticiones() {
        codigoRespuesta = 201;
        cuerpoRespuesta = """
                {"correcto":true,"mensaje":"Solicitud registrada con 2 solicitudes pendientes de cita.",
                 "idSolicitud":"SEG-1","repeticiones":[
                  {"id":"SEG-1-1","idSolicitudSeguimiento":"SEG-1","numero":1,
                   "fechaPrevista":"2026-11-20","estado":"PENDIENTE_CITA"},
                  {"id":"SEG-1-2","idSolicitudSeguimiento":"SEG-1","numero":2,
                   "fechaPrevista":"2026-12-04","estado":"PENDIENTE_CITA"}]}
                """;

        ResultadoSolicitudSeguimiento resultado = cliente().solicitar(CON_REPETICIONES);

        assertTrue(resultado.correcto());
        assertEquals("SEG-1", resultado.idSolicitud());
        assertEquals(List.of(
                new RepeticionSeguimiento("SEG-1-1", "SEG-1", 1, LocalDate.of(2026, 11, 20),
                        "PENDIENTE_CITA"),
                new RepeticionSeguimiento("SEG-1-2", "SEG-1", 2, LocalDate.of(2026, 12, 4),
                        "PENDIENTE_CITA")), resultado.repeticiones());
    }

    @Test
    void devuelveElRechazoDelServidor() {
        codigoRespuesta = 400;
        cuerpoRespuesta = "{\"correcto\":false,\"mensaje\":\"El tipo de prueba Radiografía no admite "
                + "seguimiento periódico.\",\"idSolicitud\":null,\"repeticiones\":[]}";

        ResultadoSolicitudSeguimiento resultado = cliente().solicitar(CON_REPETICIONES);

        assertFalse(resultado.correcto());
        assertEquals("El tipo de prueba Radiografía no admite seguimiento periódico.",
                resultado.mensaje());
        assertNull(resultado.idSolicitud());
        assertTrue(resultado.repeticiones().isEmpty());
    }

    @Test
    void sinListaDeRepeticionesDevuelveListaVacia() {
        codigoRespuesta = 400;
        cuerpoRespuesta = "{\"correcto\":false,\"mensaje\":\"Error\"}";

        assertTrue(cliente().solicitar(CON_REPETICIONES).repeticiones().isEmpty());
    }

    @Test
    void respuestaQueNoEsUnResultadoEsErrorDeComunicacion() {
        codigoRespuesta = 500;
        cuerpoRespuesta = "Internal server error";

        assertThrows(ErrorComunicacionException.class, () -> cliente().solicitar(CON_REPETICIONES));
    }

    @Test
    void servidorParadoEsErrorDeComunicacion() {
        String url = url();
        servidor.stop(0);

        assertThrows(ErrorComunicacionException.class,
                () -> new ClienteSeguimientosHttp(url).solicitar(CON_REPETICIONES));
    }

    private ClienteSeguimientosHttp cliente() {
        return new ClienteSeguimientosHttp(url() + "/");
    }

    private String url() {
        return "http://localhost:" + servidor.getAddress().getPort();
    }
}
