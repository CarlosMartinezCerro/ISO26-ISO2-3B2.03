package es.uclm.esi.iso2.radiodiagnostico.modelo.informe;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Objects;

/**
 * Implementación de {@link ClienteImagenes} sobre la API REST del servidor (D-009).
 * El contenido del fichero viaja en Base64 dentro del JSON (Jackson lo hace solo con byte[]).
 */
public class ClienteImagenesHttp implements ClienteImagenes {

    static final String RUTA_IMAGENES = "/imagenes";
    private static final Duration TIEMPO_CONEXION = Duration.ofSeconds(10);
    /** Más margen que en otros casos de uso: las imágenes pueden ocupar varios MB. */
    private static final Duration TIEMPO_MAXIMO = Duration.ofSeconds(60);

    private final URI urlImagenes;
    private final HttpClient http;
    private final ObjectMapper json = new ObjectMapper();

    /** @param urlServidor dirección base del servidor, por ejemplo {@code http://localhost:7070} */
    public ClienteImagenesHttp(String urlServidor) {
        this(urlServidor, HttpClient.newBuilder().connectTimeout(TIEMPO_CONEXION).build());
    }

    public ClienteImagenesHttp(String urlServidor, HttpClient http) {
        Objects.requireNonNull(urlServidor, "La URL del servidor es obligatoria");
        this.urlImagenes = URI.create(urlServidor.replaceAll("/+$", "") + RUTA_IMAGENES);
        this.http = Objects.requireNonNull(http, "El cliente HTTP es obligatorio");
    }

    @Override
    public ResultadoCreacionImagen crearImagen(DatosImagen datos) {
        try {
            HttpRequest peticion = HttpRequest.newBuilder(urlImagenes)
                    .timeout(TIEMPO_MAXIMO)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json.writeValueAsString(datos)))
                    .build();
            HttpResponse<String> respuesta = http.send(peticion, HttpResponse.BodyHandlers.ofString());
            // El servidor responde con un ResultadoCreacionImagen tanto si acepta (201) como si rechaza (400).
            return json.readValue(respuesta.body(), ResultadoCreacionImagen.class);
        } catch (IOException e) {
            throw new ErrorComunicacionException("No se pudo contactar con el servidor", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ErrorComunicacionException("Se interrumpió la petición al servidor", e);
        }
    }
}
