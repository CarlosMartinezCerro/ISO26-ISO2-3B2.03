package es.uclm.esi.iso2.radiodiagnostico.notificacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Prueba el cliente HTTP contra un servidor falso del JDK que imita la API del CdU5. */
class ClienteNotificacionesHttpTest {

    private static final DatosNotificacion DATOS =
            new DatosNotificacion("12345678Z", "SMS", "Recordatorio", "Traiga la tarjeta.");

    private HttpServer servidor;
    private String rutaRecibida;
    private String metodoRecibido;
    private String cuerpoRecibido;
    private int codigoRespuesta;
    private String cuerpoRespuesta;

    @BeforeEach
    void arrancarServidorFalso() throws IOException {
        servidor = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        servidor.createContext("/", intercambio -> {
            rutaRecibida = intercambio.getRequestURI().getRawPath();
            metodoRecibido = intercambio.getRequestMethod();
            cuerpoRecibido = new String(intercambio.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
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
    void enviaLaNotificacionEnJsonYDevuelveElResultado() {
        codigoRespuesta = 201;
        cuerpoRespuesta = "{\"correcto\":true,\"mensaje\":\"Notificación NOT-001 enviada.\",\"idNotificacion\":\"NOT-001\"}";

        ResultadoNotificacion resultado = cliente().notificar(DATOS);

        assertEquals(new ResultadoNotificacion(true, "Notificación NOT-001 enviada.", "NOT-001"), resultado);
        assertEquals("POST", metodoRecibido);
        assertEquals(ClienteNotificacionesHttp.RUTA_NOTIFICACIONES, rutaRecibida);
        assertEquals("{\"dniPaciente\":\"12345678Z\",\"canal\":\"SMS\",\"asunto\":\"Recordatorio\","
                + "\"mensaje\":\"Traiga la tarjeta.\"}", cuerpoRecibido);
    }

    @Test
    void devuelveElRechazoDelServidor() {
        codigoRespuesta = 400;
        cuerpoRespuesta = "{\"correcto\":false,\"mensaje\":\"No existe ningún paciente.\",\"idNotificacion\":null}";

        assertEquals(new ResultadoNotificacion(false, "No existe ningún paciente.", null), cliente().notificar(DATOS));
    }

    @Test
    void pideElHistorialDelPacienteYLeeLasFechas() {
        codigoRespuesta = 200;
        cuerpoRespuesta = "{\"correcto\":true,\"mensaje\":\"1 notificación(es) encontradas.\",\"notificaciones\":["
                + "{\"id\":\"NOT-001\",\"dniPaciente\":\"12345678Z\",\"tipo\":\"GENERAL\",\"canal\":\"SMS\","
                + "\"destino\":\"612345678\",\"asunto\":\"Recordatorio\",\"mensaje\":\"Traiga la tarjeta.\","
                + "\"fechaEnvio\":\"2026-11-21T10:00:00\",\"campoNuevo\":1}]}";

        RespuestaListadoNotificaciones respuesta = cliente().historialPaciente("12345678Z");

        assertEquals("GET", metodoRecibido);
        assertEquals("/pacientes/12345678Z/notificaciones", rutaRecibida);
        assertTrue(respuesta.correcto());
        assertEquals(List.of(new NotificacionEnviada("NOT-001", "12345678Z", "GENERAL", "SMS", "612345678",
                "Recordatorio", "Traiga la tarjeta.", LocalDateTime.of(2026, 11, 21, 10, 0))),
                respuesta.notificaciones());
    }

    @Test
    void codificaElDniEnLaUrl() {
        codigoRespuesta = 400;
        cuerpoRespuesta = "{\"correcto\":false,\"mensaje\":\"x\",\"notificaciones\":[]}";

        cliente().historialPaciente("12 34/Z");

        assertEquals("/pacientes/12%2034%2FZ/notificaciones", rutaRecibida);
    }

    @Test
    void respuestaQueNoEsJsonLanzaErrorDeComunicacion() {
        codigoRespuesta = 404;
        cuerpoRespuesta = "Not Found";

        assertThrows(ErrorComunicacionException.class, () -> cliente().historialPaciente("12345678Z"));
    }

    @Test
    void servidorCaidoLanzaErrorDeComunicacion() {
        String url = "http://localhost:" + servidor.getAddress().getPort();
        servidor.stop(0);

        assertThrows(ErrorComunicacionException.class, () -> new ClienteNotificacionesHttp(url).notificar(DATOS));
    }

    @Test
    void hiloInterrumpidoLanzaErrorDeComunicacionYConservaLaInterrupcion() {
        codigoRespuesta = 200;
        cuerpoRespuesta = "{}";
        Thread.currentThread().interrupt();

        assertThrows(ErrorComunicacionException.class, () -> cliente().notificar(DATOS));
        assertEquals(true, Thread.interrupted());
    }

    @Test
    void parametrosObligatorios() {
        assertThrows(NullPointerException.class, () -> new ClienteNotificacionesHttp(null));
        assertThrows(NullPointerException.class, () -> new ClienteNotificacionesHttp("http://localhost", null));
    }

    private ClienteNotificacionesHttp cliente() {
        return new ClienteNotificacionesHttp("http://localhost:" + servidor.getAddress().getPort() + "/");
    }
}
