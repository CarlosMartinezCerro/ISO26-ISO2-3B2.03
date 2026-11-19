
package es.uclm.esi.iso2.radiodiagnostico;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import es.uclm.esi.iso2.radiodiagnostico.cita.CitaRepositorio;
import es.uclm.esi.iso2.radiodiagnostico.cita.CitaRepositorioEnMemoria;
import es.uclm.esi.iso2.radiodiagnostico.cita.CitaRepositorioMySql;
import es.uclm.esi.iso2.radiodiagnostico.cita.ServicioCitas;
import es.uclm.esi.iso2.radiodiagnostico.cita.ServicioCitasImpl;
import es.uclm.esi.iso2.radiodiagnostico.controlador.ControladorCitas;
import es.uclm.esi.iso2.radiodiagnostico.controlador.ControladorPacientes;
import es.uclm.esi.iso2.radiodiagnostico.controlador.RutasCitas;
import es.uclm.esi.iso2.radiodiagnostico.controlador.RutasPacientes;
import es.uclm.esi.iso2.radiodiagnostico.paciente.PacienteRepositorio;
import es.uclm.esi.iso2.radiodiagnostico.paciente.PacienteRepositorioEnMemoria;
import es.uclm.esi.iso2.radiodiagnostico.paciente.PacienteRepositorioMySql;
import es.uclm.esi.iso2.radiodiagnostico.paciente.ServicioPacientes;
import es.uclm.esi.iso2.radiodiagnostico.paciente.ServicioPacientesImpl;
import es.uclm.esi.iso2.radiodiagnostico.solicitud.SolicitudRepositorioEnMemoria;
import io.javalin.Javalin;
import io.javalin.json.JavalinJackson;

/**
 * Punto de entrada del servidor: monta la API REST e inyecta las dependencias.
 */
public final class Servidor {

    public static final int PUERTO_POR_DEFECTO = 7070;

    private Servidor() {
    }

    public static void main(String[] args) {
        ServicioPacientes servicioPacientes =
                new ServicioPacientesImpl(repositorioPacientes());

        ServicioCitas servicioCitas = servicioCitas();

        crear(servicioPacientes, servicioCitas).start(puerto());
    }

    public static Javalin crear(ServicioPacientes servicioPacientes) {
        return crear(servicioPacientes, servicioCitas());
    }

    public static Javalin crear(
            ServicioPacientes servicioPacientes,
            ServicioCitas servicioCitas) {

        ObjectMapper json = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        Javalin app = Javalin.create(config ->
                config.jsonMapper(new JavalinJackson(json, false)));

        new RutasPacientes(
                new ControladorPacientes(servicioPacientes)
        ).registrar(app);

        new RutasCitas(
                new ControladorCitas(servicioCitas)
        ).registrar(app);

        return app;
    }

    private static ServicioCitas servicioCitas() {
        return new ServicioCitasImpl(
                new SolicitudRepositorioEnMemoria(),
                repositorioCitas()
        );
    }

    private static CitaRepositorio repositorioCitas() {
        String url = System.getenv("MYSQL_URL");

        if (url == null || url.isBlank()) {
            System.err.println(
                    "MYSQL_URL no está definida: las citas se guardarán en memoria."
            );
            return new CitaRepositorioEnMemoria();
        }

        return new CitaRepositorioMySql();
    }

    private static PacienteRepositorio repositorioPacientes() {
        String url = System.getenv("MYSQL_URL");

        if (url == null || url.isBlank()) {
            System.err.println(
                    "MYSQL_URL no está definida: los pacientes se guardarán en memoria."
            );
            return new PacienteRepositorioEnMemoria();
        }

        return new PacienteRepositorioMySql();
    }

    private static int puerto() {
        String puerto = System.getenv("PUERTO");

        return puerto == null || puerto.isBlank()
                ? PUERTO_POR_DEFECTO
                : Integer.parseInt(puerto);
    }
}

