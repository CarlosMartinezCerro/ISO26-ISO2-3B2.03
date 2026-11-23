package es.uclm.esi.iso2.radiodiagnostico.controlador;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.uclm.esi.iso2.radiodiagnostico.Servidor;
import es.uclm.esi.iso2.radiodiagnostico.cita.Cita;
import es.uclm.esi.iso2.radiodiagnostico.cita.CitaRepositorioEnMemoria;
import es.uclm.esi.iso2.radiodiagnostico.cita.EstadoCita;
import es.uclm.esi.iso2.radiodiagnostico.cita.ServicioCitasImpl;
import es.uclm.esi.iso2.radiodiagnostico.paciente.PacienteRepositorioEnMemoria;
import es.uclm.esi.iso2.radiodiagnostico.paciente.ServicioPacientesImpl;
import es.uclm.esi.iso2.radiodiagnostico.solicitud.EstadoSolicitud;
import es.uclm.esi.iso2.radiodiagnostico.solicitud.PrioridadSolicitud;
import es.uclm.esi.iso2.radiodiagnostico.solicitud.SolicitudPrueba;
import es.uclm.esi.iso2.radiodiagnostico.solicitud.SolicitudRepositorioEnMemoria;
import io.javalin.Javalin;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Pruebas de RutasCitas: cada caso de uso que lo usa tiene su grupo de tests. */
class RutasCitasTest {

    /** CdU25: API REST de asignar cita, con el servidor arrancado en un puerto libre. */
    @Nested
    class AsignarCitaCdU25 {
        private final HttpClient http = HttpClient.newHttpClient();
        private SolicitudRepositorioEnMemoria solicitudes;
        private Javalin app;

        @BeforeEach
        void arrancarServidor() {
            solicitudes = new SolicitudRepositorioEnMemoria();
            solicitudes.guardar(new SolicitudPrueba("SOL-001", "FICTICIO-001", "Radiografía",
                    PrioridadSolicitud.ORDINARIO, EstadoSolicitud.PENDIENTE));
            solicitudes.guardar(new SolicitudPrueba("SOL-002", "FICTICIO-002", "TAC",
                    PrioridadSolicitud.URGENTE, EstadoSolicitud.PENDIENTE));
            app = Servidor.crear(new RutasCitas(new ControladorCitas(
                    new ServicioCitasImpl(solicitudes, new CitaRepositorioEnMemoria())))).start(0);
        }

        @AfterEach
        void pararServidor() {
            app.stop();
        }

        @Test
        void devuelveLasSolicitudesPendientesPorPrioridad() throws Exception {
            HttpResponse<String> respuesta = get(RutasCitas.RUTA_SOLICITUDES_PENDIENTES);

            assertEquals(200, respuesta.statusCode());
            assertTrue(respuesta.body().startsWith("[{\"id\":\"SOL-002\""), respuesta.body());
            assertTrue(respuesta.body().contains("\"prioridad\":\"URGENTE\""));
        }

        @Test
        void asignacionCorrectaDevuelve201YLaSolicitudDejaDeEstarPendiente() throws Exception {
            HttpResponse<String> respuesta = post(cita("SOL-001", "2099-01-01T09:30", "SALA-RX-1"));

            assertEquals(201, respuesta.statusCode());
            assertTrue(respuesta.body().contains("\"correcto\":true"));
            assertEquals(EstadoSolicitud.CITADA, solicitudes.buscarPorId("SOL-001").orElseThrow().estado());
        }

        @Test
        void datosInvalidosDevuelven400ConMensaje() throws Exception {
            HttpResponse<String> respuesta = post(cita("SOL-001", "2099-01-01T09:30", ""));

            assertEquals(400, respuesta.statusCode());
            assertTrue(respuesta.body().contains("El campo sala es obligatorio."));
        }

        @Test
        void solicitudYaCitadaDevuelve400() throws Exception {
            post(cita("SOL-001", "2099-01-01T09:30", "SALA-RX-1"));

            HttpResponse<String> respuesta = post(cita("SOL-001", "2099-01-02T09:30", "SALA-RX-1"));

            assertEquals(400, respuesta.statusCode());
            assertTrue(respuesta.body().contains("ya tiene una cita asignada"));
        }

