package es.uclm.esi.iso2.radiodiagnostico.controlador;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.uclm.esi.iso2.radiodiagnostico.Servidor;
import es.uclm.esi.iso2.radiodiagnostico.informe.Imagen;
import es.uclm.esi.iso2.radiodiagnostico.informe.ImagenRepositorioEnMemoria;
import es.uclm.esi.iso2.radiodiagnostico.informe.ServicioImagenesImpl;
import es.uclm.esi.iso2.radiodiagnostico.informe.SistemaPacsSimulado;
import io.javalin.Javalin;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Prueba la API REST del CdU17 de extremo a extremo, con el servidor arrancado en un puerto libre. */
class RutasImagenesTest {

    private static final byte[] CONTENIDO = {1, 2, 3};

    private final HttpClient http = HttpClient.newHttpClient();
    private ImagenRepositorioEnMemoria imagenes;
    private Javalin app;

    @BeforeEach
    void arrancarServidor() {
        imagenes = new ImagenRepositorioEnMemoria();
        app = Servidor.crear(new RutasImagenes(new ControladorImagenes(
                new ServicioImagenesImpl(imagenes, new SistemaPacsSimulado())))).start(0);
    }

    @AfterEach
    void pararServidor() {
        app.stop();
    }

    private static String imagen(String tipoPrueba, String fichero, byte[] contenido) {
        return """
                {"idInforme":"INF-001","dniPaciente":"FICTICIO-001","idMedico":"MED-001",
                 "tipoPrueba":"%s","nombreFichero":"%s","contenido":"%s"}
                """.formatted(tipoPrueba, fichero, Base64.getEncoder().encodeToString(contenido));
    }

    @Test
    void creacionCorrectaDevuelve201YGuardaLaImagenConSuContenido() throws Exception {
        HttpResponse<String> respuesta = post(imagen("Radiografía", "torax.dcm", CONTENIDO));

        assertEquals(201, respuesta.statusCode());
        assertTrue(respuesta.body().contains("\"correcto\":true"));
        Imagen guardada = imagenes.buscarPorId("IMG-001").orElseThrow();
        assertEquals("torax.dcm", guardada.nombreFichero());
        assertEquals(3, guardada.contenido().length);
        assertEquals(2, guardada.contenido()[1]);
    }

    @Test
    void datosInvalidosDevuelven400ConMensaje() throws Exception {
        HttpResponse<String> respuesta = post(imagen("", "torax.dcm", CONTENIDO));

        assertEquals(400, respuesta.statusCode());
        assertTrue(respuesta.body().contains("\"correcto\":false"));
        assertTrue(respuesta.body().contains("El campo tipo de prueba es obligatorio."));
    }

    @Test
    void formatoNoPermitidoDevuelve400() throws Exception {
        HttpResponse<String> respuesta = post(imagen("Radiografía", "informe.pdf", CONTENIDO));

        assertEquals(400, respuesta.statusCode());
        assertTrue(respuesta.body().contains("Formato no permitido"));
        assertTrue(imagenes.buscarPorInforme("INF-001").isEmpty());
    }

    @Test
    void cuerpoMalFormadoDevuelve400() throws Exception {
        HttpResponse<String> respuesta = post("esto no es json");

        assertEquals(400, respuesta.statusCode());
        assertTrue(respuesta.body().contains(RutasImagenes.PETICION_MAL_FORMADA));
    }

    @Test
    void admiteImagenesDeMasDeUnMegabyte() throws Exception {
        HttpResponse<String> respuesta = post(imagen("TAC", "craneo.dcm", new byte[3 * 1024 * 1024]));

        assertEquals(201, respuesta.statusCode());
    }

    private HttpResponse<String> post(String cuerpo) throws IOException, InterruptedException {
        HttpRequest peticion = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + app.port() + RutasImagenes.RUTA_IMAGENES))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(cuerpo))
                .build();
        return http.send(peticion, HttpResponse.BodyHandlers.ofString());
    }
}
