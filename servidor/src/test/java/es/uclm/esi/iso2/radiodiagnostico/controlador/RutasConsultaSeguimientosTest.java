package es.uclm.esi.iso2.radiodiagnostico.controlador;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.uclm.esi.iso2.radiodiagnostico.Servidor;
import es.uclm.esi.iso2.radiodiagnostico.prueba.CatalogoTiposPruebaSimulado;
import es.uclm.esi.iso2.radiodiagnostico.prueba.RegistroMedicosSimulado;
import es.uclm.esi.iso2.radiodiagnostico.prueba.ServicioConsultaSeguimientoImpl;
import es.uclm.esi.iso2.radiodiagnostico.prueba.ServicioSolicitudSeguimiento;
import es.uclm.esi.iso2.radiodiagnostico.prueba.ServicioSolicitudSeguimientoImpl;
import es.uclm.esi.iso2.radiodiagnostico.prueba.SolicitudSeguimientoRepositorioEnMemoria;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Prueba la API REST del CdU11 de extremo a extremo junto con la del CdU13: el médico registra
 * un seguimiento con POST /seguimientos y después se consulta con GET /seguimientos.
 */
@DisplayName("CdU11 · API REST GET /seguimientos")
class RutasConsultaSeguimientosTest {

    private static final ZoneId ZONA = ZoneId.of("Europe/Madrid");
    private static final LocalDate HOY = LocalDate.of(2026, 11, 18);

    private static final String SOLICITUD_VALIDA = """
            {"dniPaciente":"12345678Z","dniMedico":"00000001R","tipoPrueba":"TAC",
             "indicacionClinica":"Indicación ficticia","justificacion":"Justificación ficticia",
             "prioridad":"URGENTE","cada":2,"unidad":"SEMANAS","fechaInicio":"2026-11-20",
             "numeroRepeticiones":3}
            """;

    private final HttpClient http = HttpClient.newHttpClient();
    private Javalin app;

    @BeforeEach
    void arrancarServidor() {
        Clock reloj = Clock.fixed(HOY.atStartOfDay(ZONA).toInstant(), ZONA);
        // Igual que en Servidor.main: el CdU11 consulta la misma instancia que usa el CdU13.
        ServicioSolicitudSeguimiento seguimientos = new ServicioSolicitudSeguimientoImpl(
                new SolicitudSeguimientoRepositorioEnMemoria(), new CatalogoTiposPruebaSimulado(),
                new RegistroMedicosSimulado(), reloj);
        app = Servidor.crear(
                new RutasSolicitudSeguimiento(new ControladorSolicitudSeguimiento(seguimientos)),
                new RutasConsultaSeguimientos(new ControladorConsultaSeguimientos(
                        new ServicioConsultaSeguimientoImpl(seguimientos, reloj)))).start(0);
    }

    @AfterEach
    void pararServidor() {
        app.stop();
    }

    // CP-R01 · Flujo principal de extremo a extremo con el CdU13
    @Test
    @DisplayName("Devuelve 200 con los seguimientos registrados por el CdU13")
    void consultaLosSeguimientosRegistradosPorElCdU13() throws Exception {
        assertEquals(201, post(SOLICITUD_VALIDA).statusCode());

        HttpResponse<String> respuesta = get("?dniPaciente=12345678Z");

        assertEquals(200, respuesta.statusCode());
        String cuerpo = respuesta.body();
        assertTrue(cuerpo.contains("\"correcto\":true"), cuerpo);
        assertTrue(cuerpo.contains("\"tipoPrueba\":\"TAC\""), cuerpo);
        assertTrue(cuerpo.contains("\"periodicidad\":\"Cada 2 semanas\""), cuerpo);
        assertTrue(cuerpo.contains("\"proximaFecha\":\"2026-11-20\""), cuerpo);
        assertTrue(cuerpo.contains("\"fechaFin\":\"2026-12-18\""), cuerpo);
        assertTrue(cuerpo.contains("\"repeticionesTotales\":3"), cuerpo);
        assertTrue(cuerpo.contains("\"estado\":\"EN_CURSO\""), cuerpo);
    }

    // CP-R02 · Paciente sin seguimientos (A2)
    @Test
    @DisplayName("Un paciente sin seguimientos devuelve 200 con la lista vacía")
    void pacienteSinSeguimientosDevuelve200ConListaVacia() throws Exception {
        HttpResponse<String> respuesta = get("?dniPaciente=87654321X");

        assertEquals(200, respuesta.statusCode());
        assertTrue(respuesta.body().contains("\"correcto\":true"));
        assertTrue(respuesta.body().contains("\"seguimientos\":[]"));
        assertTrue(respuesta.body().contains("no tiene pruebas de seguimiento"));
    }

    // CP-R03 · DNI ausente, vacío o en blanco (A1): clase no válida
    @ParameterizedTest(name = "consulta [{0}] → 400")
    @ValueSource(strings = {"", "?dniPaciente=", "?dniPaciente=%20%20"})
    @DisplayName("Sin DNI, con DNI vacío o en blanco devuelve 400 con mensaje")
    void dniNoValidoDevuelve400(String consulta) throws Exception {
        HttpResponse<String> respuesta = get(consulta);

        assertEquals(400, respuesta.statusCode());
        assertTrue(respuesta.body().contains("\"correcto\":false"));
        assertTrue(respuesta.body().contains("El DNI del paciente es obligatorio."));
    }

    private HttpResponse<String> post(String cuerpo) throws IOException, InterruptedException {
        HttpRequest peticion = HttpRequest.newBuilder(uri(""))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(cuerpo))
                .build();
        return http.send(peticion, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> get(String consulta) throws IOException, InterruptedException {
        return http.send(HttpRequest.newBuilder(uri(consulta)).GET().build(),
                HttpResponse.BodyHandlers.ofString());
    }

    private URI uri(String consulta) {
        return URI.create("http://localhost:" + app.port()
                + RutasSolicitudSeguimiento.RUTA_SEGUIMIENTOS + consulta);
    }
}
