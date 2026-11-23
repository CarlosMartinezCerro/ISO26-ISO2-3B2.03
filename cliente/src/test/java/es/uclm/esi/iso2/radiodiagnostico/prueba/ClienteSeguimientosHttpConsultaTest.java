package es.uclm.esi.iso2.radiodiagnostico.prueba;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Prueba la consulta del CdU11 (GET /seguimientos) que se añade a {@link ClienteSeguimientosHttp}
 * del CdU13, contra un servidor falso del JDK. La parte del CdU13 (POST) se prueba en
 * {@link ClienteSeguimientosHttpTest}.
 */
class ClienteSeguimientosHttpConsultaTest {

    private static final String RESPUESTA_CON_SEGUIMIENTO = """
            {"correcto":true,"mensaje":"Se ha encontrado 1 prueba de seguimiento periódica.",
             "seguimientos":[{"idSolicitud":"SEG-1","dniPaciente":"12345678Z",
               "dniMedico":"00000001R","tipoPrueba":"TAC","indicacionClinica":"Indicación ficticia",
               "prioridad":"URGENTE","periodicidad":"Cada 2 semanas","fechaInicio":"2026-11-20",
               "fechaFin":"2026-12-04","repeticionesTotales":2,"repeticionesCitadas":1,
               "proximaFecha":"2026-12-04","estado":"EN_CURSO","campoNuevo":"se ignora",
               "repeticiones":[
                 {"id":"SEG-1-1","idSolicitudSeguimiento":"SEG-1","numero":1,
                  "fechaPrevista":"2026-11-20","estado":"CITADA"},
                 {"id":"SEG-1-2","idSolicitudSeguimiento":"SEG-1","numero":2,
                  "fechaPrevista":"2026-12-04","estado":"PENDIENTE_CITA"}]}]}
            """;

    private HttpServer servidor;
    private String metodoRecibido;
    private String consultaRecibida;
    private int codigoRespuesta;
    private String cuerpoRespuesta;

    @BeforeEach
    void arrancarServidorFalso() throws IOException {
        servidor = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        servidor.createContext(ClienteSeguimientosHttp.RUTA_SEGUIMIENTOS, intercambio -> {
            metodoRecibido = intercambio.getRequestMethod();
            consultaRecibida = intercambio.getRequestURI().getRawQuery();
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
    void consultaPorDniYConvierteElJsonEnSeguimientos() {
        codigoRespuesta = 200;
        cuerpoRespuesta = RESPUESTA_CON_SEGUIMIENTO;

        ResultadoConsultaSeguimientos resultado = cliente().consultarSeguimientos("12345678Z");

        assertEquals("GET", metodoRecibido);
        assertEquals("dniPaciente=12345678Z", consultaRecibida);
        assertTrue(resultado.correcto());
        assertEquals(1, resultado.seguimientos().size());
        SeguimientoPeriodico s = resultado.seguimientos().get(0);
        assertEquals("TAC", s.tipoPrueba());
        assertEquals(PrioridadSeguimiento.URGENTE, s.prioridad());
        assertEquals("Cada 2 semanas", s.periodicidad());
        assertEquals(LocalDate.of(2026, 12, 4), s.proximaFecha());
        assertEquals(1, s.repeticionesCitadas());
        assertEquals(List.of(
                new RepeticionSeguimiento("SEG-1-1", "SEG-1", 1, LocalDate.of(2026, 11, 20), "CITADA"),
                new RepeticionSeguimiento("SEG-1-2", "SEG-1", 2, LocalDate.of(2026, 12, 4),
                        "PENDIENTE_CITA")), s.repeticiones());
    }

    @Test
    void codificaElDniEnLaUrl() {
        codigoRespuesta = 200;
        cuerpoRespuesta = "{\"correcto\":true,\"mensaje\":\"\",\"seguimientos\":[]}";

        cliente().consultarSeguimientos("12 34&x");

        assertEquals("dniPaciente=12+34%26x", consultaRecibida);
    }

    @Test
    void devuelveElRechazoDelServidorCon400() {
        codigoRespuesta = 400;
        cuerpoRespuesta = "{\"correcto\":false,\"mensaje\":\"El DNI del paciente es obligatorio.\","
                + "\"seguimientos\":[]}";

        ResultadoConsultaSeguimientos resultado = cliente().consultarSeguimientos("");

        assertFalse(resultado.correcto());
        assertEquals("El DNI del paciente es obligatorio.", resultado.mensaje());
        assertTrue(resultado.seguimientos().isEmpty());
    }

    @Test
    void otroCodigoEsUnErrorDeComunicacion() {
        codigoRespuesta = 500;
        cuerpoRespuesta = "Error interno";

        assertThrows(ErrorComunicacionException.class,
                () -> cliente().consultarSeguimientos("12345678Z"));
    }

    @Test
    void respuestaQueNoEsJsonEsUnErrorDeComunicacion() {
        codigoRespuesta = 200;
        cuerpoRespuesta = "<html>no es json</html>";

        assertThrows(ErrorComunicacionException.class,
                () -> cliente().consultarSeguimientos("12345678Z"));
    }

    @Test
    void servidorParadoEsUnErrorDeComunicacion() {
        String url = "http://localhost:" + servidor.getAddress().getPort();
        servidor.stop(0);

        assertThrows(ErrorComunicacionException.class,
                () -> new ClienteSeguimientosHttp(url).consultarSeguimientos("12345678Z"));
    }

    @Test
    void quitaLaBarraFinalDeLaUrl() {
        codigoRespuesta = 200;
        cuerpoRespuesta = "{\"correcto\":true,\"mensaje\":\"\",\"seguimientos\":[]}";

        new ClienteSeguimientosHttp("http://localhost:" + servidor.getAddress().getPort() + "/")
                .consultarSeguimientos("12345678Z");

        assertEquals("GET", metodoRecibido);
    }

    private ClienteSeguimientosHttp cliente() {
        return new ClienteSeguimientosHttp("http://localhost:" + servidor.getAddress().getPort());
    }
}
