package es.uclm.esi.iso2.radiodiagnostico.modelo.cita;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Pruebas de ClienteCitasHttp: cada caso de uso que lo usa tiene su grupo de tests. */
class ClienteCitasHttpTest {

    /** CdU25: solicitudes pendientes y asignar cita, contra un servidor falso del JDK. */
    @Nested
    class AsignarCitaCdU25 {
        private static final DatosAsignacionCita DATOS =
                new DatosAsignacionCita("SOL-001", LocalDateTime.of(2026, 12, 1, 9, 30), "SALA-RX-1");

        private HttpServer servidor;
        private String rutaRecibida;
        private String metodoRecibido;
        private String cuerpoRecibido;
        private int codigoRespuesta;
        private String cuerpoRespuesta;

        @BeforeEach
        void arrancarServidorFalso() throws IOException {
            servidor = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
            servidor.createContext("/", intercambio -> {
                rutaRecibida = intercambio.getRequestURI().getPath();
                metodoRecibido = intercambio.getRequestMethod();
                cuerpoRecibido = new String(intercambio.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
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
        void pideLasSolicitudesPendientesIgnorandoLosCamposQueNoUsa() {
            codigoRespuesta = 200;
            cuerpoRespuesta = "[{\"id\":\"SOL-002\",\"dniPaciente\":\"FICTICIO-002\",\"tipoPrueba\":\"TAC\","
                    + "\"prioridad\":\"URGENTE\",\"estado\":\"PENDIENTE\"}]";

            List<SolicitudPendiente> solicitudes = cliente().obtenerSolicitudesPendientes();

            assertEquals("GET", metodoRecibido);
            assertEquals(ClienteCitasHttp.RUTA_SOLICITUDES_PENDIENTES, rutaRecibida);
            assertEquals(List.of(new SolicitudPendiente("SOL-002", "FICTICIO-002", "TAC", "URGENTE")), solicitudes);
        }

        @Test
        void enviaLaCitaEnJsonYDevuelveLaAsignacionCorrecta() {
            codigoRespuesta = 201;
            cuerpoRespuesta = "{\"correcto\":true,\"mensaje\":\"Cita asignada.\"}";

            ResultadoAsignacion resultado = cliente().asignarCita(DATOS);

            assertEquals(new ResultadoAsignacion(true, "Cita asignada."), resultado);
            assertEquals("POST", metodoRecibido);
            assertEquals(ClienteCitasHttp.RUTA_CITAS, rutaRecibida);
            assertEquals("{\"idSolicitud\":\"SOL-001\",\"fechaHora\":\"2026-12-01T09:30:00\","
                    + "\"sala\":\"SALA-RX-1\"}", cuerpoRecibido);
        }

        @Test
        void devuelveElRechazoDelServidor() {
            codigoRespuesta = 400;
            cuerpoRespuesta = "{\"correcto\":false,\"mensaje\":\"La sala ya está ocupada.\"}";

            assertEquals(new ResultadoAsignacion(false, "La sala ya está ocupada."), cliente().asignarCita(DATOS));
        }

        @Test
        void respuestaQueNoEsJsonLanzaErrorDeComunicacion() {
            codigoRespuesta = 404;
            cuerpoRespuesta = "Not Found";

            assertThrows(ErrorComunicacionException.class, () -> cliente().obtenerSolicitudesPendientes());
        }

        @Test
        void servidorCaidoLanzaErrorDeComunicacion() {
            String url = "http://localhost:" + servidor.getAddress().getPort();
            servidor.stop(0);

            assertThrows(ErrorComunicacionException.class, () -> new ClienteCitasHttp(url).asignarCita(DATOS));
        }

        @Test
        void hiloInterrumpidoLanzaErrorDeComunicacionYConservaLaInterrupcion() {
            codigoRespuesta = 200;
            cuerpoRespuesta = "[]";
            Thread.currentThread().interrupt();

            assertThrows(ErrorComunicacionException.class, () -> cliente().obtenerSolicitudesPendientes());
            assertEquals(true, Thread.interrupted());
        }

        private ClienteCitasHttp cliente() {
            return new ClienteCitasHttp("http://localhost:" + servidor.getAddress().getPort() + "/");
        }
    }

    /** CU1: consultar citas, contra un servidor falso del JDK. */
    @Nested
    class ConsultarCitasCU1 {
        private HttpServer servidor;
        private ClienteCitasHttp cliente;

        @BeforeEach
        void preparar() throws Exception {
            servidor = HttpServer.create(
                    new InetSocketAddress("localhost", 0), 0);

            servidor.start();

            cliente = new ClienteCitasHttp(
                    "http://localhost:" + servidor.getAddress().getPort());
        }

        @AfterEach
        void cerrar() {
            servidor.stop(0);
        }

        @Test
        void consultaYConvierteLasCitasRecibidas() {
            servidor.createContext("/citas", exchange -> {
                String respuesta = """
                        [
                          {
                            "id": "CITA-1",
                            "idSolicitud": "SOL-1",
                            "dniPaciente": "12345678A",
                            "tipoPrueba": "Radiografía",
                            "fechaHora": "2026-12-01T10:30:00",
                            "sala": "SALA-1",
                            "estado": "ASIGNADA"
                          }
                        ]
                        """;

                byte[] datos = respuesta.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set(
                        "Content-Type", "application/json");
                exchange.sendResponseHeaders(200, datos.length);
                exchange.getResponseBody().write(datos);
                exchange.close();
            });

            List<CitaPaciente> citas =
                    cliente.consultarCitas();

            assertEquals(1, citas.size());
            assertEquals("CITA-1", citas.get(0).id());
            assertEquals("Radiografía", citas.get(0).tipoPrueba());
            assertEquals("SALA-1", citas.get(0).sala());
            assertEquals("ASIGNADA", citas.get(0).estado());
            assertEquals(
                    LocalDateTime.of(2026, 12, 1, 10, 30),
                    citas.get(0).fechaHora());
        }

        @Test
        void respuestaSinCitasDevuelveListaVacia() {
            servidor.createContext("/citas", exchange -> {
                byte[] datos = "[]".getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(200, datos.length);
                exchange.getResponseBody().write(datos);
                exchange.close();
            });

            List<CitaPaciente> citas =
                    cliente.consultarCitas();

            assertTrue(citas.isEmpty());
        }

        @Test
        void respuestaHttpIncorrectaLanzaErrorDeComunicacion() {
            servidor.createContext("/citas", exchange -> {
                exchange.sendResponseHeaders(500, -1);
                exchange.close();
            });

            assertThrows(
                    ErrorComunicacionException.class,
                    () -> cliente.consultarCitas());
        }
    }
}
