package es.uclm.esi.iso2.radiodiagnostico.controlador;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import es.uclm.esi.iso2.radiodiagnostico.Servidor;
import es.uclm.esi.iso2.radiodiagnostico.informe.DatosInformeInvalidosException;
import es.uclm.esi.iso2.radiodiagnostico.informe.DniPacienteInvalidoException;
import es.uclm.esi.iso2.radiodiagnostico.informe.HistorialClinicoNoDisponibleException;
import es.uclm.esi.iso2.radiodiagnostico.informe.Informe;
import es.uclm.esi.iso2.radiodiagnostico.informe.InformeNoEncontradoException;
import io.javalin.Javalin;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class RutasInformeErroresTest {

    private final HttpClient http = HttpClient.newHttpClient();
    private ControladorInforme controlador;
    private Javalin app;

    @BeforeEach
    void arrancarServidor() {
        controlador = Mockito.mock(ControladorInforme.class);
        app = Servidor.crear(new RutasInforme(controlador)).start(0);
    }

    @AfterEach
    void pararServidor() {
        app.stop();
    }

    @Test
    void rechazaJsonMalFormadoYDatosObligatorios() throws Exception {
        assertEquals(400, post("no-json").statusCode());
        HttpResponse<String> respuesta = post("""
                {"dniPaciente":"87654321B","especialidad":"Radiologia"}
                """);

        assertEquals(400, respuesta.statusCode());
        assertTrue(respuesta.body().contains("son obligatorios"));
    }

    @Test
    void devuelve201CuandoElControladorCreaElInforme() throws Exception {
        Informe informe = informe();
        when(controlador.crearInforme(Mockito.any(Informe.class))).thenReturn(informe.conId(8));

        HttpResponse<String> respuesta = post("""
                {"dniPaciente":"87654321B","especialidad":"Radiologia",
                 "consultaActual":"Consulta","diagnostico":"Diagnostico",
                 "fecha":"2026-11-24"}
                """);

        assertEquals(201, respuesta.statusCode());
        assertTrue(respuesta.body().contains("\"id\":8"));
    }

    @Test
    void convierteErroresDeCreacionEn400Y503() throws Exception {
        doThrow(new DatosInformeInvalidosException("datos incorrectos"))
                .when(controlador).crearInforme(Mockito.any(Informe.class));
        assertEquals(400, post(informeJson()).statusCode());

        doThrow(new HistorialClinicoNoDisponibleException("caido"))
                .when(controlador).crearInforme(Mockito.any(Informe.class));
        HttpResponse<String> respuesta = post(informeJson());
        assertEquals(503, respuesta.statusCode());
        assertTrue(respuesta.body().contains("sistema externo"));
    }

    @Test
    void validaParametrosDeAntecedentes() throws Exception {
        assertEquals(400, get(RutasInforme.RUTA_ANTECEDENTES
                + "?dniPaciente=87654321B").statusCode());
        when(controlador.obtenerAntecedentes(Mockito.any(Informe.class)))
                .thenReturn(informe().conAntecedentes("A", "B"));

        HttpResponse<String> respuesta = get(RutasInforme.RUTA_ANTECEDENTES
                + "?dniPaciente=87654321B&especialidad=Radiologia");
        assertEquals(200, respuesta.statusCode());
        assertTrue(respuesta.body().contains("\"antecedentes\":\"A\""));
    }

    @Test
    void convierteErroresDeAntecedentesEn400Y503() throws Exception {
        doThrow(new DniPacienteInvalidoException("dni incorrecto"))
                .when(controlador).obtenerAntecedentes(Mockito.any(Informe.class));
        assertEquals(400, get(RutasInforme.RUTA_ANTECEDENTES
                + "?dniPaciente=87654321B&especialidad=Radiologia").statusCode());

        doThrow(new HistorialClinicoNoDisponibleException("caido"))
                .when(controlador).obtenerAntecedentes(Mockito.any(Informe.class));
        assertEquals(503, get(RutasInforme.RUTA_ANTECEDENTES
                + "?dniPaciente=87654321B&especialidad=Radiologia").statusCode());
    }

    @Test
    void listaInformesYValidaElDni() throws Exception {
        assertEquals(400, get(RutasInforme.RUTA_INFORMES).statusCode());
        when(controlador.listarInformes("87654321B")).thenReturn(java.util.List.of());

        assertEquals(200, get(RutasInforme.RUTA_INFORMES
                + "?dniPaciente=87654321B").statusCode());
    }

    @Test
    void consultaInformesYGestionaIdsInvalidos() throws Exception {
        assertEquals(400, get(RutasInforme.RUTA_INFORMES + "/abc").statusCode());
        assertEquals(400, get(RutasInforme.RUTA_INFORMES + "/0").statusCode());

        when(controlador.obtenerInforme(8)).thenReturn(informe().conId(8));
        assertEquals(200, get(RutasInforme.RUTA_INFORMES + "/8").statusCode());

        doThrow(new InformeNoEncontradoException(99))
                .when(controlador).obtenerInforme(99);
        assertEquals(404, get(RutasInforme.RUTA_INFORMES + "/99").statusCode());
    }

    @Test
    void rechazaControladorNulo() {
        assertTrue(Mockito.mockingDetails(controlador).isMock());
        org.junit.jupiter.api.Assertions.assertThrows(
                NullPointerException.class, () -> new RutasInforme(null));
    }

    private Informe informe() {
        return new Informe("12345678A", "87654321B", "Radiologia", "Dolor",
                "Consulta", "Diagnostico", "Pruebas", "Tratamiento",
                false, LocalDate.of(2026, 11, 24), "Anamnesis");
    }

    private String informeJson() {
        return """
                {"dniPaciente":"87654321B","especialidad":"Radiologia",
                 "consultaActual":"Consulta","diagnostico":"Diagnostico",
                 "fecha":"2026-11-24"}
                """;
    }

    private HttpResponse<String> post(String body) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + app.port() + RutasInforme.RUTA_INFORMES))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        return http.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> get(String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + app.port() + path))
                .GET()
                .build();
        return http.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
