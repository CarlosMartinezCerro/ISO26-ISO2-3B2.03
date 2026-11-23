package es.uclm.esi.iso2.radiodiagnostico.controlador;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.uclm.esi.iso2.radiodiagnostico.Servidor;
import es.uclm.esi.iso2.radiodiagnostico.modelo.cita.Cita;
import es.uclm.esi.iso2.radiodiagnostico.modelo.cita.CitaRepositorioEnMemoria;
import es.uclm.esi.iso2.radiodiagnostico.modelo.cita.EstadoCita;
import es.uclm.esi.iso2.radiodiagnostico.modelo.cita.ServicioConfirmacionCitasImpl;
import io.javalin.Javalin;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Prueba la API REST del CdU2 de extremo a extremo, con el servidor arrancado en un puerto libre. */
class RutasConfirmacionCitasTest {

    private final HttpClient http = HttpClient.newHttpClient();
    private CitaRepositorioEnMemoria citas;
    private Javalin app;

    @BeforeEach
    void arrancarServidor() {
        citas = new CitaRepositorioEnMemoria();
        citas.guardar(new Cita("CITA-SOL-001", "SOL-001", "FICTICIO-001", "Radiografía",
                LocalDateTime.of(2026, 11, 20, 9, 30), "SALA-RX-1", EstadoCita.ASIGNADA));
        app = Servidor.crear(new RutasConfirmacionCitas(new ControladorConfirmacionCitas(
                new ServicioConfirmacionCitasImpl(citas)))).start(0);
    }

    @AfterEach
    void pararServidor() {
        app.stop();
    }

    @Test
    void confirmacionCorrectaDevuelve200YCambiaElEstado() throws Exception {
        HttpResponse<String> respuesta = post("CITA-SOL-001", "{\"dniPaciente\":\"FICTICIO-001\"}");

        assertEquals(200, respuesta.statusCode());
        assertTrue(respuesta.body().contains("\"correcto\":true"));
        assertEquals(EstadoCita.CONFIRMADA, citas.buscarPorId("CITA-SOL-001").orElseThrow().estado());
    }

    @Test
    void segundaConfirmacionDevuelve400() throws Exception {
        post("CITA-SOL-001", "{\"dniPaciente\":\"FICTICIO-001\"}");

        HttpResponse<String> respuesta = post("CITA-SOL-001", "{\"dniPaciente\":\"FICTICIO-001\"}");

        assertEquals(400, respuesta.statusCode());
        assertTrue(respuesta.body().contains("\"correcto\":false"));
    }

    @Test
    void citaDeOtroPacienteDevuelve400SinConfirmarla() throws Exception {
        HttpResponse<String> respuesta = post("CITA-SOL-001", "{\"dniPaciente\":\"FICTICIO-002\"}");

        assertEquals(400, respuesta.statusCode());
        assertEquals(EstadoCita.ASIGNADA, citas.buscarPorId("CITA-SOL-001").orElseThrow().estado());
    }

    @Test
    void mandaElIdDeLaUrlAunqueElCuerpoTraigaOtro() throws Exception {
        HttpResponse<String> respuesta = post("CITA-SOL-001",
                "{\"idCita\":\"CITA-SOL-999\",\"dniPaciente\":\"FICTICIO-001\"}");

        assertEquals(200, respuesta.statusCode());
        assertEquals(EstadoCita.CONFIRMADA, citas.buscarPorId("CITA-SOL-001").orElseThrow().estado());
    }

    @Test
    void cuerpoMalFormadoDevuelve400() throws Exception {
        HttpResponse<String> respuesta = post("CITA-SOL-001", "esto no es json");

        assertEquals(400, respuesta.statusCode());
        assertTrue(respuesta.body().contains(RutasConfirmacionCitas.PETICION_MAL_FORMADA));
    }

    private HttpResponse<String> post(String idCita, String cuerpo)
            throws IOException, InterruptedException {
        HttpRequest peticion = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + app.port() + "/citas/" + idCita + "/confirmacion"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(cuerpo))
                .build();
        return http.send(peticion, HttpResponse.BodyHandlers.ofString());
    }
}
