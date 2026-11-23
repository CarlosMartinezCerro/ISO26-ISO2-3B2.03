
package es.uclm.esi.iso2.radiodiagnostico.controlador;

import static org.junit.jupiter.api.Assertions.assertEquals;

import es.uclm.esi.iso2.radiodiagnostico.Servidor;
import es.uclm.esi.iso2.radiodiagnostico.modelo.cita.Cita;
import es.uclm.esi.iso2.radiodiagnostico.modelo.cita.CitaRepositorio;
import es.uclm.esi.iso2.radiodiagnostico.modelo.cita.ServicioCitasImpl;
import es.uclm.esi.iso2.radiodiagnostico.modelo.paciente.PacienteRepositorioEnMemoria;
import es.uclm.esi.iso2.radiodiagnostico.modelo.paciente.ServicioPacientesImpl;
import es.uclm.esi.iso2.radiodiagnostico.modelo.solicitud.SolicitudRepositorioEnMemoria;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

class RutasCitasErrorTest {

    @Test
    void falloDelRepositorioDevuelveError500() throws Exception {

        CitaRepositorio repositorio = new CitaRepositorio() {

            @Override
            public void guardar(Cita cita) {
            }

            @Override
            public Optional<Cita> buscarPorSolicitud(
                    String idSolicitud) {
                return Optional.empty();
            }

            @Override
            public Optional<Cita> buscarPorId(String id) {
                return Optional.empty();
            }

            @Override
            public void actualizar(Cita cita) {
            }

            @Override
            public boolean existeCitaEn(
                    String sala, LocalDateTime fechaHora) {
                return false;
            }

            @Override
            public List<Cita> buscarPorDniPaciente(
                    String dniPaciente) {
                throw new IllegalStateException(
                        "Error simulado del repositorio");
            }
        };

        var servicioCitas = new ServicioCitasImpl(
                new SolicitudRepositorioEnMemoria(),
                repositorio
        );

        var app = Servidor.crear(
                new ServicioPacientesImpl(
                        new PacienteRepositorioEnMemoria()),
                servicioCitas
        );

        // Simulamos un paciente autenticado.
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

            assertEquals(500, response.statusCode());
            assertEquals(
                    "{\"error\":\"No se pudieron consultar las citas.\"}",
                    response.body());

        } finally {
            app.stop();
        }
    }
}

