package es.uclm.esi.iso2.radiodiagnostico.informe;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.core.type.TypeReference;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Objects;

/** Implementacion HTTP del cliente de informes. */
public class ClienteInformesHttp implements ClienteInformes {

    public static final String RUTA_INFORMES = "/informes";
    public static final String RUTA_ANTECEDENTES = "/informes/antecedentes";
    private static final Duration TIEMPO_MAXIMO = Duration.ofSeconds(10);

    private final String urlBase;
    private final HttpClient http;
    private final ObjectMapper json = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    public ClienteInformesHttp(String urlServidor) {
        this(urlServidor, HttpClient.newBuilder().connectTimeout(TIEMPO_MAXIMO).build());
    }

    public ClienteInformesHttp(String urlServidor, HttpClient http) {
        Objects.requireNonNull(urlServidor, "La URL del servidor es obligatoria");
        this.urlBase = urlServidor.replaceAll("/+$", "");
        this.http = Objects.requireNonNull(http, "El cliente HTTP es obligatorio");
    }

    @Override
    public ResultadoCreacionInforme crearInforme(DatosCreacionInforme datos) {
        try {
            HttpRequest peticion = HttpRequest.newBuilder(URI.create(urlBase + RUTA_INFORMES))
                    .timeout(TIEMPO_MAXIMO)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json.writeValueAsString(datos)))
                    .build();
            HttpResponse<String> respuesta =
                    http.send(peticion, HttpResponse.BodyHandlers.ofString());
            return json.readValue(respuesta.body(), ResultadoCreacionInforme.class);
        } catch (IOException excepcion) {
            throw new ErrorComunicacionException(
                    "No se pudo contactar con el servidor", excepcion);
        } catch (InterruptedException excepcion) {
            Thread.currentThread().interrupt();
            throw new ErrorComunicacionException(
                    "Se interrumpio la peticion al servidor", excepcion);
        }
    }

    @Override
    public ResultadoAntecedentes obtenerAntecedentes(String dniPaciente,
                                                      String especialidad) {
        if (dniPaciente == null || dniPaciente.isBlank()
                || especialidad == null || especialidad.isBlank()) {
            throw new IllegalArgumentException(
                    "dniPaciente y especialidad son obligatorios.");
        }
        try {
            String query = "dniPaciente=" + codificar(dniPaciente)
                    + "&especialidad=" + codificar(especialidad);
            HttpRequest peticion = HttpRequest.newBuilder(
                    URI.create(urlBase + RUTA_ANTECEDENTES + "?" + query))
                    .timeout(TIEMPO_MAXIMO)
                    .GET()
                    .build();
            HttpResponse<String> respuesta =
                    http.send(peticion, HttpResponse.BodyHandlers.ofString());
            return json.readValue(respuesta.body(), ResultadoAntecedentes.class);
        } catch (IOException excepcion) {
            throw new ErrorComunicacionException(
                    "No se pudo contactar con el servidor", excepcion);
        } catch (InterruptedException excepcion) {
            Thread.currentThread().interrupt();
            throw new ErrorComunicacionException(
                    "Se interrumpio la peticion al servidor", excepcion);
        }
    }

    @Override
    public List<InformeResumen> listarInformes(String dniPaciente) {
        if (dniPaciente == null || dniPaciente.isBlank()) {
            throw new IllegalArgumentException("dniPaciente es obligatorio.");
        }
        try {
            URI uri = URI.create(urlBase + RUTA_INFORMES
                    + "?dniPaciente=" + codificar(dniPaciente));
            HttpRequest peticion = HttpRequest.newBuilder(uri)
                    .timeout(TIEMPO_MAXIMO)
                    .GET()
                    .build();
            HttpResponse<String> respuesta =
                    http.send(peticion, HttpResponse.BodyHandlers.ofString());
            if (respuesta.statusCode() != 200) {
                throw new ErrorComunicacionException(
                        "El servidor devolvió el código " + respuesta.statusCode(), null);
            }
            return json.readValue(respuesta.body(),
                    new TypeReference<List<InformeResumen>>() {
                    });
        } catch (IOException excepcion) {
            throw new ErrorComunicacionException(
                    "No se pudo contactar con el servidor", excepcion);
        } catch (InterruptedException excepcion) {
            Thread.currentThread().interrupt();
            throw new ErrorComunicacionException(
                    "Se interrumpio la peticion al servidor", excepcion);
        }
    }

    @Override
    public InformeRespuesta obtenerInforme(long idInforme) {
        if (idInforme <= 0) {
            throw new IllegalArgumentException("idInforme no es valido.");
        }
        try {
            HttpRequest peticion = HttpRequest.newBuilder(
                    URI.create(urlBase + RUTA_INFORMES + "/" + idInforme))
                    .timeout(TIEMPO_MAXIMO)
                    .GET()
                    .build();
            HttpResponse<String> respuesta =
                    http.send(peticion, HttpResponse.BodyHandlers.ofString());
            if (respuesta.statusCode() == 404) {
                return null;
            }
            if (respuesta.statusCode() != 200) {
                throw new ErrorComunicacionException(
                        "El servidor devolvió el código " + respuesta.statusCode(), null);
            }
            return json.readValue(respuesta.body(), InformeRespuesta.class);
        } catch (IOException excepcion) {
            throw new ErrorComunicacionException(
                    "No se pudo contactar con el servidor", excepcion);
        } catch (InterruptedException excepcion) {
            Thread.currentThread().interrupt();
            throw new ErrorComunicacionException(
                    "Se interrumpio la peticion al servidor", excepcion);
        }
    }

    private static String codificar(String texto) {
        return URLEncoder.encode(texto.trim(), StandardCharsets.UTF_8);
    }
}