        @Test
        void cuerpoMalFormadoDevuelve400() throws Exception {
            HttpResponse<String> respuesta = post("esto no es json");

            assertEquals(400, respuesta.statusCode());
            assertTrue(respuesta.body().contains(RutasCitas.PETICION_MAL_FORMADA));
        }

        @Test
        void fechaMalEscritaDevuelve400() throws Exception {
            HttpResponse<String> respuesta = post(cita("SOL-001", "mañana", "SALA-RX-1"));

            assertEquals(400, respuesta.statusCode());
            assertTrue(respuesta.body().contains(RutasCitas.PETICION_MAL_FORMADA));
        }

        private static String cita(String solicitud, String fechaHora, String sala) {
            return """
                    {"idSolicitud":"%s","fechaHora":"%s","sala":"%s"}
                    """.formatted(solicitud, fechaHora, sala);
        }

        private HttpResponse<String> get(String ruta) throws IOException, InterruptedException {
            return http.send(HttpRequest.newBuilder(url(ruta)).GET().build(), HttpResponse.BodyHandlers.ofString());
        }

        private HttpResponse<String> post(String cuerpo) throws IOException, InterruptedException {
            HttpRequest peticion = HttpRequest.newBuilder(url(RutasCitas.RUTA_CITAS))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(cuerpo))
                    .build();
            return http.send(peticion, HttpResponse.BodyHandlers.ofString());
        }

        private URI url(String ruta) {
            return URI.create("http://localhost:" + app.port() + ruta);
        }
    }

    /** CU1: consultar las citas del paciente con sesión iniciada. */
    @Nested
    class ConsultarCitasCU1 {
        @Test
        void consultarCitasDevuelveLasCitasDelPaciente() throws Exception {

            CitaRepositorioEnMemoria repositorio =
                    new CitaRepositorioEnMemoria();

            repositorio.guardar(new Cita(
                    "CITA-1", "SOL-1", "12345678A",
                    "Radiografía",
                    LocalDateTime.now().plusDays(1),
                    "SALA-1", EstadoCita.ASIGNADA
            ));

            repositorio.guardar(new Cita(
                    "CITA-2", "SOL-2", "87654321B",
                    "Resonancia",
                    LocalDateTime.now().plusDays(2),
                    "SALA-2", EstadoCita.ASIGNADA
            ));

            var servicioCitas = new ServicioCitasImpl(
                    new SolicitudRepositorioEnMemoria(),
                    repositorio
            );

            var app = Servidor.crear(
                    new ServicioPacientesImpl(
                            new PacienteRepositorioEnMemoria()),
                    servicioCitas
            );

            // Simulamos que el paciente ha iniciado sesión.
            app.before("/citas", ctx ->
                    ctx.sessionAttribute("dniPaciente", "12345678A"));

            app.start(0);

            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(
                                "http://localhost:" + app.port() + "/citas"))
                        .GET()
                        .build();

                HttpResponse<String> response =
                        HttpClient.newHttpClient().send(
                                request,
                                HttpResponse.BodyHandlers.ofString());

                assertEquals(200, response.statusCode());
                assertTrue(response.body().contains("CITA-1"));
                assertFalse(response.body().contains("CITA-2"));

            } finally {
                app.stop();
            }
        }

        @Test
        void consultarCitasSinSesionDevuelve401() throws Exception {

            var servicioCitas = new ServicioCitasImpl(
                    new SolicitudRepositorioEnMemoria(),
                    new CitaRepositorioEnMemoria()
            );

            var app = Servidor.crear(
                    new ServicioPacientesImpl(
                            new PacienteRepositorioEnMemoria()),
                    servicioCitas
            );

            // No configuramos ninguna sesión.
            app.start(0);

            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(
                                "http://localhost:" + app.port() + "/citas"))
                        .GET()
                        .build();

                HttpResponse<String> response =
                        HttpClient.newHttpClient().send(
                                request,
                                HttpResponse.BodyHandlers.ofString());

                assertEquals(401, response.statusCode());

            } finally {
                app.stop();
            }
        }
    }
}
