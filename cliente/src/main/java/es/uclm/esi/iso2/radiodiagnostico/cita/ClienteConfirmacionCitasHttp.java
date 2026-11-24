package es.uclm.esi.iso2.radiodiagnostico.cita;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Objects;

/** Implementación de {@link ClienteConfirmacionCitas} sobre la API REST del servidor (D-009). */
public class ClienteConfirmacionCitasHttp implements ClienteConfirmacionCitas {

    private static final Duration TIEMPO_CONEXION = Duration.ofSeconds(10);
    private static final Duration TIEMPO_MAXIMO = Duration.ofSeconds(15);

    private final String urlServidor;
    private final HttpClient http;
    private final ObjectMapper json = new ObjectMapper();

    /** @param urlServidor dirección base del servidor, por ejemplo {@code http://localhost:7070} */
    public ClienteConfirmacionCitasHttp(String urlServidor) {
        this(urlServidor, HttpClient.newBuilder().connectTimeout(TIEMPO_CONEXION).build());
    }

    public ClienteConfirmacionCitasHttp(String urlServidor, HttpClient http) {
        this.urlServidor = Objects.requireNonNull(urlServidor, "La URL del servidor es obligatoria")
                .replaceAll("/+$", "");
        this.http = Objects.requireNonNull(http, "El cliente HTTP es obligatorio");
    }

    /** {@code POST /citas/{idCita}/confirmacion} con el DNI en el cuerpo. */
    static String ruta(String idCita) {
        return "/citas/" + URLEncoder.encode(idCita, StandardCharsets.UTF_8).replace("+", "%20")
                + "/confirmacion";
    }

    @Override
    public ResultadoConfirmacion confirmarCita(DatosConfirmacionCita datos) {
        try {
            HttpRequest peticion = HttpRequest.newBuilder(URI.create(urlServidor + ruta(datos.idCita())))
                    .timeout(TIEMPO_MAXIMO)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json.writeValueAsString(datos)))
                    .build();
            HttpResponse<String> respuesta = http.send(peticion, HttpResponse.BodyHandlers.ofString());
            // El servidor responde con un ResultadoConfirmacion tanto si confirma (200) como si rechaza (400).
            return json.readValue(respuesta.body(), ResultadoConfirmacion.class);
        } catch (IOException e) {
            throw new ErrorComunicacionException("No se pudo contactar con el servidor", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ErrorComunicacionException("Se interrumpió la petición al servidor", e);
        }
    }
}
