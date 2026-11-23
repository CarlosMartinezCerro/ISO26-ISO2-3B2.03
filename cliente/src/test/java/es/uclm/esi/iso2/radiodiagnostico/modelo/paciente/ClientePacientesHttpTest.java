package es.uclm.esi.iso2.radiodiagnostico.modelo.paciente;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Prueba el cliente HTTP contra un servidor falso del JDK que imita la API del CdU22. */
class ClientePacientesHttpTest {

    private static final DatosAltaPaciente DATOS = new DatosAltaPaciente("12345678Z", "Alex",
            "Ejemplo", LocalDate.of(1990, 5, 12), "600000001");

    private HttpServer servidor;
    private String cuerpoRecibido;
    private String metodoRecibido;
    private int codigoRespuesta;
    private String cuerpoRespuesta;

    @BeforeEach
    void arrancarServidorFalso() throws IOException {
        servidor = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        servidor.createContext(ClientePacientesHttp.RUTA_PACIENTES, intercambio -> {
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
    void enviaLosDatosEnJsonYDevuelveElAltaCorrecta() {
        codigoRespuesta = 201;
        cuerpoRespuesta = "{\"correcto\":true,\"mensaje\":\"Paciente dado de alta.\"}";

        ResultadoAlta resultado = cliente().darDeAlta(DATOS);

        assertEquals(new ResultadoAlta(true, "Paciente dado de alta."), resultado);
        assertEquals("POST", metodoRecibido);
        assertEquals("{\"dni\":\"12345678Z\",\"nombre\":\"Alex\",\"apellidos\":\"Ejemplo\","
                + "\"fechaNacimiento\":\"1990-05-12\",\"telefono\":\"600000001\"}", cuerpoRecibido);
    }

    @Test
    void devuelveElRechazoDelServidor() {
        codigoRespuesta = 400;
        cuerpoRespuesta = "{\"correcto\":false,\"mensaje\":\"La letra del campo dni no es correcta.\"}";

        ResultadoAlta resultado = cliente().darDeAlta(DATOS);

        assertEquals(new ResultadoAlta(false, "La letra del campo dni no es correcta."), resultado);
    }

    @Test
    void respuestaQueNoEsUnResultadoEsErrorDeComunicacion() {
        codigoRespuesta = 500;
        cuerpoRespuesta = "Internal server error";

        assertThrows(ErrorComunicacionException.class, () -> cliente().darDeAlta(DATOS));
    }

    @Test
    void servidorParadoEsErrorDeComunicacion() {
        String url = url();
        servidor.stop(0);

        assertThrows(ErrorComunicacionException.class,
                () -> new ClientePacientesHttp(url).darDeAlta(DATOS));
    }

    private ClientePacientesHttp cliente() {
        return new ClientePacientesHttp(url() + "/");
    }

    private String url() {
        return "http://localhost:" + servidor.getAddress().getPort();
    }
}
