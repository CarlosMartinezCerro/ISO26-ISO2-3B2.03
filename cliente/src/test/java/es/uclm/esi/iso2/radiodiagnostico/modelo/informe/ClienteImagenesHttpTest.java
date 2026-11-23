package es.uclm.esi.iso2.radiodiagnostico.modelo.informe;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Prueba el cliente HTTP contra un servidor falso del JDK que imita la API del CdU17. */
class ClienteImagenesHttpTest {

    private static final DatosImagen DATOS = new DatosImagen("INF-001", "FICTICIO-001",
            "MED-001", "Radiografía", "torax.dcm", new byte[] {1, 2, 3});

    private HttpServer servidor;
    private String cuerpoRecibido;
    private String metodoRecibido;
    private int codigoRespuesta;
    private String cuerpoRespuesta;

    @BeforeEach
    void arrancarServidorFalso() throws IOException {
        servidor = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        servidor.createContext(ClienteImagenesHttp.RUTA_IMAGENES, intercambio -> {
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
    void enviaLaImagenEnJsonConElContenidoEnBase64() {
        codigoRespuesta = 201;
        cuerpoRespuesta = "{\"correcto\":true,\"mensaje\":\"Imagen IMG-001 creada.\"}";

        ResultadoCreacionImagen resultado = cliente().crearImagen(DATOS);

        assertEquals(new ResultadoCreacionImagen(true, "Imagen IMG-001 creada."), resultado);
        assertEquals("POST", metodoRecibido);
        assertEquals("{\"idInforme\":\"INF-001\",\"dniPaciente\":\"FICTICIO-001\",\"idMedico\":\"MED-001\","
                + "\"tipoPrueba\":\"Radiografía\",\"nombreFichero\":\"torax.dcm\",\"contenido\":\"AQID\"}",
                cuerpoRecibido);
    }

    @Test
    void devuelveElRechazoDelServidor() {
        codigoRespuesta = 400;
        cuerpoRespuesta = "{\"correcto\":false,\"mensaje\":\"Formato no permitido.\"}";

        ResultadoCreacionImagen resultado = cliente().crearImagen(DATOS);

        assertEquals(new ResultadoCreacionImagen(false, "Formato no permitido."), resultado);
    }

    @Test
    void respuestaQueNoEsUnResultadoEsErrorDeComunicacion() {
        codigoRespuesta = 500;
        cuerpoRespuesta = "Internal server error";

        assertThrows(ErrorComunicacionException.class, () -> cliente().crearImagen(DATOS));
    }

    @Test
    void servidorParadoEsErrorDeComunicacion() {
        String url = url();
        servidor.stop(0);

        assertThrows(ErrorComunicacionException.class,
                () -> new ClienteImagenesHttp(url).crearImagen(DATOS));
    }

    private ClienteImagenesHttp cliente() {
        return new ClienteImagenesHttp(url() + "/");
    }

    private String url() {
        return "http://localhost:" + servidor.getAddress().getPort();
    }
}
