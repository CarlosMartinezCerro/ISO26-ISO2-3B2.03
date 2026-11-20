package es.uclm.esi.iso2.radiodiagnostico.controlador;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.uclm.esi.iso2.radiodiagnostico.Servidor;
import es.uclm.esi.iso2.radiodiagnostico.informe.DatosImagen;
import es.uclm.esi.iso2.radiodiagnostico.informe.ImagenRepositorioEnMemoria;
import es.uclm.esi.iso2.radiodiagnostico.informe.ServicioImagenesImpl;
import es.uclm.esi.iso2.radiodiagnostico.informe.SistemaPacsSimulado;
import io.javalin.Javalin;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Prueba la API REST del CdU19 de extremo a extremo, con el servidor arrancado en un puerto libre. */
class RutasImagenesConsultaTest {

    private final HttpClient http = HttpClient.newHttpClient();
    private Javalin app;

    @BeforeEach
    void arrancarServidor() {
        ControladorImagenes controlador = new ControladorImagenes(
                new ServicioImagenesImpl(new ImagenRepositorioEnMemoria(), new SistemaPacsSimulado()));
        controlador.crearImagen(new DatosImagen("INF-001", "FICTICIO-001", "MED-001", "TAC",
                "craneo.dcm", new byte[] {1, 2, 3}));
        app = Servidor.crear(new RutasImagenes(controlador)).start(0);
    }

    @AfterEach
    void pararServidor() {
        app.stop();
    }

    @Test
    void listaLasImagenesDelPacienteSinElContenido() throws Exception {
        HttpResponse<String> respuesta = get("/pacientes/FICTICIO-001/imagenes", "MED-001");

        assertEquals(200, respuesta.statusCode());
        assertTrue(respuesta.body().contains("\"mensaje\":\"1 imagen(es) encontradas.\""));
        assertTrue(respuesta.body().contains("\"id\":\"IMG-001\""));
        assertTrue(respuesta.body().contains("\"nombreFichero\":\"craneo.dcm\""));
        assertFalse(respuesta.body().contains("contenido"), "El listado no debe llevar el contenido");
    }

    @Test
    void lasFechasViajanComoTexto() throws Exception {
        HttpResponse<String> respuesta = get("/pacientes/FICTICIO-001/imagenes", "MED-001");

        assertTrue(respuesta.body().matches(".*\"fechaCreacion\":\"\\d{4}-\\d{2}-\\d{2}T.*"), respuesta.body());
    }

    @Test
    void listarSinMedicoDevuelve400() throws Exception {
        HttpResponse<String> respuesta = get("/pacientes/FICTICIO-001/imagenes", null);

        assertEquals(400, respuesta.statusCode());
        assertTrue(respuesta.body().contains("El campo médico es obligatorio."));
    }

    @Test
    void consultaLaImagenConSuContenidoEnBase64() throws Exception {
        HttpResponse<String> respuesta = get("/imagenes/IMG-001", "MED-001");

        assertEquals(200, respuesta.statusCode());
        assertTrue(respuesta.body().contains("\"contenido\":\"AQID\""));
        assertTrue(respuesta.body().contains("\"referenciaPacs\":\"pacs://radiodiagnostico/FICTICIO-001/IMG-001\""));
    }

    @Test
    void consultarImagenDeOtroMedicoDevuelve400() throws Exception {
        HttpResponse<String> respuesta = get("/imagenes/IMG-001", "MED-002");

        assertEquals(400, respuesta.statusCode());
        assertTrue(respuesta.body().contains("no pertenece a un paciente de este médico"));
    }

    @Test
    void consultarImagenInexistenteDevuelve400() throws Exception {
        HttpResponse<String> respuesta = get("/imagenes/IMG-999", "MED-001");

        assertEquals(400, respuesta.statusCode());
        assertTrue(respuesta.body().contains("No existe la imagen IMG-999."));
    }

    private HttpResponse<String> get(String ruta, String medico) throws IOException, InterruptedException {
        HttpRequest.Builder peticion = HttpRequest.newBuilder(URI.create("http://localhost:" + app.port() + ruta)).GET();
        if (medico != null) {
            peticion.header(RutasImagenes.CABECERA_MEDICO, medico);
        }
        return http.send(peticion.build(), HttpResponse.BodyHandlers.ofString());
    }
}
