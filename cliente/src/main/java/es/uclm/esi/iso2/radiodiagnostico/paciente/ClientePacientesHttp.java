package es.uclm.esi.iso2.radiodiagnostico.paciente;

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

/** Implementación de {@link ClientePacientes} sobre la API REST del servidor (D-009). */
public class ClientePacientesHttp implements ClientePacientes {

    static final String RUTA_PACIENTES = "/pacientes";
    private static final Duration TIEMPO_MAXIMO = Duration.ofSeconds(10);

    private final URI urlPacientes;
    private final HttpClient http;
    private final ObjectMapper json = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    /** @param urlServidor dirección base del servidor, por ejemplo {@code http://localhost:7070} */
    public ClientePacientesHttp(String urlServidor) {
        this(urlServidor, HttpClient.newBuilder().connectTimeout(TIEMPO_MAXIMO).build());
    }

    public ClientePacientesHttp(String urlServidor, HttpClient http) {
        Objects.requireNonNull(urlServidor, "La URL del servidor es obligatoria");
        this.urlPacientes = URI.create(urlServidor.replaceAll("/+$", "") + RUTA_PACIENTES);
        this.http = Objects.requireNonNull(http, "El cliente HTTP es obligatorio");
    }

    @Override
    public ResultadoAlta darDeAlta(DatosAltaPaciente datos) {
        try {
            HttpRequest peticion = HttpRequest.newBuilder(urlPacientes)
                    .timeout(TIEMPO_MAXIMO)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json.writeValueAsString(datos)))
                    .build();
            HttpResponse<String> respuesta = http.send(peticion, HttpResponse.BodyHandlers.ofString());
            // El servidor responde con un ResultadoAlta tanto si acepta (201) como si rechaza (400).
            return json.readValue(respuesta.body(), ResultadoAlta.class);
        } catch (IOException e) {
            throw new ErrorComunicacionException("No se pudo contactar con el servidor", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ErrorComunicacionException("Se interrumpió la petición al servidor", e);
        }
    }
}
