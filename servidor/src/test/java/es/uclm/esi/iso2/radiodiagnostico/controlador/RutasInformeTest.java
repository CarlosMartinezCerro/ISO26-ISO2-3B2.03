package es.uclm.esi.iso2.radiodiagnostico.controlador;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import es.uclm.esi.iso2.radiodiagnostico.Servidor;
import es.uclm.esi.iso2.radiodiagnostico.paciente.PacienteRepositorioEnMemoria;
import es.uclm.esi.iso2.radiodiagnostico.paciente.ServicioPacientesImpl;
import io.javalin.Javalin;

class RutasInformeTest {

    private final HttpClient http = HttpClient.newHttpClient();
    private Javalin app;

    @BeforeEach
    void arrancarServidor() {
        app = Servidor.crear(
                new ServicioPacientesImpl(new PacienteRepositorioEnMemoria())).start(0);
    }

    @AfterEach
    void pararServidor() {
        app.stop();
    }

    @Test
    void creaInformeCorrecto() throws IOException, InterruptedException {
        HttpResponse<String> respuesta = post("""
                {"dniPaciente":"87654321B","dniMedico":"12345678A",
                 "especialidad":"Radiologia","consultaActual":"Dolor","diagnostico":"Control",
                 "fecha":"2026-11-19"}
                """);

        assertEquals(201, respuesta.statusCode());
        assertTrue(respuesta.body().contains("\"correcto\":true"));
    }

    @Test
    void datosAusentesDevuelven400() throws IOException, InterruptedException {
        HttpResponse<String> respuesta = post("""
                {"dniPaciente":"87654321B","especialidad":"Radiologia"}
                """);

        assertEquals(400, respuesta.statusCode());
    }

    @Test
    void obtieneAntecedentesCorrectamente() throws IOException, InterruptedException {
        HttpRequest peticion = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + app.port()
                        + RutasInforme.RUTA_ANTECEDENTES
                        + "?dniPaciente=87654321B&especialidad=Radiologia"))
                .GET()
                .build();

        HttpResponse<String> respuesta =
                http.send(peticion, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, respuesta.statusCode());
        assertTrue(respuesta.body().contains("Antecedentes simulados"));
    }

    @Test
    void parametrosAusentesDevuelven400() throws IOException, InterruptedException {
        HttpRequest peticion = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + app.port()
                        + RutasInforme.RUTA_ANTECEDENTES + "?dniPaciente=87654321B"))
                .GET()
                .build();

        HttpResponse<String> respuesta =
                http.send(peticion, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, respuesta.statusCode());
    }

    private HttpResponse<String> post(String cuerpo)
            throws IOException, InterruptedException {
        HttpRequest peticion = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + app.port()
                        + RutasInforme.RUTA_INFORMES))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(cuerpo))
                .build();
        return http.send(peticion, HttpResponse.BodyHandlers.ofString());
    }
}
