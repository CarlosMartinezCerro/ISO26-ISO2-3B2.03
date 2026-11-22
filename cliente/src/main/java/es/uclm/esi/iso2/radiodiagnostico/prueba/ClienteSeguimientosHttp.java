package es.uclm.esi.iso2.radiodiagnostico.prueba;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import es.uclm.esi.iso2.radiodiagnostico.informe.ErrorComunicacionException;
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
 * Cliente HTTP del CdU11 contra la API REST GET /seguimientos?dniPaciente=...
 * Sigue el mismo esquema que {@code ClienteCitasHttp} (CdU1) y reutiliza la
 * {@link ErrorComunicacionException} del paquete {@code informe} (CdU10).
 */
public class ClienteSeguimientosHttp implements ClienteSeguimientos {

    public static final String RUTA_SEGUIMIENTOS = "/seguimientos";
    private static final Duration TIEMPO_MAXIMO = Duration.ofSeconds(10);

    private final String urlServidor;
    private final HttpClient http;
    private final ObjectMapper json = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    public ClienteSeguimientosHttp(String urlServidor) {
        this(urlServidor, HttpClient.newBuilder().connectTimeout(TIEMPO_MAXIMO).build());
    }

    public ClienteSeguimientosHttp(String urlServidor, HttpClient http) {
        this.urlServidor = Objects.requireNonNull(urlServidor, "La URL del servidor es obligatoria")
                .replaceAll("/+$", "");
        this.http = Objects.requireNonNull(http, "El cliente HTTP es obligatorio");
    }

    @Override
    public ResultadoConsultaSeguimientos consultarSeguimientos(String dniPaciente) {
        String dni = dniPaciente == null ? "" : dniPaciente;
        URI uri = URI.create(urlServidor + RUTA_SEGUIMIENTOS + "?dniPaciente="
                + URLEncoder.encode(dni, StandardCharsets.UTF_8));
        HttpRequest peticion = HttpRequest.newBuilder(uri)
                .timeout(TIEMPO_MAXIMO)
                .header("Accept", "application/json")
                .GET()
                .build();
        try {
            HttpResponse<String> respuesta = http.send(peticion, HttpResponse.BodyHandlers.ofString());
            // 200: consulta hecha; 400: el servidor la rechaza con un mensaje para el usuario.
            if (respuesta.statusCode() != 200 && respuesta.statusCode() != 400) {
                throw new ErrorComunicacionException(
                        "El servidor respondió con el código " + respuesta.statusCode(), null);
            }
            return json.readValue(respuesta.body(), ResultadoConsultaSeguimientos.class);
        } catch (IOException e) {
            throw new ErrorComunicacionException(
                    "No se han podido consultar las pruebas de seguimiento.", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ErrorComunicacionException(
                    "Se interrumpió la consulta de pruebas de seguimiento.", e);
        }
    }
}
