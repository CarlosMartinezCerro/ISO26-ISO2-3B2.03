package es.uclm.esi.iso2.radiodiagnostico;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.javalin.Javalin;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ServidorRadiodiagnosticoTest {

    @Test
    void sinVariableUsaElPuertoPorDefecto() {
        assertEquals(8080, ServidorRadiodiagnostico.puerto(null));
        assertEquals(8080, ServidorRadiodiagnostico.puerto(" "));
    }

    @Test
    void leeElPuertoDeLaVariable() {
        assertEquals(9090, ServidorRadiodiagnostico.puerto(" 9090 "));
    }

    @Test
    void unPuertoQueNoEsNumeroEsUnError() {
        assertThrows(NumberFormatException.class, () -> ServidorRadiodiagnostico.puerto("ocho mil"));
    }

    @Test
    void enMemoriaArrancaConTresSolicitudesFicticias() {
        String pendientes = ServidorRadiodiagnostico.crearApi(false).solicitudesPendientes();

        assertTrue(pendientes.startsWith("[{\"id\":\"SOL-002\""));
        assertEquals(3, pendientes.split("\"id\"").length - 1);
    }

    /** Arranca Javalin de verdad en un puerto libre y le hace peticiones HTTP reales. */
    @Test
    void elServidorRespondeEnTodasLasRutas() throws Exception {
        int puerto = puertoLibre();
        Javalin servidor = ServidorRadiodiagnostico.iniciar(Map.of("PUERTO", String.valueOf(puerto)));
        try {
            String url = "http://localhost:" + puerto;
            HttpResponse<String> pendientes = enviar(HttpRequest.newBuilder(URI.create(url + "/api/solicitudes/pendientes")).GET());
            assertEquals(200, pendientes.statusCode());
            assertTrue(pendientes.headers().firstValue("Content-Type").orElse("").startsWith("application/json"));
            assertTrue(pendientes.body().contains("SOL-002"));

            HttpResponse<String> cita = enviar(HttpRequest.newBuilder(URI.create(url + "/api/citas"))
                    .POST(HttpRequest.BodyPublishers.ofString("{\"idSolicitud\":\"SOL-002\",\"fechaHora\":\"2099-01-01T09:00\",\"sala\":\"S1\"}")));
            assertTrue(cita.body().contains("\"correcto\":true"));

            HttpResponse<String> imagen = enviar(HttpRequest.newBuilder(URI.create(url + "/api/imagenes"))
                    .POST(HttpRequest.BodyPublishers.ofString("{\"idInforme\":\"INF-1\",\"dniPaciente\":\"FICTICIO-001\","
                            + "\"idMedico\":\"MED-001\",\"tipoPrueba\":\"TAC\",\"nombreFichero\":\"a.png\",\"contenido\":\"AQID\"}")));
            assertTrue(imagen.body().contains("IMG-001"));

            HttpResponse<String> listado = enviar(HttpRequest.newBuilder(
                    URI.create(url + "/api/pacientes/FICTICIO-001/imagenes")).header("X-Medico", "MED-001").GET());
            assertTrue(listado.body().contains("\"id\":\"IMG-001\""));

            HttpResponse<String> consulta = enviar(HttpRequest.newBuilder(
                    URI.create(url + "/api/imagenes/IMG-001")).header("X-Medico", "MED-001").GET());
            assertTrue(consulta.body().contains("\"contenido\":\"AQID\""));
        } finally {
            servidor.stop();
        }
    }

    private static HttpResponse<String> enviar(HttpRequest.Builder peticion) throws IOException, InterruptedException {
        return HttpClient.newHttpClient().send(peticion.build(), HttpResponse.BodyHandlers.ofString());
    }

    private static int puertoLibre() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }
}
