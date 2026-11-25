package es.uclm.esi.iso2.radiodiagnostico.solicitud;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Objects;

public class ClienteSolicitudesHttp implements ClienteSolicitudes {

    private static final Duration TIEMPO_MAXIMO = Duration.ofSeconds(10);
    private final String urlServidor;
    private final HttpClient http;
    private final ObjectMapper json = new ObjectMapper();

    public ClienteSolicitudesHttp(String urlServidor) {
        this(urlServidor, HttpClient.newBuilder()
                .connectTimeout(TIEMPO_MAXIMO)
                .cookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_ALL))
                .build());
    }

    public ClienteSolicitudesHttp(String urlServidor, HttpClient http) {
        this.urlServidor = Objects.requireNonNull(urlServidor,
                "La URL del servidor es obligatoria").replaceAll("/+$", "");
        this.http = Objects.requireNonNull(http, "El cliente HTTP es obligatorio");
    }

    @Override
    public List<String> obtenerPruebasDisponibles() {
        HttpRequest request = HttpRequest.newBuilder(
                        URI.create(urlServidor + "/pruebas-disponibles"))
                .timeout(TIEMPO_MAXIMO).GET().build();
        HttpResponse<String> response = enviar(request);
        if (response.statusCode() != 200) {
            throw new ErrorComunicacionException("No se pudieron cargar las pruebas disponibles.", null);
        }
        try {
            return json.readValue(response.body(), new TypeReference<List<String>>() { });
        } catch (IOException e) {
            throw new ErrorComunicacionException("La respuesta de pruebas no es válida.", e);
        }
    }

    @Override
    public ResultadoSolicitud solicitar(DatosSolicitudPrueba datos) {
        try {
            HttpRequest request = HttpRequest.newBuilder(
                            URI.create(urlServidor + "/solicitudes"))
                    .timeout(TIEMPO_MAXIMO)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json.writeValueAsString(datos)))
                    .build();
            HttpResponse<String> response = enviar(request);
            if (response.statusCode() == 201 || response.statusCode() == 400) {
                return json.readValue(response.body(), ResultadoSolicitud.class);
            }
            String mensaje = response.statusCode() == 401
                    ? "El paciente no está autenticado."
                    : "No se pudo registrar la solicitud.";
            throw new ErrorComunicacionException(mensaje, null);
        } catch (IOException e) {
            throw new ErrorComunicacionException("No se pudo preparar la solicitud.", e);
        }
    }

    private HttpResponse<String> enviar(HttpRequest request) {
        try {
            return http.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new ErrorComunicacionException("No se pudo contactar con el servidor.", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ErrorComunicacionException("Se interrumpió la comunicación con el servidor.", e);
        }
    }
}
