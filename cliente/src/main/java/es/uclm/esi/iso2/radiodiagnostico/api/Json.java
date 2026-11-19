package es.uclm.esi.iso2.radiodiagnostico.api;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Conversión entre objetos Java y JSON para la API REST (D-009).
 * Las fechas viajan como texto ISO-8601 ("2026-11-02T09:30:00") y los byte[] en Base64.
 */
public final class Json {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .registerModule(new SimpleModule("fechas")
                    .addSerializer(LocalDateTime.class, comoTexto())
                    .addDeserializer(LocalDateTime.class, desdeTexto(LocalDateTime::parse))
                    .addSerializer(LocalDate.class, comoTexto())
                    .addDeserializer(LocalDate.class, desdeTexto(LocalDate::parse)));

    private Json() {
    }

    public static String escribir(Object objeto) {
        try {
            return MAPPER.writeValueAsString(objeto);
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** @throws JsonInvalidoException si el texto no es un JSON válido para ese tipo */
    public static <T> T leer(String json, Class<T> tipo) {
        try {
            return MAPPER.readValue(json, tipo);
        } catch (IOException | RuntimeException e) {
            throw new JsonInvalidoException("El JSON recibido no es válido.", e);
        }
    }

    private static <T> JsonSerializer<T> comoTexto() {
        return new JsonSerializer<>() {
            @Override
            public void serialize(T valor, JsonGenerator salida, SerializerProvider proveedor) throws IOException {
                salida.writeString(valor.toString());
            }
        };
    }

    private static <T> JsonDeserializer<T> desdeTexto(java.util.function.Function<String, T> convertir) {
        return new JsonDeserializer<>() {
            @Override
            public T deserialize(JsonParser entrada, DeserializationContext contexto) throws IOException {
                return convertir.apply(entrada.getValueAsString());
            }
        };
    }
}
