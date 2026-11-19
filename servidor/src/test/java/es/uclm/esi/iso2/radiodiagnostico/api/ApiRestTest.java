package es.uclm.esi.iso2.radiodiagnostico.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.uclm.esi.iso2.radiodiagnostico.cita.CitaRepositorioEnMemoria;
import es.uclm.esi.iso2.radiodiagnostico.cita.ServicioCitasImpl;
import es.uclm.esi.iso2.radiodiagnostico.controlador.ControladorCitas;
import es.uclm.esi.iso2.radiodiagnostico.controlador.ControladorImagenes;
import es.uclm.esi.iso2.radiodiagnostico.informe.DatosImagen;
import es.uclm.esi.iso2.radiodiagnostico.informe.ImagenRepositorioEnMemoria;
import es.uclm.esi.iso2.radiodiagnostico.informe.ServicioImagenesImpl;
import es.uclm.esi.iso2.radiodiagnostico.informe.SistemaPacsSimulado;
import es.uclm.esi.iso2.radiodiagnostico.solicitud.EstadoSolicitud;
import es.uclm.esi.iso2.radiodiagnostico.solicitud.PrioridadSolicitud;
import es.uclm.esi.iso2.radiodiagnostico.solicitud.SolicitudPrueba;
import es.uclm.esi.iso2.radiodiagnostico.solicitud.SolicitudRepositorioEnMemoria;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tests de la API REST del CdU25, CdU19 y CdU17: comprueban el JSON de entrada y de salida. */
class ApiRestTest {

