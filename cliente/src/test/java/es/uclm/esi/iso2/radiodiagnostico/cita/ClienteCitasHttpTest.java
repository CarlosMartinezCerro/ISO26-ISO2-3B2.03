
package es.uclm.esi.iso2.radiodiagnostico.cita;

import static org.junit.jupiter.api.Assertions.*;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ClienteCitasHttpTest {

    private HttpServer servidor;
    private ClienteCitasHttp cliente;

    @BeforeEach
    void preparar() throws Exception {
        servidor = HttpServer.create(
                new InetSocketAddress("localhost", 0), 0);

        servidor.start();

        cliente = new ClienteCitasHttp(
                "http://localhost:" + servidor.getAddress().getPort());
    }

    @AfterEach
    void cerrar() {
        servidor.stop(0);
    }

    @Test
    void consultaYConvierteLasCitasRecibidas() {
        servidor.createContext("/citas", exchange -> {
            String respuesta = """
                    [
                      {
                        "id": "CITA-1",
                        "idSolicitud": "SOL-1",
                        "dniPaciente": "12345678A",
                        "tipoPrueba": "Radiografía",
                        "fechaHora": "2026-12-01T10:30:00",
                        "sala": "SALA-1",
                        "estado": "ASIGNADA"
                      }
                    ]
                    """;

            byte[] datos = respuesta.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set(
                    "Content-Type", "application/json");
            exchange.sendResponseHeaders(200, datos.length);
            exchange.getResponseBody().write(datos);
            exchange.close();
        });

        List<CitaPaciente> citas =
                cliente.consultarCitas();

        assertEquals(1, citas.size());
        assertEquals("CITA-1", citas.get(0).id());
        assertEquals("Radiografía", citas.get(0).tipoPrueba());
        assertEquals("SALA-1", citas.get(0).sala());
        assertEquals("ASIGNADA", citas.get(0).estado());
        assertEquals(
                LocalDateTime.of(2026, 12, 1, 10, 30),
                citas.get(0).fechaHora());
    }

    @Test
    void respuestaSinCitasDevuelveListaVacia() {
        servidor.createContext("/citas", exchange -> {
            byte[] datos = "[]".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, datos.length);
            exchange.getResponseBody().write(datos);
            exchange.close();
        });

        List<CitaPaciente> citas =
                cliente.consultarCitas();

        assertTrue(citas.isEmpty());
    }

    @Test
    void respuestaHttpIncorrectaLanzaErrorDeComunicacion() {
        servidor.createContext("/citas", exchange -> {
            exchange.sendResponseHeaders(500, -1);
            exchange.close();
        });

        assertThrows(
                ErrorComunicacionException.class,
                () -> cliente.consultarCitas());
    }

}

