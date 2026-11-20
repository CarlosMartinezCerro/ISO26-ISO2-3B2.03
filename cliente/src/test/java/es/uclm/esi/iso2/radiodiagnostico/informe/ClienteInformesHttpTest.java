package es.uclm.esi.iso2.radiodiagnostico.informe;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ClienteInformesHttpTest {

    private static final DatosCreacionInforme DATOS = new DatosCreacionInforme(
            "12345678A", "87654321B", "Radiología", "Dolor", "Consulta",
            "Diagnóstico", "Pruebas", "Tratamiento", false,
            LocalDate.of(2026, 11, 19), "Anamnesis", "Antecedentes",
            "Historial");

    private HttpServer servidor;
    private int codigo;
    private String cuerpo;
    private String metodo;
    private String ruta;
    private String requestBody;

    @BeforeEach
    void prepararServidor() throws IOException {
        servidor = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        servidor.createContext("/", intercambio -> {
            metodo = intercambio.getRequestMethod();
            ruta = intercambio.getRequestURI().toString();
            requestBody = new String(intercambio.getRequestBody().readAllBytes(),
                    StandardCharsets.UTF_8);
            byte[] respuesta = cuerpo == null ? new byte[0]
                    : cuerpo.getBytes(StandardCharsets.UTF_8);
            intercambio.sendResponseHeaders(codigo, respuesta.length);
            try (OutputStream salida = intercambio.getResponseBody()) {
                salida.write(respuesta);
            }
        });
        servidor.start();
    }

    @AfterEach
    void pararServidor() {
        servidor.stop(0);
    }

    @Test
    @DisplayName("URL nula lanza excepción")
    void constructorUrlNula() {
        assertThrows(NullPointerException.class,
                () -> new ClienteInformesHttp(null, HttpClient.newHttpClient()));
    }

    @Test
    @DisplayName("URL formada por espacios no se valida en el constructor")
    void constructorUrlEspacios() {
        ClienteInformesHttp cliente =
                new ClienteInformesHttp("   ", HttpClient.newHttpClient());
        assertTrue(cliente != null);
    }

    @Test
    @DisplayName("URL sin barra final construye la ruta")
    void constructorUrlSinBarra() {
        codigo = 201;
        cuerpo = "{\"correcto\":true,\"mensaje\":\"Creado\",\"informe\":null}";
        ClienteInformesHttp cliente = new ClienteInformesHttp(url());
        assertTrue(cliente.crearInforme(DATOS).correcto());
        assertEquals("POST", metodo);
        assertEquals("/informes", ruta);
    }

    @Test
    @DisplayName("URL con una barra final elimina la barra")
    void constructorUrlUnaBarra() {
        codigo = 201;
        cuerpo = "{\"correcto\":true,\"mensaje\":\"Creado\",\"informe\":null}";
        ClienteInformesHttp cliente = new ClienteInformesHttp(url() + "/");
        assertTrue(cliente.crearInforme(DATOS).correcto());
    }

    @Test
    @DisplayName("URL con varias barras finales elimina las barras")
    void constructorUrlVariasBarras() {
        codigo = 201;
        cuerpo = "{\"correcto\":true,\"mensaje\":\"Creado\",\"informe\":null}";
        ClienteInformesHttp cliente = new ClienteInformesHttp(url() + "///");
        assertTrue(cliente.crearInforme(DATOS).correcto());
    }

    @Test
    @DisplayName("Cliente HTTP nulo lanza excepción")
    void constructorHttpNulo() {
        assertThrows(NullPointerException.class,
                () -> new ClienteInformesHttp(url(), null));
    }

    @Test
    @DisplayName("Datos válidos crean un informe")
    void crearInformeDatosValidos() {
        codigo = 201;
        cuerpo = "{\"correcto\":true,\"mensaje\":\"Informe creado\",\"informe\":null}";
        ResultadoCreacionInforme resultado = new ClienteInformesHttp(url()).crearInforme(DATOS);
        assertTrue(resultado.correcto());
        assertTrue(requestBody.contains("\"dniPaciente\":\"87654321B\""));
    }

    @Test
    @DisplayName("Datos nulos no producen un resultado válido")
    void crearInformeDatosNulos() {
        codigo = 400;
        cuerpo = "{\"correcto\":false,\"mensaje\":\"Datos obligatorios\",\"informe\":null}";
        ResultadoCreacionInforme resultado =
                new ClienteInformesHttp(url()).crearInforme(null);
        assertEquals(false, resultado.correcto());
    }

    @Test
    @DisplayName("Respuesta 400 devuelve rechazo")
    void crearInformeRespuesta400() {
        codigo = 400;
        cuerpo = "{\"correcto\":false,\"mensaje\":\"Datos inválidos\",\"informe\":null}";
        ResultadoCreacionInforme resultado =
                new ClienteInformesHttp(url()).crearInforme(DATOS);
        assertEquals(false, resultado.correcto());
        assertEquals("Datos inválidos", resultado.mensaje());
    }

    @Test
    @DisplayName("Respuesta 503 devuelve indisponibilidad")
    void crearInformeRespuesta503() {
        codigo = 503;
        cuerpo = "{\"correcto\":false,\"mensaje\":\"Sistema no disponible\",\"informe\":null}";
        ResultadoCreacionInforme resultado =
                new ClienteInformesHttp(url()).crearInforme(DATOS);
        assertEquals(false, resultado.correcto());
    }

    @Test
    @DisplayName("Respuesta JSON inválida produce error")
    void crearInformeJsonInvalido() {
        codigo = 500;
        cuerpo = "esto no es json";
        assertThrows(RuntimeException.class,
                () -> new ClienteInformesHttp(url()).crearInforme(DATOS));
    }

    @Test
    @DisplayName("Servidor apagado produce ErrorComunicacionException")
    void crearInformeServidorApagado() {
        String url = url();
        servidor.stop(0);
        assertThrows(ErrorComunicacionException.class,
                () -> new ClienteInformesHttp(url).crearInforme(DATOS));
    }

    @Test
    @DisplayName("Petición interrumpida produce ErrorComunicacionException")
    void crearInformeInterrumpido() {
        HttpClient clienteFalso = new HttpClient() {
            @Override public java.util.Optional<java.net.CookieHandler> cookieHandler() { return java.util.Optional.empty(); }
            @Override public java.util.Optional<java.time.Duration> connectTimeout() { return java.util.Optional.empty(); }
            @Override public Redirect followRedirects() { return Redirect.NEVER; }
            @Override public java.util.Optional<java.net.ProxySelector> proxy() { return java.util.Optional.empty(); }
            @Override public javax.net.ssl.SSLContext sslContext() { return null; }
            @Override public javax.net.ssl.SSLParameters sslParameters() { return new javax.net.ssl.SSLParameters(); }
            @Override public java.util.Optional<java.net.Authenticator> authenticator() { return java.util.Optional.empty(); }
            @Override public java.util.Optional<java.util.concurrent.Executor> executor() { return java.util.Optional.empty(); }
            @Override public java.net.http.HttpClient.Version version() { return java.net.http.HttpClient.Version.HTTP_1_1; }

            @Override
            public <T> java.net.http.HttpResponse<T> send(java.net.http.HttpRequest request,
                    java.net.http.HttpResponse.BodyHandler<T> handler)
                    throws IOException, InterruptedException {
                throw new InterruptedException("interrumpida");
            }

            @Override
            public <T> java.util.concurrent.CompletableFuture<java.net.http.HttpResponse<T>> sendAsync(
                    java.net.http.HttpRequest request,
                    java.net.http.HttpResponse.BodyHandler<T> handler) {
                return null;
            }

            @Override
            public <T> java.util.concurrent.CompletableFuture<java.net.http.HttpResponse<T>> sendAsync(
                    java.net.http.HttpRequest request,
                    java.net.http.HttpResponse.BodyHandler<T> handler,
                    java.net.http.HttpResponse.PushPromiseHandler<T> pushHandler) {
                return null;
            }
        };
        assertThrows(ErrorComunicacionException.class,
                () -> new ClienteInformesHttp(url(), clienteFalso).crearInforme(DATOS));
    }

    @Test
    @DisplayName("DNI y especialidad válidos realizan GET")
    void antecedentesValidos() {
        codigo = 200;
        cuerpo = "{\"correcto\":true,\"mensaje\":\"OK\",\"antecedentes\":\"A\",\"historialEspecialidad\":\"H\"}";
        ResultadoAntecedentes resultado =
                new ClienteInformesHttp(url()).obtenerAntecedentes("87654321B", "Radiología");
        assertEquals("A", resultado.antecedentes());
        assertEquals("GET", metodo);
        assertTrue(ruta.contains("dniPaciente=87654321B"));
        assertTrue(ruta.contains("especialidad=Radiolog"));
    }

    @Test
    @DisplayName("DNI nulo lanza excepción")
    void antecedentesDniNulo() {
        assertThrows(IllegalArgumentException.class,
                () -> new ClienteInformesHttp(url()).obtenerAntecedentes(null, "Radiología"));
    }

    @Test
    @DisplayName("DNI vacío o espacios lanza excepción")
    void antecedentesDniEspacios() {
        assertThrows(IllegalArgumentException.class,
                () -> new ClienteInformesHttp(url()).obtenerAntecedentes("   ", "Radiología"));
    }

    @Test
    @DisplayName("Especialidad nula lanza excepción")
    void antecedentesEspecialidadNula() {
        assertThrows(IllegalArgumentException.class,
                () -> new ClienteInformesHttp(url()).obtenerAntecedentes("87654321B", null));
    }

    @Test
    @DisplayName("Especialidad vacía o espacios lanza excepción")
    void antecedentesEspecialidadEspacios() {
        assertThrows(IllegalArgumentException.class,
                () -> new ClienteInformesHttp(url()).obtenerAntecedentes("87654321B", "   "));
    }

    @Test
    @DisplayName("DNI y especialidad ausentes lanzan excepción")
    void antecedentesAmbosAusentes() {
        assertThrows(IllegalArgumentException.class,
                () -> new ClienteInformesHttp(url()).obtenerAntecedentes("", ""));
    }

    @Test
    @DisplayName("DNI y especialidad de longitud uno realizan la petición")
    void antecedentesLongitudUno() {
        codigo = 200;
        cuerpo = "{\"correcto\":true,\"mensaje\":\"OK\",\"antecedentes\":\"A\",\"historialEspecialidad\":\"H\"}";
        ResultadoAntecedentes resultado =
                new ClienteInformesHttp(url()).obtenerAntecedentes("1", "R");
        assertTrue(resultado.correcto());
    }

    @Test
    @DisplayName("Caracteres especiales se codifican en la URL")
    void antecedentesCaracteresEspeciales() {
        codigo = 200;
        cuerpo = "{\"correcto\":true,\"mensaje\":\"OK\",\"antecedentes\":\"A\",\"historialEspecialidad\":\"H\"}";
        new ClienteInformesHttp(url()).obtenerAntecedentes(
                "87654321B", "Radiología & Medicina");
        assertTrue(ruta.contains("%"));
    }

    @Test
    @DisplayName("Respuesta 400 devuelve rechazo de antecedentes")
    void antecedentesRespuesta400() {
        codigo = 400;
        cuerpo = "{\"correcto\":false,\"mensaje\":\"Datos inválidos\",\"antecedentes\":null,\"historialEspecialidad\":null}";
        ResultadoAntecedentes resultado =
                new ClienteInformesHttp(url()).obtenerAntecedentes("87654321B", "Radiología");
        assertEquals(false, resultado.correcto());
    }

    @Test
    @DisplayName("Respuesta 503 devuelve sistema no disponible")
    void antecedentesRespuesta503() {
        codigo = 503;
        cuerpo = "{\"correcto\":false,\"mensaje\":\"Sistema no disponible\",\"antecedentes\":null,\"historialEspecialidad\":null}";
        ResultadoAntecedentes resultado =
                new ClienteInformesHttp(url()).obtenerAntecedentes("87654321B", "Radiología");
        assertEquals(false, resultado.correcto());
    }

    @Test
    @DisplayName("Servidor no accesible produce ErrorComunicacionException")
    void antecedentesServidorNoAccesible() {
        String url = url();
        servidor.stop(0);
        assertThrows(ErrorComunicacionException.class,
                () -> new ClienteInformesHttp(url).obtenerAntecedentes("87654321B", "Radiología"));
    }

    @Test
    @DisplayName("Timeout se representa como ErrorComunicacionException")
    void antecedentesTimeout() throws IOException {
        servidor.stop(0);
        String url = url();
        assertThrows(ErrorComunicacionException.class,
                () -> new ClienteInformesHttp(url).obtenerAntecedentes("87654321B", "Radiología"));
    }

    private String url() {
        return "http://localhost:" + servidor.getAddress().getPort();
    }
}
