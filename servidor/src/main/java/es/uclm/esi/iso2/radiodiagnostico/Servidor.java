
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
import es.uclm.esi.iso2.radiodiagnostico.controlador.ControladorImagenes;
import es.uclm.esi.iso2.radiodiagnostico.controlador.ControladorInforme;
import es.uclm.esi.iso2.radiodiagnostico.controlador.ControladorPacientes;
import es.uclm.esi.iso2.radiodiagnostico.controlador.Rutas;
import es.uclm.esi.iso2.radiodiagnostico.controlador.RutasCitas;
import es.uclm.esi.iso2.radiodiagnostico.controlador.RutasImagenes;
import es.uclm.esi.iso2.radiodiagnostico.controlador.RutasInforme;
import es.uclm.esi.iso2.radiodiagnostico.controlador.RutasPacientes;
import es.uclm.esi.iso2.radiodiagnostico.controlador.ControladorSolicitudSeguimiento;
import es.uclm.esi.iso2.radiodiagnostico.controlador.RutasSolicitudSeguimiento;
import es.uclm.esi.iso2.radiodiagnostico.informe.ImagenRepositorioEnMemoria;
import es.uclm.esi.iso2.radiodiagnostico.informe.ServicioImagenesImpl;
import es.uclm.esi.iso2.radiodiagnostico.informe.ServicioInforme;
import es.uclm.esi.iso2.radiodiagnostico.informe.ServicioInformeImpl;
import es.uclm.esi.iso2.radiodiagnostico.informe.SistemaPacsSimulado;
import es.uclm.esi.iso2.radiodiagnostico.paciente.PacienteRepositorio;
import es.uclm.esi.iso2.radiodiagnostico.paciente.PacienteRepositorioEnMemoria;
import es.uclm.esi.iso2.radiodiagnostico.paciente.PacienteRepositorioMySql;
import es.uclm.esi.iso2.radiodiagnostico.paciente.ServicioPacientes;
import es.uclm.esi.iso2.radiodiagnostico.paciente.ServicioPacientesImpl;
import es.uclm.esi.iso2.radiodiagnostico.prueba.CatalogoTiposPruebaSimulado;
import es.uclm.esi.iso2.radiodiagnostico.prueba.RegistroMedicosSimulado;
import es.uclm.esi.iso2.radiodiagnostico.prueba.ServicioSolicitudSeguimientoImpl;
import es.uclm.esi.iso2.radiodiagnostico.prueba.SolicitudSeguimientoRepositorioEnMemoria;
import es.uclm.esi.iso2.radiodiagnostico.sistemasexternos.HistorialClinicoSimulado;
import es.uclm.esi.iso2.radiodiagnostico.solicitud.SolicitudRepositorioEnMemoria;
import io.javalin.Javalin;
import io.javalin.json.JavalinJackson;

/**
 * Punto de entrada del servidor: monta la API REST e inyecta las dependencias.
 */
public final class Servidor {

    public static final int PUERTO_POR_DEFECTO = 7070;

    /**
     * Tamaño máximo de una petición. Se amplía para permitir el envío
     * de imágenes médicas en Base64.
     */
    static final long TAMANO_MAXIMO_PETICION = 30L * 1024 * 1024;

    private Servidor() {
    }

    public static void main(String[] args) {
        crear(
                new RutasPacientes(new ControladorPacientes(
                        new ServicioPacientesImpl(repositorioPacientes()))),

                new RutasImagenes(new ControladorImagenes(
                        new ServicioImagenesImpl(
                                new ImagenRepositorioEnMemoria(),
                                new SistemaPacsSimulado()))),

                new RutasInforme(new ControladorInforme(
                        new ServicioInformeImpl(
                                new HistorialClinicoSimulado()))),

                new RutasCitas(new ControladorCitas(servicioCitas())),

                new RutasSolicitudSeguimiento(new ControladorSolicitudSeguimiento(
                        new ServicioSolicitudSeguimientoImpl(
                                new SolicitudSeguimientoRepositorioEnMemoria(),
                                new CatalogoTiposPruebaSimulado(),
                                new RegistroMedicosSimulado())))
        ).start(puerto());
    }

    /**
     * Crea la aplicación con pacientes e informes.
     * La utilizan los tests del CdU22 y del CU16.
     */
    public static Javalin crear(ServicioPacientes servicioPacientes) {
        return crear(
                servicioPacientes,
                new ServicioInformeImpl(new HistorialClinicoSimulado()));
    }

    /**
     * Crea la aplicación con pacientes e informes.
     */
    public static Javalin crear(
            ServicioPacientes servicioPacientes,
            ServicioInforme servicioInforme) {

        return crear(
                new RutasPacientes(
                        new ControladorPacientes(servicioPacientes)),
                new RutasInforme(
                        new ControladorInforme(servicioInforme)));
    }

    /**
     * Crea la aplicación con las rutas de pacientes y citas.
     */
    public static Javalin crear(
            ServicioPacientes servicioPacientes,
            ServicioCitas servicioCitas) {

        return crear(
                new RutasPacientes(
                        new ControladorPacientes(servicioPacientes)),
                new RutasCitas(
                        new ControladorCitas(servicioCitas)));
    }

    /**
     * Crea la aplicación con los bloques de rutas indicados,
     * sin arrancarla.
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
