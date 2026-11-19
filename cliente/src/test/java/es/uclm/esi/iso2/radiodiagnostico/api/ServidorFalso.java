package es.uclm.esi.iso2.radiodiagnostico.api;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

/** Servidor HTTP de mentira para los tests: responde lo que se le diga y apunta lo que recibe. */
public class ServidorFalso implements AutoCloseable {

    private final HttpServer servidor;
    public int codigo = 200;
    public String respuesta = "{}";
    String metodo;
    public String ruta;
    String medico;
    String tipoContenido;
    String cuerpo;

    public ServidorFalso() throws IOException {
        servidor = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        servidor.createContext("/", intercambio -> {
            metodo = intercambio.getRequestMethod();
            ruta = intercambio.getRequestURI().getRawPath();
            medico = intercambio.getRequestHeaders().getFirst(ClienteHttp.CABECERA_MEDICO);
            tipoContenido = intercambio.getRequestHeaders().getFirst("Content-Type");
            cuerpo = new String(intercambio.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            byte[] bytes = respuesta.getBytes(StandardCharsets.UTF_8);
            intercambio.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
            intercambio.sendResponseHeaders(codigo, bytes.length);
            try (OutputStream salida = intercambio.getResponseBody()) {
                salida.write(bytes);
            }
        });
        servidor.start();
    }

    public String url() {
        return "http://localhost:" + servidor.getAddress().getPort();
    }

    @Override
    public void close() {
        servidor.stop(0);
    }
}
