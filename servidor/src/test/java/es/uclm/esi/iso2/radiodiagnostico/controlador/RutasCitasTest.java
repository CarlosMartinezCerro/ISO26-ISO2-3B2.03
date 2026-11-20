package es.uclm.esi.iso2.radiodiagnostico.controlador;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.uclm.esi.iso2.radiodiagnostico.Servidor;
import es.uclm.esi.iso2.radiodiagnostico.cita.CitaRepositorioEnMemoria;
import es.uclm.esi.iso2.radiodiagnostico.cita.ServicioCitasImpl;
import es.uclm.esi.iso2.radiodiagnostico.solicitud.EstadoSolicitud;
import es.uclm.esi.iso2.radiodiagnostico.solicitud.PrioridadSolicitud;
import es.uclm.esi.iso2.radiodiagnostico.solicitud.SolicitudPrueba;
import es.uclm.esi.iso2.radiodiagnostico.solicitud.SolicitudRepositorioEnMemoria;
import io.javalin.Javalin;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Prueba la API REST del CdU25 de extremo a extremo, con el servidor arrancado en un puerto libre. */
class RutasCitasTest {

    private final HttpClient http = HttpClient.newHttpClient();
    private SolicitudRepositorioEnMemoria solicitudes;
    private Javalin app;

    @BeforeEach
    void arrancarServidor() {
        solicitudes = new SolicitudRepositorioEnMemoria();
        solicitudes.guardar(new SolicitudPrueba("SOL-001", "FICTICIO-001", "Radiografía",
                PrioridadSolicitud.ORDINARIO, EstadoSolicitud.PENDIENTE));
        solicitudes.guardar(new SolicitudPrueba("SOL-002", "FICTICIO-002", "TAC",
                PrioridadSolicitud.URGENTE, EstadoSolicitud.PENDIENTE));
        app = Servidor.crear(new RutasCitas(new ControladorCitas(
                new ServicioCitasImpl(solicitudes, new CitaRepositorioEnMemoria())))).start(0);
    }

    @AfterEach
    void pararServidor() {
        app.stop();
    }

    @Test
    void devuelveLasSolicitudesPendientesPorPrioridad() throws Exception {
        HttpResponse<String> respuesta = get(RutasCitas.RUTA_SOLICITUDES_PENDIENTES);

        assertEquals(200, respuesta.statusCode());
        assertTrue(respuesta.body().startsWith("[{\"id\":\"SOL-002\""), respuesta.body());
        assertTrue(respuesta.body().contains("\"prioridad\":\"URGENTE\""));
    }

    @Test
    void asignacionCorrectaDevuelve201YLaSolicitudDejaDeEstarPendiente() throws Exception {
        HttpResponse<String> respuesta = post(cita("SOL-001", "2099-01-01T09:30", "SALA-RX-1"));

        assertEquals(201, respuesta.statusCode());
        assertTrue(respuesta.body().contains("\"correcto\":true"));
        assertEquals(EstadoSolicitud.CITADA, solicitudes.buscarPorId("SOL-001").orElseThrow().estado());
    }

    @Test
    void datosInvalidosDevuelven400ConMensaje() throws Exception {
        HttpResponse<String> respuesta = post(cita("SOL-001", "2099-01-01T09:30", ""));

        assertEquals(400, respuesta.statusCode());
        assertTrue(respuesta.body().contains("El campo sala es obligatorio."));
    }

    @Test
    void solicitudYaCitadaDevuelve400() throws Exception {
        post(cita("SOL-001", "2099-01-01T09:30", "SALA-RX-1"));

        HttpResponse<String> respuesta = post(cita("SOL-001", "2099-01-02T09:30", "SALA-RX-1"));

        assertEquals(400, respuesta.statusCode());
        assertTrue(respuesta.body().contains("ya tiene una cita asignada"));
    }

    @Test
    void cuerpoMalFormadoDevuelve400() throws Exception {
        HttpResponse<String> respuesta = post("esto no es json");

        assertEquals(400, respuesta.statusCode());
        assertTrue(respuesta.body().contains(RutasCitas.PETICION_MAL_FORMADA));
    }

    @Test
    void fechaMalEscritaDevuelve400() throws Exception {
        HttpResponse<String> respuesta = post(cita("SOL-001", "mañana", "SALA-RX-1"));

        assertEquals(400, respuesta.statusCode());
        assertTrue(respuesta.body().contains(RutasCitas.PETICION_MAL_FORMADA));
    }

    private static String cita(String solicitud, String fechaHora, String sala) {
        return """
                {"idSolicitud":"%s","fechaHora":"%s","sala":"%s"}
                """.formatted(solicitud, fechaHora, sala);
    }

    private HttpResponse<String> get(String ruta) throws IOException, InterruptedException {
        return http.send(HttpRequest.newBuilder(url(ruta)).GET().build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> post(String cuerpo) throws IOException, InterruptedException {
        HttpRequest peticion = HttpRequest.newBuilder(url(RutasCitas.RUTA_CITAS))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(cuerpo))
                .build();
        return http.send(peticion, HttpResponse.BodyHandlers.ofString());
    }

    private URI url(String ruta) {
        return URI.create("http://localhost:" + app.port() + ruta);
    }
}
