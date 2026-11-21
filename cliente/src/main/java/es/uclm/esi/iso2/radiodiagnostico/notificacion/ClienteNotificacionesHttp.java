package es.uclm.esi.iso2.radiodiagnostico.notificacion;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Objects;

/** Implementación de {@link ClienteNotificaciones} sobre la API REST del servidor (D-009). CdU5. */
public class ClienteNotificacionesHttp implements ClienteNotificaciones {

    static final String RUTA_NOTIFICACIONES = "/notificaciones";
    private static final Duration TIEMPO_MAXIMO = Duration.ofSeconds(10);

    private final String urlServidor;
    private final HttpClient http;
    private final ObjectMapper json = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    /** @param urlServidor dirección base del servidor, por ejemplo {@code http://localhost:7070} */
    public ClienteNotificacionesHttp(String urlServidor) {
        this(urlServidor, HttpClient.newBuilder().connectTimeout(TIEMPO_MAXIMO).build());
    }

    public ClienteNotificacionesHttp(String urlServidor, HttpClient http) {
        Objects.requireNonNull(urlServidor, "La URL del servidor es obligatoria");
        this.urlServidor = urlServidor.replaceAll("/+$", "");
        this.http = Objects.requireNonNull(http, "El cliente HTTP es obligatorio");
    }

    @Override
    public ResultadoNotificacion notificar(DatosNotificacion datos) {
        HttpRequest peticion;
        try {
            peticion = HttpRequest.newBuilder(URI.create(urlServidor + RUTA_NOTIFICACIONES))
                    .timeout(TIEMPO_MAXIMO)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json.writeValueAsString(datos)))
                    .build();
        } catch (IOException e) {
            throw new ErrorComunicacionException("No se pudieron preparar los datos de la notificación", e);
        }
        // El servidor responde con un ResultadoNotificacion tanto si envía (201) como si rechaza (400).
        return enviar(peticion, ResultadoNotificacion.class);
    }

    @Override
    public RespuestaListadoNotificaciones historialPaciente(String dniPaciente) {
        HttpRequest peticion = HttpRequest.newBuilder(
                        URI.create(urlServidor + "/pacientes/" + segmento(dniPaciente) + RUTA_NOTIFICACIONES))
                .timeout(TIEMPO_MAXIMO)
                .GET()
                .build();
        return enviar(peticion, RespuestaListadoNotificaciones.class);
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

    /** Codifica el DNI para ponerlo en la URL (espacios, caracteres raros...). */
    static String segmento(String valor) {
        return URLEncoder.encode(valor, StandardCharsets.UTF_8).replace("+", "%20");
    }
}
