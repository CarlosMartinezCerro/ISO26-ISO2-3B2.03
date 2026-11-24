package es.uclm.esi.iso2.radiodiagnostico.solicitud;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class ClienteSolicitudesHttpTest {

    @Test
    void consultaCatalogoYEnviaSolicitudConCookieDeSesion() throws Exception {
        HttpServer servidor = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        servidor.createContext("/pruebas-disponibles", ctx -> {
            ctx.getResponseHeaders().add("Set-Cookie", "JSESSIONID=sesion-prueba; Path=/");
            responder(ctx, 200, "[\"TAC\"]");
        });
        servidor.createContext("/solicitudes", ctx -> {
            String cuerpo = new String(ctx.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            boolean cookie = ctx.getRequestHeaders().getFirst("Cookie") != null;
            if (!cookie || !cuerpo.contains("TAC")) {
                responder(ctx, 400, "{\"correcto\":false,\"mensaje\":\"Datos inválidos\"}");
            } else {
                responder(ctx, 201, "{\"correcto\":true,\"mensaje\":\"Registrada\",\"idSolicitud\":\"SOL-1\",\"estado\":\"PENDIENTE\"}");
            }
        });
        servidor.start();
        try {
            ClienteSolicitudes cliente = new ClienteSolicitudesHttp(
                    "http://localhost:" + servidor.getAddress().getPort());
            assertEquals(java.util.List.of("TAC"), cliente.obtenerPruebasDisponibles());
            ResultadoSolicitud resultado = cliente.solicitar(
                    new DatosSolicitudPrueba("TAC", "URGENTE"));
            assertTrue(resultado.correcto());
            assertEquals("SOL-1", resultado.idSolicitud());
        } finally {
            servidor.stop(0);
        }
    }

    @Test
    void informaErroresHttpSinPresentarExito() throws Exception {
        HttpServer servidor = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        servidor.createContext("/solicitudes", ctx -> responder(ctx, 500,
                "{\"correcto\":false,\"mensaje\":\"Error interno\"}"));
        servidor.start();
        try {
            ClienteSolicitudes cliente = new ClienteSolicitudesHttp(
                    "http://localhost:" + servidor.getAddress().getPort());
            ErrorComunicacionException error = assertThrows(ErrorComunicacionException.class,
                    () -> cliente.solicitar(new DatosSolicitudPrueba("TAC", "URGENTE")));
            assertTrue(error.getMessage().contains("No se pudo registrar"));
        } finally {
            servidor.stop(0);
        }
    }

    private static void responder(com.sun.net.httpserver.HttpExchange exchange,
                                  int status, String cuerpo) throws java.io.IOException {
        byte[] bytes = cuerpo.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, bytes.length);
        try (var salida = exchange.getResponseBody()) {
            salida.write(bytes);
        }
    }
}
