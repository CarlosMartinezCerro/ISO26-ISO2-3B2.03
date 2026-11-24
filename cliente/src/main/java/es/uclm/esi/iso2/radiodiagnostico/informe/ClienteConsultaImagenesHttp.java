package es.uclm.esi.iso2.radiodiagnostico.informe;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
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

/**
 * Implementación de {@link ClienteConsultaImagenes} sobre la API REST del servidor (D-009). CdU19.
 * El contenido de la imagen llega en Base64 dentro del JSON (Jackson lo convierte solo a byte[]).
 */
public class ClienteConsultaImagenesHttp implements ClienteConsultaImagenes {

    static final String CABECERA_MEDICO = "X-Medico";
    private static final Duration TIEMPO_CONEXION = Duration.ofSeconds(10);
    /** Más margen que en otros casos de uso: las imágenes pueden ocupar varios MB. */
    private static final Duration TIEMPO_MAXIMO = Duration.ofSeconds(60);

    private final String urlServidor;
    private final HttpClient http;
    /** Ignora los campos que el cliente no usa (por ejemplo el médico de la imagen). */
    private final ObjectMapper json = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    /** @param urlServidor dirección base del servidor, por ejemplo {@code http://localhost:7070} */
    public ClienteConsultaImagenesHttp(String urlServidor) {
        this(urlServidor, HttpClient.newBuilder().connectTimeout(TIEMPO_CONEXION).build());
    }

    public ClienteConsultaImagenesHttp(String urlServidor, HttpClient http) {
        Objects.requireNonNull(urlServidor, "La URL del servidor es obligatoria");
        this.urlServidor = urlServidor.replaceAll("/+$", "");
        this.http = Objects.requireNonNull(http, "El cliente HTTP es obligatorio");
    }

    @Override
    public RespuestaListadoImagenes obtenerImagenesPaciente(String idMedico, String dniPaciente) {
        return get("/pacientes/" + segmento(dniPaciente) + "/imagenes", idMedico, RespuestaListadoImagenes.class);
    }

    @Override
    public RespuestaConsultaImagen obtenerImagen(String idMedico, String idImagen) {
        return get("/imagenes/" + segmento(idImagen), idMedico, RespuestaConsultaImagen.class);
    }

    private <T> T get(String ruta, String idMedico, Class<T> tipo) {
        HttpRequest.Builder peticion = HttpRequest.newBuilder(URI.create(urlServidor + ruta))
                .timeout(TIEMPO_MAXIMO)
                .GET();
        if (idMedico != null) {
            peticion.header(CABECERA_MEDICO, idMedico);
        }
        try {
            HttpResponse<String> respuesta = http.send(peticion.build(), HttpResponse.BodyHandlers.ofString());
            // El servidor responde con el resultado tanto si acepta (200) como si rechaza (400).
            return json.readValue(respuesta.body(), tipo);
        } catch (IOException e) {
            throw new ErrorComunicacionException("No se pudo contactar con el servidor", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ErrorComunicacionException("Se interrumpió la petición al servidor", e);
        }
    }

    /** Codifica el DNI o el id para que no rompa la URL (espacios, barras, eñes...). */
    static String segmento(String valor) {
        return URLEncoder.encode(valor, StandardCharsets.UTF_8).replace("+", "%20");
    }
}
