package es.uclm.esi.iso2.radiodiagnostico;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import es.uclm.esi.iso2.radiodiagnostico.controlador.ControladorPacientes;
import es.uclm.esi.iso2.radiodiagnostico.controlador.ControladorInforme;
import es.uclm.esi.iso2.radiodiagnostico.controlador.RutasPacientes;
import es.uclm.esi.iso2.radiodiagnostico.controlador.RutasInforme;
import es.uclm.esi.iso2.radiodiagnostico.informe.ServicioInforme;
import es.uclm.esi.iso2.radiodiagnostico.informe.ServicioInformeImpl;
import es.uclm.esi.iso2.radiodiagnostico.paciente.PacienteRepositorio;
import es.uclm.esi.iso2.radiodiagnostico.paciente.PacienteRepositorioEnMemoria;
import es.uclm.esi.iso2.radiodiagnostico.paciente.PacienteRepositorioMySql;
import es.uclm.esi.iso2.radiodiagnostico.paciente.ServicioPacientes;
import es.uclm.esi.iso2.radiodiagnostico.paciente.ServicioPacientesImpl;
import es.uclm.esi.iso2.radiodiagnostico.sistemasexternos.HistorialClinicoSimulado;
import io.javalin.Javalin;
import io.javalin.json.JavalinJackson;

/**
 * Punto de entrada del servidor: monta la API REST (D-009) e inyecta las dependencias.
 *
 * <p>Variables de entorno:
 * <ul>
 *   <li>{@code PUERTO}: puerto HTTP (por defecto 7070).</li>
 *   <li>{@code MYSQL_URL}, {@code MYSQL_USER}, {@code MYSQL_PASSWORD}: conexión a MySQL (D-010).
 *       Si no se define {@code MYSQL_URL}, los pacientes se guardan en memoria.</li>
 * </ul>
 */
public final class Servidor {

    public static final int PUERTO_POR_DEFECTO = 7070;

    private Servidor() {
    }

    public static void main(String[] args) {
        crear(
                new ServicioPacientesImpl(repositorioPacientes()),
                new ServicioInformeImpl(new HistorialClinicoSimulado())
        ).start(puerto());
    }

    /** Crea la aplicación con sus rutas, sin arrancarla (los tests la arrancan en un puerto libre). */
    public static Javalin crear(ServicioPacientes servicioPacientes) {
        return crear(servicioPacientes,
                new ServicioInformeImpl(new HistorialClinicoSimulado()));
    }

    /** Permite inyectar el servicio de informes, por ejemplo en pruebas. */
    public static Javalin crear(ServicioPacientes servicioPacientes,
                                ServicioInforme servicioInforme) {
        ObjectMapper json = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        Javalin app = Javalin.create(config -> config.jsonMapper(new JavalinJackson(json, false)));
        new RutasPacientes(new ControladorPacientes(servicioPacientes)).registrar(app);
        new RutasInforme(new ControladorInforme(servicioInforme)).registrar(app);
        return app;
    }

    private static PacienteRepositorio repositorioPacientes() {
        String url = System.getenv("MYSQL_URL");
        if (url == null || url.isBlank()) {
            System.err.println("MYSQL_URL no está definida: los pacientes se guardarán en memoria.");
            return new PacienteRepositorioEnMemoria();
        }
        return new PacienteRepositorioMySql();
    }

    private static int puerto() {
        String puerto = System.getenv("PUERTO");
        return puerto == null || puerto.isBlank() ? PUERTO_POR_DEFECTO : Integer.parseInt(puerto);
    }
}