    private static final ZoneId ZONA = ZoneId.of("Europe/Madrid");
    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 11, 16, 10, 0);
    private static final ObjectMapper LECTOR = new ObjectMapper();

    private ApiRest api;
    private ControladorImagenes imagenes;

    @BeforeEach
    void crearApi() {
        Clock reloj = Clock.fixed(AHORA.atZone(ZONA).toInstant(), ZONA);
        SolicitudRepositorioEnMemoria solicitudes = new SolicitudRepositorioEnMemoria();
        solicitudes.guardar(new SolicitudPrueba("SOL-001", "FICTICIO-001", "Radiografía",
                PrioridadSolicitud.ORDINARIO, EstadoSolicitud.PENDIENTE));
        solicitudes.guardar(new SolicitudPrueba("SOL-002", "FICTICIO-002", "TAC",
                PrioridadSolicitud.URGENTE, EstadoSolicitud.PENDIENTE));
        ControladorCitas citas = new ControladorCitas(
                new ServicioCitasImpl(solicitudes, new CitaRepositorioEnMemoria(), reloj));
        imagenes = new ControladorImagenes(new ServicioImagenesImpl(
                new ImagenRepositorioEnMemoria(), new SistemaPacsSimulado(), reloj));
        api = new ApiRest(citas, imagenes);
    }

    @Test
    void listaLasSolicitudesPendientesPorPrioridad() throws Exception {
        JsonNode lista = leer(api.solicitudesPendientes());

        assertEquals(2, lista.size());
        assertEquals("SOL-002", lista.get(0).get("id").asText());
        assertEquals("URGENTE", lista.get(0).get("prioridad").asText());
        assertEquals("FICTICIO-002", lista.get(0).get("dniPaciente").asText());
    }

    @Test
    void asignaUnaCitaDesdeJson() throws Exception {
        JsonNode resultado = leer(api.asignarCita(
                "{\"idSolicitud\":\"SOL-001\",\"fechaHora\":\"2026-11-17T09:30:00\",\"sala\":\"SALA-RX-1\"}"));

        assertTrue(resultado.get("correcto").asBoolean());
        assertTrue(resultado.get("mensaje").asText().contains("SOL-001"));
        assertEquals(1, leer(api.solicitudesPendientes()).size());
    }

    @Test
    void asignarConDatosInvalidosDevuelveElMensajeDelControlador() throws Exception {
        JsonNode resultado = leer(api.asignarCita(
                "{\"idSolicitud\":\"SOL-001\",\"fechaHora\":\"2026-11-17T09:30:00\",\"sala\":\"\"}"));

        assertFalse(resultado.get("correcto").asBoolean());
        assertEquals("El campo sala es obligatorio.", resultado.get("mensaje").asText());
    }

    @Test
    void asignarConJsonMalFormadoDevuelveError() throws Exception {
        JsonNode resultado = leer(api.asignarCita("esto no es json"));

        assertFalse(resultado.get("correcto").asBoolean());
        assertEquals(ApiRest.MENSAJE_JSON_INVALIDO, resultado.get("mensaje").asText());
    }

    @Test
    void asignarConFechaMalEscritaDevuelveError() throws Exception {
        JsonNode resultado = leer(api.asignarCita(
                "{\"idSolicitud\":\"SOL-001\",\"fechaHora\":\"mañana\",\"sala\":\"SALA-RX-1\"}"));

        assertFalse(resultado.get("correcto").asBoolean());
        assertEquals(ApiRest.MENSAJE_JSON_INVALIDO, resultado.get("mensaje").asText());
    }

    @Test
    void creaUnaImagenConContenidoEnBase64() throws Exception {
        String contenido = Base64.getEncoder().encodeToString(new byte[] {1, 2, 3});
        JsonNode resultado = leer(api.crearImagen("{\"idInforme\":\"INF-001\",\"dniPaciente\":\"FICTICIO-001\","
                + "\"idMedico\":\"MED-001\",\"tipoPrueba\":\"TAC\",\"nombreFichero\":\"craneo.dcm\","
                + "\"contenido\":\"" + contenido + "\"}"));

        assertTrue(resultado.get("correcto").asBoolean());
        assertTrue(resultado.get("mensaje").asText().contains("IMG-001"));
    }

    @Test
    void crearImagenConJsonMalFormadoDevuelveError() throws Exception {
        JsonNode resultado = leer(api.crearImagen("{"));

        assertFalse(resultado.get("correcto").asBoolean());
        assertEquals(ApiRest.MENSAJE_JSON_INVALIDO, resultado.get("mensaje").asText());
    }

    @Test
    void elListadoDeImagenesNoIncluyeElContenido() throws Exception {
        crearImagen("MED-001");

        JsonNode resultado = leer(api.imagenesPaciente("MED-001", "FICTICIO-001"));

        assertTrue(resultado.get("correcto").asBoolean());
        assertEquals("1 imagen(es) encontradas.", resultado.get("mensaje").asText());
        JsonNode imagen = resultado.get("imagenes").get(0);
        assertEquals("IMG-001", imagen.get("id").asText());
        assertEquals("INF-001", imagen.get("idInforme").asText());
        assertEquals("2026-11-16T10:00", imagen.get("fechaCreacion").asText());
        assertEquals("craneo.dcm", imagen.get("nombreFichero").asText());
        assertFalse(imagen.has("contenido"));
    }

    @Test
    void listarSinMedicoDevuelveError() throws Exception {
        JsonNode resultado = leer(api.imagenesPaciente(null, "FICTICIO-001"));

        assertFalse(resultado.get("correcto").asBoolean());
        assertEquals("El campo médico es obligatorio.", resultado.get("mensaje").asText());
        assertEquals(0, resultado.get("imagenes").size());
    }

    @Test
    void consultarDevuelveLaImagenConSuContenidoEnBase64() throws Exception {
        crearImagen("MED-001");

        JsonNode resultado = leer(api.consultarImagen("MED-001", "IMG-001"));

        assertTrue(resultado.get("correcto").asBoolean());
        JsonNode imagen = resultado.get("imagen");
        assertEquals("FICTICIO-001", imagen.get("dniPaciente").asText());
        assertEquals("AQID", imagen.get("contenido").asText());
        assertEquals("pacs://radiodiagnostico/FICTICIO-001/IMG-001", imagen.get("referenciaPacs").asText());
    }

    @Test
    void consultarImagenDeOtroMedicoDevuelveError() throws Exception {
        crearImagen("MED-002");

        JsonNode resultado = leer(api.consultarImagen("MED-001", "IMG-001"));

        assertFalse(resultado.get("correcto").asBoolean());
        assertTrue(resultado.get("imagen").isNull());
    }

    private void crearImagen(String medico) {
        imagenes.crearImagen(new DatosImagen("INF-001", "FICTICIO-001", medico, "TAC",
                "craneo.dcm", new byte[] {1, 2, 3}));
    }

    private static JsonNode leer(String json) throws Exception {
        return LECTOR.readTree(json);
    }
}
