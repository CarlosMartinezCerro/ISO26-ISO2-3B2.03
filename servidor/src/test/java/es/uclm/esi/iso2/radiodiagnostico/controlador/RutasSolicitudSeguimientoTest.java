package es.uclm.esi.iso2.radiodiagnostico.controlador;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.uclm.esi.iso2.radiodiagnostico.Servidor;
import es.uclm.esi.iso2.radiodiagnostico.modelo.prueba.CatalogoTiposPruebaSimulado;
import es.uclm.esi.iso2.radiodiagnostico.modelo.prueba.RegistroMedicosSimulado;
import es.uclm.esi.iso2.radiodiagnostico.modelo.prueba.ServicioSolicitudSeguimiento;
import es.uclm.esi.iso2.radiodiagnostico.modelo.prueba.ServicioSolicitudSeguimientoImpl;
import es.uclm.esi.iso2.radiodiagnostico.modelo.prueba.SolicitudSeguimientoRepositorioEnMemoria;
import io.javalin.Javalin;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Prueba la API REST del CdU13 de extremo a extremo, con el servidor en un puerto libre. */
class RutasSolicitudSeguimientoTest {

    private static final ZoneId ZONA = ZoneId.of("Europe/Madrid");
    private static final LocalDate HOY = LocalDate.of(2026, 11, 18);

    private static final String SOLICITUD_VALIDA = """
            {"dniPaciente":"12345678Z","dniMedico":"00000001R","tipoPrueba":"TAC",
             "indicacionClinica":"Indicación ficticia","justificacion":"Justificación ficticia",
             "prioridad":"URGENTE","cada":2,"unidad":"SEMANAS","fechaInicio":"2026-11-20",
             "numeroRepeticiones":3}
            """;

    private final HttpClient http = HttpClient.newHttpClient();
    private ServicioSolicitudSeguimiento servicio;
    private Javalin app;

    @BeforeEach
    void arrancarServidor() {
        Clock reloj = Clock.fixed(HOY.atStartOfDay(ZONA).toInstant(), ZONA);
        servicio = new ServicioSolicitudSeguimientoImpl(new SolicitudSeguimientoRepositorioEnMemoria(),
                new CatalogoTiposPruebaSimulado(), new RegistroMedicosSimulado(), reloj);
        app = Servidor.crear(new RutasSolicitudSeguimiento(
                new ControladorSolicitudSeguimiento(servicio))).start(0);
    }

    @AfterEach
    void pararServidor() {
        app.stop();
    }

    @Test
    void solicitudCorrectaDevuelve201ConLasRepeticiones() throws Exception {
        HttpResponse<String> respuesta = post(SOLICITUD_VALIDA);

        assertEquals(201, respuesta.statusCode());
        assertTrue(respuesta.body().contains("\"correcto\":true"));
        assertTrue(respuesta.body().contains("\"fechaPrevista\":\"2026-12-04\""));
        assertTrue(respuesta.body().contains("\"estado\":\"PENDIENTE_CITA\""));
        assertEquals(1, servicio.buscarPorPaciente("12345678Z").size());
    }

    @Test
    void usuarioNoMedicoDevuelve400ConMensaje() throws Exception {
        HttpResponse<String> respuesta = post(SOLICITUD_VALIDA.replace("00000001R", "99999999R"));

        assertEquals(400, respuesta.statusCode());
        assertTrue(respuesta.body().contains("\"correcto\":false"));
        assertTrue(respuesta.body().contains("Solo un médico puede solicitar"));
        assertTrue(servicio.buscarPorPaciente("12345678Z").isEmpty());
    }

    @Test
    void ambosCriteriosDeFinDevuelven400() throws Exception {
        HttpResponse<String> respuesta = post(SOLICITUD_VALIDA.replace(
                "\"numeroRepeticiones\":3", "\"numeroRepeticiones\":3,\"fechaLimite\":\"2027-06-01\""));

        assertEquals(400, respuesta.statusCode());
        assertTrue(respuesta.body().contains("Indique exactamente un criterio de fin"));
    }

    @Test
    void cuerpoMalFormadoDevuelve400() throws Exception {
        HttpResponse<String> respuesta = post("esto no es json");

        assertEquals(400, respuesta.statusCode());
        assertTrue(respuesta.body().contains(RutasSolicitudSeguimiento.PETICION_MAL_FORMADA));
    }

    @Test
    void unidadDesconocidaDevuelve400() throws Exception {
        HttpResponse<String> respuesta = post(SOLICITUD_VALIDA.replace("SEMANAS", "AÑOS"));

        assertEquals(400, respuesta.statusCode());
        assertTrue(respuesta.body().contains(RutasSolicitudSeguimiento.PETICION_MAL_FORMADA));
    }

    private HttpResponse<String> post(String cuerpo) throws IOException, InterruptedException {
        HttpRequest peticion = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + app.port()
                        + RutasSolicitudSeguimiento.RUTA_SEGUIMIENTOS))
                .header("Content-Type", "application/json; charset=utf-8")
                .POST(HttpRequest.BodyPublishers.ofString(cuerpo))
                .build();
        return http.send(peticion, HttpResponse.BodyHandlers.ofString());
    }
}
