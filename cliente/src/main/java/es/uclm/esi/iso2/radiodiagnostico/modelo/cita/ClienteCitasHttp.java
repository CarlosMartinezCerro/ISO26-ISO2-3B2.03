package es.uclm.esi.iso2.radiodiagnostico.modelo.cita;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Objects;

/**
 * Cliente HTTP para consultar y gestionar citas mediante la API REST (D-009):
 * CdU25 (solicitudes pendientes y asignar cita) y CU1 (consultar citas).
 */
public class ClienteCitasHttp implements ClienteCitas {

    static final String RUTA_SOLICITUDES_PENDIENTES = "/solicitudes/pendientes";
    static final String RUTA_CITAS = "/citas";
    private static final Duration TIEMPO_MAXIMO = Duration.ofSeconds(10);

    private final String urlServidor;
    private final HttpClient http;

    /** Ignora los campos que el cliente no usa (por ejemplo el estado de la solicitud). */
    private final ObjectMapper json = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    /** @param urlServidor dirección base del servidor, por ejemplo {@code http://localhost:7070} */
    public ClienteCitasHttp(String urlServidor) {
        this(urlServidor, HttpClient.newBuilder()
                .connectTimeout(TIEMPO_MAXIMO)
                .build());
    }

    public ClienteCitasHttp(String urlServidor, HttpClient http) {
        this.urlServidor = Objects.requireNonNull(
                urlServidor, "La URL del servidor es obligatoria")
                .replaceAll("/+$", "");

        this.http = Objects.requireNonNull(
                http, "El cliente HTTP es obligatorio");
    }

    /** CU1 · Consultar citas. */
    @Override
    public List<CitaPaciente> consultarCitas() {
        try {
            URI uri = URI.create(urlServidor + RUTA_CITAS);

            HttpRequest peticion = HttpRequest.newBuilder(uri)
                    .timeout(TIEMPO_MAXIMO)
                    .GET()
                    .build();

            HttpResponse<String> respuesta = http.send(
                    peticion, HttpResponse.BodyHandlers.ofString());

            if (respuesta.statusCode() == 401) {
                throw new ErrorComunicacionException(
                        "El paciente no está autenticado.", null);
            }

            if (respuesta.statusCode() != 200) {
                throw new ErrorComunicacionException(
                        "El servidor respondió con el código "
                                + respuesta.statusCode(), null);
            }

            return json.readValue(
                    respuesta.body(),
                    new TypeReference<List<CitaPaciente>>() {
                    });

        } catch (IOException e) {
            throw new ErrorComunicacionException(
                    "No se pudieron consultar las citas.", e);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ErrorComunicacionException(
                    "Se interrumpió la consulta de citas.", e);
        }
    }

    /** CdU25 · Asignar cita solicitada: solicitudes que aún no tienen cita. */
    @Override
    public List<SolicitudPendiente> obtenerSolicitudesPendientes() {
        HttpRequest peticion = HttpRequest.newBuilder(URI.create(urlServidor + RUTA_SOLICITUDES_PENDIENTES))
                .timeout(TIEMPO_MAXIMO)
                .GET()
                .build();
        return List.of(enviar(peticion, SolicitudPendiente[].class));
    }

    /** CdU25 · Asignar cita solicitada. */
    @Override
    public ResultadoAsignacion asignarCita(DatosAsignacionCita datos) {
        HttpRequest peticion;
        try {
            peticion = HttpRequest.newBuilder(URI.create(urlServidor + RUTA_CITAS))
                    .timeout(TIEMPO_MAXIMO)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json.writeValueAsString(datos)))
                    .build();
        } catch (IOException e) {
            throw new ErrorComunicacionException("No se pudieron preparar los datos de la cita", e);
        }
        // El servidor responde con un ResultadoAsignacion tanto si asigna (201) como si rechaza (400).
        return enviar(peticion, ResultadoAsignacion.class);
    }

    private <T> T enviar(HttpRequest peticion, Class<T> tipo) {
        try {
            HttpResponse<String> respuesta = http.send(peticion, HttpResponse.BodyHandlers.ofString());
            return json.readValue(respuesta.body(), tipo);
        } catch (IOException e) {
            throw new ErrorComunicacionException("No se pudo contactar con el servidor", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ErrorComunicacionException("Se interrumpió la petición al servidor", e);
        }
    }
}
