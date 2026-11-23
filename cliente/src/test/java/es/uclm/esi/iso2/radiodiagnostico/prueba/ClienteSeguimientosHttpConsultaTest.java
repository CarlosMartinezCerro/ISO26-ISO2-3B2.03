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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Prueba la consulta del CdU11 (GET /seguimientos) que se añade a {@link ClienteSeguimientosHttp}
 * del CdU13, contra un servidor falso del JDK. La parte del CdU13 (POST) se prueba en
 * {@link ClienteSeguimientosHttpTest}.
 */
@DisplayName("CdU11 · Cliente HTTP de la consulta (GET /seguimientos)")
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

    // CP-H01 · Flujo principal (200)
    @Test
    @DisplayName("Hace GET con el DNI y convierte el JSON en seguimientos")
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

    // CP-H02 · Conjetura de errores: caracteres especiales en la URL
    @Test
    @DisplayName("Codifica en la URL los caracteres especiales del DNI")
    void codificaElDniEnLaUrl() {
        codigoRespuesta = 200;
        cuerpoRespuesta = "{\"correcto\":true,\"mensaje\":\"\",\"seguimientos\":[]}";

        cliente().consultarSeguimientos("12 34&x");

        assertEquals("dniPaciente=12+34%26x", consultaRecibida);
    }

    // CP-H03 · Rechazo del servidor (A1)
    @Test
    @DisplayName("Con 400 devuelve el rechazo del servidor con su mensaje")
    void devuelveElRechazoDelServidorCon400() {
        codigoRespuesta = 400;
        cuerpoRespuesta = "{\"correcto\":false,\"mensaje\":\"El DNI del paciente es obligatorio.\","
                + "\"seguimientos\":[]}";

        ResultadoConsultaSeguimientos resultado = cliente().consultarSeguimientos("");

        assertFalse(resultado.correcto());
        assertEquals("El DNI del paciente es obligatorio.", resultado.mensaje());
        assertTrue(resultado.seguimientos().isEmpty());
    }

    // CP-H04 · Códigos HTTP fuera de la clase válida {200, 400} (A3)
    @ParameterizedTest(name = "código {0} → error de comunicación")
    @ValueSource(ints = {201, 404, 500, 503})
    @DisplayName("Cualquier código distinto de 200 y 400 es un error de comunicación")
    void otroCodigoEsUnErrorDeComunicacion(int codigo) {
        codigoRespuesta = codigo;
        cuerpoRespuesta = "Error";

        assertThrows(ErrorComunicacionException.class,
                () -> cliente().consultarSeguimientos("12345678Z"));
    }

    // CP-H05 · Conjetura de errores: cuerpo no válido
    @Test
    @DisplayName("Una respuesta que no es JSON es un error de comunicación")
    void respuestaQueNoEsJsonEsUnErrorDeComunicacion() {
        codigoRespuesta = 200;
        cuerpoRespuesta = "<html>no es json</html>";

        assertThrows(ErrorComunicacionException.class,
                () -> cliente().consultarSeguimientos("12345678Z"));
    }

    // CP-H06 · Servidor no disponible (A3)
    @Test
    @DisplayName("Con el servidor parado se lanza un error de comunicación")
    void servidorParadoEsUnErrorDeComunicacion() {
        String url = "http://localhost:" + servidor.getAddress().getPort();
        servidor.stop(0);

        assertThrows(ErrorComunicacionException.class,
                () -> new ClienteSeguimientosHttp(url).consultarSeguimientos("12345678Z"));
    }

    // CP-H07 · Conjetura de errores: URL con barra final
    @Test
    @DisplayName("Admite la URL del servidor con barra final")
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
