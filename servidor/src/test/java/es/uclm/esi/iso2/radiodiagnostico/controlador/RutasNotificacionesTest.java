package es.uclm.esi.iso2.radiodiagnostico.controlador;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.uclm.esi.iso2.radiodiagnostico.Servidor;
import es.uclm.esi.iso2.radiodiagnostico.notificacion.NotificacionRepositorioEnMemoria;
import es.uclm.esi.iso2.radiodiagnostico.notificacion.ServicioNotificacionesImpl;
import io.javalin.Javalin;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Prueba la API REST del CdU5 de extremo a extremo, con el servidor arrancado en un puerto libre. */
class RutasNotificacionesTest {

    private static final String SMS_A_JUAN = "{\"dniPaciente\":\"12345678Z\",\"canal\":\"SMS\","
            + "\"asunto\":\"Recordatorio\",\"mensaje\":\"Traiga la tarjeta.\"}";

    private final HttpClient http = HttpClient.newHttpClient();
    private Javalin app;

    @BeforeEach
    void arrancarServidor() {
        app = Servidor.crear(new RutasNotificaciones(new ControladorNotificaciones(new ServicioNotificacionesImpl(
                ControladorNotificacionesTest.pacientes(), new NotificacionRepositorioEnMemoria(), n -> { }))))
                .start(0);
    }

    @AfterEach
    void pararServidor() {
        app.stop();
    }

    @Test
    void notificarDevuelve201() throws Exception {
        HttpResponse<String> respuesta = post(SMS_A_JUAN);

        assertEquals(201, respuesta.statusCode());
        assertTrue(respuesta.body().contains("\"correcto\":true"));
        assertTrue(respuesta.body().contains("\"idNotificacion\":\"NOT-001\""));
    }

    @Test
    void notificacionRechazadaDevuelve400ConElMotivo() throws Exception {
        HttpResponse<String> respuesta = post(SMS_A_JUAN.replace("12345678Z", "87654321X"));

        assertEquals(400, respuesta.statusCode());
        assertTrue(respuesta.body().contains("No existe ningún paciente con el DNI 87654321X."));
    }

    @Test
    void canalDesconocidoDevuelve400() throws Exception {
        HttpResponse<String> respuesta = post(SMS_A_JUAN.replace("SMS", "PALOMA_MENSAJERA"));

        assertEquals(400, respuesta.statusCode());
        assertTrue(respuesta.body().contains(RutasNotificaciones.PETICION_MAL_FORMADA));
    }

    @Test
    void jsonInvalidoDevuelve400() throws Exception {
        HttpResponse<String> respuesta = post("esto no es json");

        assertEquals(400, respuesta.statusCode());
        assertTrue(respuesta.body().contains(RutasNotificaciones.PETICION_MAL_FORMADA));
    }

    @Test
    void historialDevuelveLasNotificacionesConLaFechaComoTexto() throws Exception {
        post(SMS_A_JUAN);

        HttpResponse<String> respuesta = get("/pacientes/12345678Z/notificaciones");

        assertEquals(200, respuesta.statusCode());
        assertTrue(respuesta.body().contains("\"mensaje\":\"1 notificación(es) encontradas.\""));
        assertTrue(respuesta.body().contains("\"destino\":\"612345678\""));
        assertTrue(respuesta.body().matches(".*\"fechaEnvio\":\"\\d{4}-\\d{2}-\\d{2}T.*"), respuesta.body());
    }

    @Test
    void historialConDniEnBlancoDevuelve400() throws Exception {
        HttpResponse<String> respuesta = get("/pacientes/%20/notificaciones");

        assertEquals(400, respuesta.statusCode());
        assertTrue(respuesta.body().contains("El campo dni es obligatorio."));
    }

    @Test
    void controladorObligatorio() {
        assertThrows(NullPointerException.class, () -> new RutasNotificaciones(null));
    }

    private HttpResponse<String> post(String cuerpo) throws IOException, InterruptedException {
        return http.send(HttpRequest.newBuilder(URI.create(url(RutasNotificaciones.RUTA_NOTIFICACIONES)))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(cuerpo))
                .build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> get(String ruta) throws IOException, InterruptedException {
        return http.send(HttpRequest.newBuilder(URI.create(url(ruta))).GET().build(),
                HttpResponse.BodyHandlers.ofString());
    }

    private String url(String ruta) {
        return "http://localhost:" + app.port() + ruta;
    }
}
