
package es.uclm.esi.iso2.radiodiagnostico.controlador;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import es.uclm.esi.iso2.radiodiagnostico.Servidor;
import es.uclm.esi.iso2.radiodiagnostico.cita.Cita;
import es.uclm.esi.iso2.radiodiagnostico.cita.CitaRepositorioEnMemoria;
import es.uclm.esi.iso2.radiodiagnostico.cita.EstadoCita;
import es.uclm.esi.iso2.radiodiagnostico.cita.ServicioCitasImpl;
import es.uclm.esi.iso2.radiodiagnostico.solicitud.SolicitudRepositorioEnMemoria;
import es.uclm.esi.iso2.radiodiagnostico.paciente.PacienteRepositorioEnMemoria;
import es.uclm.esi.iso2.radiodiagnostico.paciente.ServicioPacientesImpl;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

class RutasCitasTest {

    @Test
    void consultarCitasDevuelveLasCitasDelPaciente() throws Exception {

        CitaRepositorioEnMemoria repositorio =
                new CitaRepositorioEnMemoria();

        repositorio.guardar(new Cita(
                "CITA-1", "SOL-1", "12345678A",
                "Radiografía",
                LocalDateTime.now().plusDays(1),
                "SALA-1", EstadoCita.ASIGNADA
        ));

        repositorio.guardar(new Cita(
                "CITA-2", "SOL-2", "87654321B",
                "Resonancia",
                LocalDateTime.now().plusDays(2),
                "SALA-2", EstadoCita.ASIGNADA
        ));

        var servicioCitas = new ServicioCitasImpl(
                new SolicitudRepositorioEnMemoria(),
                repositorio
        );

        var app = Servidor.crear(
                new ServicioPacientesImpl(
                        new PacienteRepositorioEnMemoria()),
                servicioCitas
        );

        // Simulamos que el paciente ha iniciado sesión.
        app.before("/citas", ctx ->
                ctx.sessionAttribute("dniPaciente", "12345678A"));

        app.start(0);

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(
                            "http://localhost:" + app.port() + "/citas"))
                    .GET()
                    .build();

            HttpResponse<String> response =
                    HttpClient.newHttpClient().send(
                            request,
                            HttpResponse.BodyHandlers.ofString());

            assertEquals(200, response.statusCode());
            assertTrue(response.body().contains("CITA-1"));
            assertFalse(response.body().contains("CITA-2"));

        } finally {
            app.stop();
        }
    }

    @Test
    void consultarCitasSinSesionDevuelve401() throws Exception {

        var servicioCitas = new ServicioCitasImpl(
                new SolicitudRepositorioEnMemoria(),
                new CitaRepositorioEnMemoria()
        );

        var app = Servidor.crear(
                new ServicioPacientesImpl(
                        new PacienteRepositorioEnMemoria()),
                servicioCitas
        );

        // No configuramos ninguna sesión.
        app.start(0);

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(
                            "http://localhost:" + app.port() + "/citas"))
                    .GET()
                    .build();

            HttpResponse<String> response =
                    HttpClient.newHttpClient().send(
                            request,
                            HttpResponse.BodyHandlers.ofString());

            assertEquals(401, response.statusCode());

        } finally {
            app.stop();
        }
    }
}

