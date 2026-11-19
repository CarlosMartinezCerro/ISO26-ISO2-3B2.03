package es.uclm.esi.iso2.radiodiagnostico;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import es.uclm.esi.iso2.radiodiagnostico.cita.CitaRepositorio;
import es.uclm.esi.iso2.radiodiagnostico.cita.CitaRepositorioEnMemoria;
import es.uclm.esi.iso2.radiodiagnostico.cita.ServicioConfirmacionCitasImpl;
import es.uclm.esi.iso2.radiodiagnostico.controlador.ControladorConfirmacionCitas;
import es.uclm.esi.iso2.radiodiagnostico.controlador.ControladorImagenes;
import es.uclm.esi.iso2.radiodiagnostico.controlador.ControladorPacientes;
import es.uclm.esi.iso2.radiodiagnostico.controlador.Rutas;
import es.uclm.esi.iso2.radiodiagnostico.controlador.RutasConfirmacionCitas;
import es.uclm.esi.iso2.radiodiagnostico.controlador.RutasImagenes;
import es.uclm.esi.iso2.radiodiagnostico.controlador.RutasPacientes;
import es.uclm.esi.iso2.radiodiagnostico.informe.ImagenRepositorioEnMemoria;
import es.uclm.esi.iso2.radiodiagnostico.informe.ServicioImagenesImpl;
import es.uclm.esi.iso2.radiodiagnostico.informe.SistemaPacsSimulado;
import es.uclm.esi.iso2.radiodiagnostico.paciente.PacienteRepositorio;
import es.uclm.esi.iso2.radiodiagnostico.paciente.PacienteRepositorioEnMemoria;
import es.uclm.esi.iso2.radiodiagnostico.paciente.PacienteRepositorioMySql;
import es.uclm.esi.iso2.radiodiagnostico.paciente.ServicioPacientes;
import es.uclm.esi.iso2.radiodiagnostico.paciente.ServicioPacientesImpl;
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

    /**
     * Tamaño máximo de una petición. Javalin admite 1 MB por defecto, que no basta para
     * imágenes médicas (CdU17): el fichero viaja en Base64, que ocupa un 33 % más.
     */
    static final long TAMANO_MAXIMO_PETICION = 30L * 1024 * 1024;

    private Servidor() {
    }

    public static void main(String[] args) {
        // Un único repositorio de citas para todos los casos de uso de citas
        // (CdU25 asignar, CdU2 confirmar, CdU4 cancelar...), para que vean las mismas citas.
        CitaRepositorio citas = new CitaRepositorioEnMemoria();
        crear(
                new RutasPacientes(new ControladorPacientes(
                        new ServicioPacientesImpl(repositorioPacientes()))),
                new RutasImagenes(new ControladorImagenes(
                        new ServicioImagenesImpl(new ImagenRepositorioEnMemoria(), new SistemaPacsSimulado()))),
                new RutasConfirmacionCitas(new ControladorConfirmacionCitas(
                        new ServicioConfirmacionCitasImpl(citas)))
        ).start(puerto());
    }

    /** Crea la aplicación solo con las rutas de pacientes (la usan los tests del CdU22). */
    public static Javalin crear(ServicioPacientes servicioPacientes) {
        return crear(new RutasPacientes(new ControladorPacientes(servicioPacientes)));
    }

    /**
     * Crea la aplicación con las rutas indicadas, sin arrancarla
     * (los tests la arrancan en un puerto libre). Cada bloque funcional aporta sus {@link Rutas}.
     */
    public static Javalin crear(Rutas... rutas) {
        ObjectMapper json = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        Javalin app = Javalin.create(config -> {
            config.jsonMapper(new JavalinJackson(json, false));
            config.http.maxRequestSize = TAMANO_MAXIMO_PETICION;
        });
        for (Rutas bloque : rutas) {
            bloque.registrar(app);
        }
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
