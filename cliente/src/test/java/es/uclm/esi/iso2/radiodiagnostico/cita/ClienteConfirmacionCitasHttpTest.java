package es.uclm.esi.iso2.radiodiagnostico.cita;

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

/** Prueba el cliente HTTP contra un servidor falso del JDK que imita la API del CdU2. */
class ClienteConfirmacionCitasHttpTest {

    private static final DatosConfirmacionCita DATOS =
            new DatosConfirmacionCita("CITA-SOL-001", "FICTICIO-001");

    private HttpServer servidor;
    private String rutaRecibida;
    private String metodoRecibido;
    private String cuerpoRecibido;
    private int codigoRespuesta;
    private String cuerpoRespuesta;

    @BeforeEach
    void arrancarServidorFalso() throws IOException {
        servidor = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        servidor.createContext("/citas", intercambio -> {
            metodoRecibido = intercambio.getRequestMethod();
            rutaRecibida = intercambio.getRequestURI().getRawPath();
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
    void enviaLaConfirmacionALaRutaDeLaCita() {
        codigoRespuesta = 200;
        cuerpoRespuesta = "{\"correcto\":true,\"mensaje\":\"Cita CITA-SOL-001 confirmada.\"}";

        ResultadoConfirmacion resultado = cliente().confirmarCita(DATOS);

        assertEquals(new ResultadoConfirmacion(true, "Cita CITA-SOL-001 confirmada."), resultado);
        assertEquals("POST", metodoRecibido);
        assertEquals("/citas/CITA-SOL-001/confirmacion", rutaRecibida);
        assertEquals("{\"idCita\":\"CITA-SOL-001\",\"dniPaciente\":\"FICTICIO-001\"}", cuerpoRecibido);
    }

    @Test
    void codificaCaracteresEspecialesDelId() {
        assertEquals("/citas/CITA%2F1%20A/confirmacion", ClienteConfirmacionCitasHttp.ruta("CITA/1 A"));
    }

    @Test
    void devuelveElRechazoDelServidor() {
        codigoRespuesta = 400;
        cuerpoRespuesta = "{\"correcto\":false,\"mensaje\":\"La cita ya está CONFIRMADA.\"}";

        ResultadoConfirmacion resultado = cliente().confirmarCita(DATOS);

        assertEquals(new ResultadoConfirmacion(false, "La cita ya está CONFIRMADA."), resultado);
    }

    @Test
    void servidorParadoEsErrorDeComunicacion() {
        String url = url();
        servidor.stop(0);

        assertThrows(ErrorComunicacionException.class,
                () -> new ClienteConfirmacionCitasHttp(url).confirmarCita(DATOS));
    }

    private ClienteConfirmacionCitasHttp cliente() {
        return new ClienteConfirmacionCitasHttp(url() + "/");
    }

    private String url() {
        return "http://localhost:" + servidor.getAddress().getPort();
    }
}
