package es.uclm.esi.iso2.radiodiagnostico.controlador;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.uclm.esi.iso2.radiodiagnostico.Servidor;
import es.uclm.esi.iso2.radiodiagnostico.paciente.PacienteRepositorioEnMemoria;
import es.uclm.esi.iso2.radiodiagnostico.paciente.ServicioPacientes;
import es.uclm.esi.iso2.radiodiagnostico.paciente.ServicioPacientesImpl;
import io.javalin.Javalin;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Prueba la API REST de extremo a extremo, con el servidor arrancado en un puerto libre. */
class RutasPacientesTest {

    private static final String PACIENTE_VALIDO = """
            {"dni":"12345678Z","nombre":"Alex","apellidos":"Ejemplo",
             "fechaNacimiento":"1990-05-12","telefono":"600000001"}
            """;

    private final HttpClient http = HttpClient.newHttpClient();
    private ServicioPacientes servicio;
    private Javalin app;

    @BeforeEach
    void arrancarServidor() {
        servicio = new ServicioPacientesImpl(new PacienteRepositorioEnMemoria());
        app = Servidor.crear(servicio).start(0);
    }

    @AfterEach
    void pararServidor() {
        app.stop();
    }

    @Test
    void altaCorrectaDevuelve201YGuardaElPaciente() throws Exception {
        HttpResponse<String> respuesta = post(PACIENTE_VALIDO);

        assertEquals(201, respuesta.statusCode());
        assertTrue(respuesta.body().contains("\"correcto\":true"));
        assertTrue(servicio.buscarPorDni("12345678Z").isPresent());
    }

    @Test
    void altaDuplicadaDevuelve400ConMensaje() throws Exception {
        post(PACIENTE_VALIDO);

        HttpResponse<String> respuesta = post(PACIENTE_VALIDO);

        assertEquals(400, respuesta.statusCode());
        assertTrue(respuesta.body().contains("\"correcto\":false"));
        assertTrue(respuesta.body().contains("Ya existe un paciente con el DNI 12345678Z."));
    }

    @Test
    void datosInvalidosDevuelven400() throws Exception {
        HttpResponse<String> respuesta = post("""
                {"dni":"12345678A","nombre":"Alex","apellidos":"Ejemplo",
                 "fechaNacimiento":"1990-05-12"}
                """);

        assertEquals(400, respuesta.statusCode());
        assertTrue(respuesta.body().contains("La letra del campo dni no es correcta."));
    }

    @Test
    void cuerpoMalFormadoDevuelve400() throws Exception {
        HttpResponse<String> respuesta = post("esto no es json");

        assertEquals(400, respuesta.statusCode());
        assertTrue(respuesta.body().contains(RutasPacientes.PETICION_MAL_FORMADA));
    }

    @Test
    void fechaConFormatoIncorrectoDevuelve400() throws Exception {
        HttpResponse<String> respuesta = post("""
                {"dni":"12345678Z","nombre":"Alex","apellidos":"Ejemplo",
                 "fechaNacimiento":"12/05/1990"}
                """);

        assertEquals(400, respuesta.statusCode());
        assertTrue(respuesta.body().contains(RutasPacientes.PETICION_MAL_FORMADA));
    }

    private HttpResponse<String> post(String cuerpo) throws IOException, InterruptedException {
        HttpRequest peticion = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + app.port() + RutasPacientes.RUTA_PACIENTES))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(cuerpo))
                .build();
        return http.send(peticion, HttpResponse.BodyHandlers.ofString());
    }
}
