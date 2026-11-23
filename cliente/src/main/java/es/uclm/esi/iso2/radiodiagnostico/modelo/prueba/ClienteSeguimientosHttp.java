package es.uclm.esi.iso2.radiodiagnostico.modelo.prueba;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Objects;

/** Implementación de {@link ClienteSeguimientos} sobre la API REST del servidor (D-009). */
public class ClienteSeguimientosHttp implements ClienteSeguimientos {

    static final String RUTA_SEGUIMIENTOS = "/seguimientos";
    private static final Duration TIEMPO_MAXIMO = Duration.ofSeconds(10);

    private final URI urlSeguimientos;
    private final HttpClient http;
    private final ObjectMapper json = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    /** @param urlServidor dirección base del servidor, por ejemplo {@code http://localhost:7070} */
    public ClienteSeguimientosHttp(String urlServidor) {
        this(urlServidor, HttpClient.newBuilder().connectTimeout(TIEMPO_MAXIMO).build());
    }

    public ClienteSeguimientosHttp(String urlServidor, HttpClient http) {
        Objects.requireNonNull(urlServidor, "La URL del servidor es obligatoria");
        this.urlSeguimientos = URI.create(urlServidor.replaceAll("/+$", "") + RUTA_SEGUIMIENTOS);
        this.http = Objects.requireNonNull(http, "El cliente HTTP es obligatorio");
    }

    @Override
    public ResultadoSolicitudSeguimiento solicitar(DatosSolicitudSeguimiento datos) {
        try {
            HttpRequest peticion = HttpRequest.newBuilder(urlSeguimientos)
                    .timeout(TIEMPO_MAXIMO)
                    .header("Content-Type", "application/json; charset=utf-8")
                    .POST(HttpRequest.BodyPublishers.ofString(json.writeValueAsString(datos)))
                    .build();
            HttpResponse<String> respuesta = http.send(peticion, HttpResponse.BodyHandlers.ofString());
            // El servidor responde con un ResultadoSolicitudSeguimiento tanto si acepta (201)
            // como si rechaza (400).
            return json.readValue(respuesta.body(), ResultadoSolicitudSeguimiento.class);
        } catch (IOException e) {
            throw new ErrorComunicacionException("No se pudo contactar con el servidor", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ErrorComunicacionException("Se interrumpió la petición al servidor", e);
        }
    }
}
