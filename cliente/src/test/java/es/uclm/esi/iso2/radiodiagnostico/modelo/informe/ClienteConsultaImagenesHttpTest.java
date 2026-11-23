package es.uclm.esi.iso2.radiodiagnostico.modelo.informe;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Prueba el cliente HTTP contra un servidor falso del JDK que imita la API del CdU19. */
class ClienteConsultaImagenesHttpTest {

    private HttpServer servidor;
    private String rutaRecibida;
    private String medicoRecibido;
    private int codigoRespuesta;
    private String cuerpoRespuesta;

    @BeforeEach
    void arrancarServidorFalso() throws IOException {
        servidor = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        servidor.createContext("/", intercambio -> {
            rutaRecibida = intercambio.getRequestURI().getRawPath();
            medicoRecibido = intercambio.getRequestHeaders().getFirst(ClienteConsultaImagenesHttp.CABECERA_MEDICO);
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
    void pideLasImagenesDelPacienteConLaCabeceraDelMedico() {
        codigoRespuesta = 200;
        cuerpoRespuesta = "{\"correcto\":true,\"mensaje\":\"1 imagen(es) encontradas.\",\"imagenes\":["
                + "{\"id\":\"IMG-001\",\"idInforme\":\"INF-001\",\"tipoPrueba\":\"TAC\","
                + "\"fechaCreacion\":\"2026-11-02T09:30:00\",\"nombreFichero\":\"craneo.dcm\"}]}";

        RespuestaListadoImagenes respuesta = cliente().obtenerImagenesPaciente("MED-001", "FICTICIO-001");

        assertEquals("/pacientes/FICTICIO-001/imagenes", rutaRecibida);
        assertEquals("MED-001", medicoRecibido);
        assertTrue(respuesta.correcto());
        assertEquals(new ResumenImagen("IMG-001", "INF-001", "TAC",
                LocalDateTime.of(2026, 11, 2, 9, 30), "craneo.dcm"), respuesta.imagenes().get(0));
    }

    @Test
    void codificaElDniEnLaRuta() {
        codigoRespuesta = 200;
        cuerpoRespuesta = "{\"correcto\":true,\"mensaje\":\"\",\"imagenes\":[]}";

        cliente().obtenerImagenesPaciente("MED-001", "12345678 Z/Ñ");

        assertEquals("/pacientes/12345678%20Z%2F%C3%91/imagenes", rutaRecibida);
    }

    @Test
    void pideUnaImagenYDecodificaSuContenidoIgnorandoLosCamposQueNoUsa() {
        codigoRespuesta = 200;
        cuerpoRespuesta = "{\"correcto\":true,\"mensaje\":\"Imagen recuperada.\",\"imagen\":{"
                + "\"id\":\"IMG-001\",\"idInforme\":\"INF-001\",\"dniPaciente\":\"FICTICIO-001\","
                + "\"idMedico\":\"MED-001\",\"tipoPrueba\":\"TAC\",\"nombreFichero\":\"craneo.dcm\","
                + "\"contenido\":\"AQID\",\"fechaCreacion\":\"2026-11-02T09:30:00\","
                + "\"referenciaPacs\":\"pacs://radiodiagnostico/FICTICIO-001/IMG-001\"}}";

        RespuestaConsultaImagen respuesta = cliente().obtenerImagen("MED-001", "IMG-001");

        assertEquals("/imagenes/IMG-001", rutaRecibida);
        assertEquals("FICTICIO-001", respuesta.imagen().dniPaciente());
        assertArrayEquals(new byte[] {1, 2, 3}, respuesta.imagen().contenido());
    }

    @Test
    void devuelveElRechazoDelServidor() {
        codigoRespuesta = 400;
        cuerpoRespuesta = "{\"correcto\":false,\"mensaje\":\"No existe la imagen IMG-9.\",\"imagen\":null}";

        RespuestaConsultaImagen respuesta = cliente().obtenerImagen("MED-001", "IMG-9");

        assertEquals("No existe la imagen IMG-9.", respuesta.mensaje());
        assertNull(respuesta.imagen());
    }

    @Test
    void sinMedicoNoSeEnviaLaCabecera() {
        codigoRespuesta = 400;
        cuerpoRespuesta = "{\"correcto\":false,\"mensaje\":\"El campo médico es obligatorio.\",\"imagenes\":[]}";

        cliente().obtenerImagenesPaciente(null, "FICTICIO-001");

        assertNull(medicoRecibido);
    }

    @Test
    void respuestaQueNoEsJsonLanzaErrorDeComunicacion() {
        codigoRespuesta = 404;
        cuerpoRespuesta = "Not Found";

        assertThrows(ErrorComunicacionException.class, () -> cliente().obtenerImagen("MED-001", "IMG-001"));
    }

    @Test
    void servidorCaidoLanzaErrorDeComunicacion() {
        String url = "http://localhost:" + servidor.getAddress().getPort();
        servidor.stop(0);

        assertThrows(ErrorComunicacionException.class,
                () -> new ClienteConsultaImagenesHttp(url).obtenerImagenesPaciente("MED-001", "FICTICIO-001"));
    }

    @Test
    void hiloInterrumpidoLanzaErrorDeComunicacionYConservaLaInterrupcion() {
        codigoRespuesta = 200;
        cuerpoRespuesta = "{}";
        Thread.currentThread().interrupt();

        assertThrows(ErrorComunicacionException.class, () -> cliente().obtenerImagen("MED-001", "IMG-001"));
        assertTrue(Thread.interrupted());
    }

    private ClienteConsultaImagenesHttp cliente() {
        return new ClienteConsultaImagenesHttp("http://localhost:" + servidor.getAddress().getPort());
    }
}
