package es.uclm.esi.iso2.radiodiagnostico.api;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Objects;

/**
 * Peticiones HTTP a la API REST del servidor (D-009), con el HttpClient de Java.
 * La comparten todos los clientes REST (ClienteRestCitas, ClienteRestImagenes...).
 */
public class ClienteHttp {

    public static final String CABECERA_MEDICO = "X-Medico";
    private static final Duration ESPERA = Duration.ofSeconds(5);

    private final String urlBase;
    private final HttpClient http;

    /** @param urlBase por ejemplo http://localhost:8080 */
    public ClienteHttp(String urlBase) {
        this(urlBase, HttpClient.newBuilder().connectTimeout(ESPERA).build());
    }

    ClienteHttp(String urlBase, HttpClient http) {
        this.urlBase = Objects.requireNonNull(urlBase, "La URL del servidor es obligatoria").replaceAll("/+$", "");
        this.http = Objects.requireNonNull(http, "El cliente HTTP es obligatorio");
    }

    /** GET con la cabecera del médico (si no es null). Devuelve el cuerpo JSON. */
    public String get(String ruta, String idMedico) throws IOException {
        HttpRequest.Builder peticion = peticion(ruta).GET();
        if (idMedico != null) {
            peticion.header(CABECERA_MEDICO, idMedico);
        }
        return enviar(peticion.build());
    }

    /** POST con un cuerpo JSON. Devuelve el cuerpo JSON de la respuesta. */
    public String post(String ruta, String json) throws IOException {
        return enviar(peticion(ruta)
                .header("Content-Type", "application/json; charset=utf-8")
                .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                .build());
    }

    /** Codifica un trozo de ruta (DNI, id...) para que no rompa la URL. */
    public static String segmento(String valor) {
        return URLEncoder.encode(valor, StandardCharsets.UTF_8).replace("+", "%20");
    }

    private HttpRequest.Builder peticion(String ruta) {
        return HttpRequest.newBuilder(URI.create(urlBase + ruta))
                .timeout(ESPERA)
                .header("Accept", "application/json");
    }

    /** Cualquier fallo (red, servidor caído, hilo interrumpido, código HTTP no 2xx) llega como IOException. */
    private String enviar(HttpRequest peticion) throws IOException {
        HttpResponse<String> respuesta;
        try {
            respuesta = http.send(peticion, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Se interrumpió la petición al servidor.", e);
        }
        if (respuesta.statusCode() / 100 != 2) {
            throw new IOException("El servidor respondió con el código HTTP " + respuesta.statusCode() + ".");
        }
        return respuesta.body();
    }
}
