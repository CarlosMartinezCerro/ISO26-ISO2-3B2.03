
package es.uclm.esi.iso2.radiodiagnostico.cita;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Objects;

/** Cliente HTTP para consultar y gestionar citas mediante la API REST. */
public class ClienteCitasHttp implements ClienteCitas {

    private static final String RUTA_CITAS = "/citas/";
    private static final Duration TIEMPO_MAXIMO = Duration.ofSeconds(10);

    private final String urlServidor;
    private final HttpClient http;

    private final ObjectMapper json = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    public ClienteCitasHttp(String urlServidor) {
        this(urlServidor, HttpClient.newBuilder()
                .connectTimeout(TIEMPO_MAXIMO)
                .build());
    }

    public ClienteCitasHttp(String urlServidor, HttpClient http) {
        this.urlServidor = Objects.requireNonNull(
                urlServidor, "La URL del servidor es obligatoria")
                .replaceAll("/+$", "");

        this.http = Objects.requireNonNull(
                http, "El cliente HTTP es obligatorio");
    }

    @Override
    public List<CitaPaciente> consultarCitas(String dniPaciente) {
        if (dniPaciente == null || dniPaciente.isBlank()) {
            throw new IllegalArgumentException("El DNI es obligatorio.");
        }

        try {
            URI uri = URI.create(urlServidor + RUTA_CITAS
                    + dniPaciente.trim());

            HttpRequest peticion = HttpRequest.newBuilder(uri)
                    .timeout(TIEMPO_MAXIMO)
                    .GET()
                    .build();

            HttpResponse<String> respuesta = http.send(
                    peticion, HttpResponse.BodyHandlers.ofString());

            if (respuesta.statusCode() != 200) {
                throw new ErrorComunicacionException(
                        "El servidor respondió con el código "
                                + respuesta.statusCode(), null);
            }

            return json.readValue(
                    respuesta.body(),
                    new TypeReference<List<CitaPaciente>>() {
                    });

        } catch (IOException e) {
            throw new ErrorComunicacionException(
                    "No se pudieron consultar las citas.", e);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ErrorComunicacionException(
                    "Se interrumpió la consulta de citas.", e);
        }
    }

    @Override
    public List<SolicitudPendiente> obtenerSolicitudesPendientes() {
        throw new UnsupportedOperationException(
                "Operación pendiente de integrar en este cliente.");
    }

    @Override
    public ResultadoAsignacion asignarCita(DatosAsignacionCita datos) {
        throw new UnsupportedOperationException(
                "Operación pendiente de integrar en este cliente.");
    }
}

