package es.uclm.esi.iso2.radiodiagnostico.controlador;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.uclm.esi.iso2.radiodiagnostico.Servidor;
import es.uclm.esi.iso2.radiodiagnostico.solicitud.SolicitudRepositorio;
import es.uclm.esi.iso2.radiodiagnostico.solicitud.SolicitudRepositorioEnMemoria;
import es.uclm.esi.iso2.radiodiagnostico.solicitud.ServicioSolicitudesImpl;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;

class RutasSolicitudesTest {

    @Test
    void listaPruebasDisponiblesYRegistraSolicitudDeSesion() throws Exception {
        SolicitudRepositorio solicitudes = new SolicitudRepositorioEnMemoria();
        var app = crearApp(solicitudes);
        app.before("/solicitudes", ctx -> ctx.sessionAttribute("dniPaciente", "12345678A"));
        app.start(0);
        try {
            HttpClient cliente = HttpClient.newHttpClient();
            HttpResponse<String> catalogo = cliente.send(HttpRequest.newBuilder(uri(app, "/pruebas-disponibles")).GET().build(),
                    HttpResponse.BodyHandlers.ofString());
            assertEquals(200, catalogo.statusCode());
            assertTrue(catalogo.body().contains("TAC"));

            HttpRequest post = HttpRequest.newBuilder(uri(app, "/solicitudes"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString("{\"tipoPrueba\":\"TAC\",\"prioridad\":\"URGENTE\"}"))
                    .build();
            HttpResponse<String> respuesta = cliente.send(post, HttpResponse.BodyHandlers.ofString());
            assertEquals(201, respuesta.statusCode());
            assertTrue(respuesta.body().contains("PENDIENTE"));
            assertTrue(respuesta.body().contains("SOL-"));
            assertEquals(1, solicitudes.buscarPorPaciente("12345678A").size());
        } finally {
            app.stop();
        }
    }

    @Test
    void noAceptaSolicitudSinSesion() throws Exception {
        var app = crearApp(new SolicitudRepositorioEnMemoria());
        app.start(0);
        try {
            HttpResponse<String> respuesta = HttpClient.newHttpClient().send(
                    HttpRequest.newBuilder(uri(app, "/solicitudes"))
                            .header("Content-Type", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString("{\"tipoPrueba\":\"TAC\",\"prioridad\":\"URGENTE\"}"))
                            .build(), HttpResponse.BodyHandlers.ofString());
            assertEquals(401, respuesta.statusCode());
            assertTrue(respuesta.body().contains("autenticado"));
        } finally {
            app.stop();
        }
    }

    @Test
    void devuelveErrorSiFallaLaPersistencia() throws Exception {
        SolicitudRepositorio fallo = new SolicitudRepositorio() {
            @Override public java.util.Optional<es.uclm.esi.iso2.radiodiagnostico.solicitud.SolicitudPrueba> buscarPorId(String id) { return java.util.Optional.empty(); }
            @Override public java.util.List<es.uclm.esi.iso2.radiodiagnostico.solicitud.SolicitudPrueba> buscarPorEstado(es.uclm.esi.iso2.radiodiagnostico.solicitud.EstadoSolicitud estado) { return java.util.List.of(); }
            @Override public java.util.List<es.uclm.esi.iso2.radiodiagnostico.solicitud.SolicitudPrueba> buscarPorPaciente(String dniPaciente) { return java.util.List.of(); }
            @Override public void guardar(es.uclm.esi.iso2.radiodiagnostico.solicitud.SolicitudPrueba solicitud) { throw new IllegalStateException("fallo"); }
        };
        var app = crearApp(fallo);
        app.before("/solicitudes", ctx -> ctx.sessionAttribute("dniPaciente", "12345678A"));
        app.start(0);
        try {
            HttpResponse<String> respuesta = HttpClient.newHttpClient().send(
                    HttpRequest.newBuilder(uri(app, "/solicitudes"))
                            .header("Content-Type", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString("{\"tipoPrueba\":\"TAC\",\"prioridad\":\"URGENTE\"}"))
                            .build(), HttpResponse.BodyHandlers.ofString());
            assertEquals(500, respuesta.statusCode());
            assertTrue(respuesta.body().contains("No se pudo registrar"));
            assertTrue(!respuesta.body().contains("SOL-"));
        } finally {
            app.stop();
        }
    }

    private static io.javalin.Javalin crearApp(SolicitudRepositorio repositorio) {
        return Servidor.crear(new RutasSolicitudes(new ControladorSolicitudes(
                new ServicioSolicitudesImpl(repositorio))));
    }

    private static URI uri(io.javalin.Javalin app, String ruta) {
        return URI.create("http://localhost:" + app.port() + ruta);
    }
}
