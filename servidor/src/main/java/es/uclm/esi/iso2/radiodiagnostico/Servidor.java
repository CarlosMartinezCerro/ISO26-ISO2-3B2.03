
package es.uclm.esi.iso2.radiodiagnostico;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import es.uclm.esi.iso2.radiodiagnostico.modelo.cita.CitaRepositorio;
import es.uclm.esi.iso2.radiodiagnostico.modelo.cita.CitaRepositorioEnMemoria;
import es.uclm.esi.iso2.radiodiagnostico.modelo.cita.CitaRepositorioMySql;
import es.uclm.esi.iso2.radiodiagnostico.modelo.cita.ServicioCitas;
import es.uclm.esi.iso2.radiodiagnostico.modelo.cita.ServicioCitasImpl;
import es.uclm.esi.iso2.radiodiagnostico.modelo.cita.ServicioConfirmacionCitasImpl;
import es.uclm.esi.iso2.radiodiagnostico.controlador.ControladorCitas;
import es.uclm.esi.iso2.radiodiagnostico.controlador.ControladorConfirmacionCitas;
import es.uclm.esi.iso2.radiodiagnostico.controlador.ControladorImagenes;
import es.uclm.esi.iso2.radiodiagnostico.controlador.ControladorInforme;
import es.uclm.esi.iso2.radiodiagnostico.controlador.ControladorPacientes;
import es.uclm.esi.iso2.radiodiagnostico.controlador.Rutas;
import es.uclm.esi.iso2.radiodiagnostico.controlador.RutasCitas;
import es.uclm.esi.iso2.radiodiagnostico.controlador.RutasConfirmacionCitas;
import es.uclm.esi.iso2.radiodiagnostico.controlador.RutasImagenes;
import es.uclm.esi.iso2.radiodiagnostico.controlador.RutasInforme;
import es.uclm.esi.iso2.radiodiagnostico.controlador.RutasPacientes;
import es.uclm.esi.iso2.radiodiagnostico.controlador.ControladorSolicitudSeguimiento;
import es.uclm.esi.iso2.radiodiagnostico.controlador.RutasSolicitudSeguimiento;
import es.uclm.esi.iso2.radiodiagnostico.modelo.informe.ImagenRepositorio;
import es.uclm.esi.iso2.radiodiagnostico.modelo.informe.ImagenRepositorioEnMemoria;
import es.uclm.esi.iso2.radiodiagnostico.modelo.informe.ImagenRepositorioMySql;
import es.uclm.esi.iso2.radiodiagnostico.modelo.informe.ServicioImagenesImpl;
import es.uclm.esi.iso2.radiodiagnostico.modelo.informe.ServicioInforme;
import es.uclm.esi.iso2.radiodiagnostico.modelo.informe.ServicioInformeImpl;
import es.uclm.esi.iso2.radiodiagnostico.modelo.informe.SistemaPacsSimulado;
import es.uclm.esi.iso2.radiodiagnostico.modelo.paciente.PacienteRepositorio;
import es.uclm.esi.iso2.radiodiagnostico.modelo.paciente.PacienteRepositorioEnMemoria;
import es.uclm.esi.iso2.radiodiagnostico.modelo.paciente.PacienteRepositorioMySql;
import es.uclm.esi.iso2.radiodiagnostico.modelo.paciente.ServicioPacientes;
import es.uclm.esi.iso2.radiodiagnostico.modelo.paciente.ServicioPacientesImpl;
import es.uclm.esi.iso2.radiodiagnostico.modelo.prueba.CatalogoTiposPruebaSimulado;
import es.uclm.esi.iso2.radiodiagnostico.modelo.prueba.RegistroMedicosSimulado;
import es.uclm.esi.iso2.radiodiagnostico.modelo.prueba.ServicioSolicitudSeguimientoImpl;
import es.uclm.esi.iso2.radiodiagnostico.modelo.prueba.SolicitudSeguimientoRepositorioEnMemoria;
import es.uclm.esi.iso2.radiodiagnostico.modelo.sistemasexternos.HistorialClinicoSimulado;
import es.uclm.esi.iso2.radiodiagnostico.modelo.solicitud.EstadoSolicitud;
import es.uclm.esi.iso2.radiodiagnostico.modelo.solicitud.PrioridadSolicitud;
import es.uclm.esi.iso2.radiodiagnostico.modelo.solicitud.SolicitudPrueba;
import es.uclm.esi.iso2.radiodiagnostico.modelo.solicitud.SolicitudRepositorio;
import es.uclm.esi.iso2.radiodiagnostico.modelo.solicitud.SolicitudRepositorioEnMemoria;
import io.javalin.Javalin;
import io.javalin.json.JavalinJackson;

/**
 * Punto de entrada del servidor: monta la API REST (D-009) e inyecta las dependencias.
 *
 * <p>Variables de entorno:
 * <ul>
 *   <li>{@code PUERTO}: puerto HTTP (por defecto 7070).</li>
 *   <li>{@code MYSQL_URL}, {@code MYSQL_USER}, {@code MYSQL_PASSWORD}: conexión a MySQL (D-010).
 *       Si no se define {@code MYSQL_URL}, los pacientes, las citas y las imágenes se guardan en memoria.</li>
 * </ul>
 *
 * <p>Las solicitudes (CdU25) están en memoria, con solicitudes de ejemplo, hasta que exista el
 * CdU12 (solicitar prueba).
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
        // Un único repositorio de citas para todos los casos de uso de citas
        // (CdU1 consultar, CdU25 asignar, CdU2 confirmar...), para que vean las mismas citas.
        CitaRepositorio citas = repositorioCitas();

        crear(
                new RutasPacientes(new ControladorPacientes(
                        new ServicioPacientesImpl(repositorioPacientes()))),

                new RutasImagenes(new ControladorImagenes(
                        new ServicioImagenesImpl(
                                repositorioImagenes(),
                                new SistemaPacsSimulado()))),

                new RutasInforme(new ControladorInforme(
                        new ServicioInformeImpl(
                                new HistorialClinicoSimulado()))),



                new RutasSolicitudSeguimiento(new ControladorSolicitudSeguimiento(
                        new ServicioSolicitudSeguimientoImpl(
                                new SolicitudSeguimientoRepositorioEnMemoria(),
                                new CatalogoTiposPruebaSimulado(),
                                new RegistroMedicosSimulado()))),
          
                new RutasCitas(new ControladorCitas(servicioCitas(citas))),

                new RutasConfirmacionCitas(new ControladorConfirmacionCitas(
                        new ServicioConfirmacionCitasImpl(citas)))
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

    private static ServicioCitas servicioCitas(CitaRepositorio citas) {
        // Solicitudes de ejemplo para poder asignar citas (CdU25) hasta que exista el CdU12.
        return new ServicioCitasImpl(
                solicitudesDeEjemplo(),
                citas
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

    private static ImagenRepositorio repositorioImagenes() {
        String url = System.getenv("MYSQL_URL");
        if (url == null || url.isBlank()) {
            System.err.println("MYSQL_URL no está definida: las imágenes se guardarán en memoria.");
            return new ImagenRepositorioEnMemoria();
        }
        return new ImagenRepositorioMySql();
    }

    /**
     * Solicitudes de ejemplo para poder probar el CdU25 hasta que exista el CdU12.
     * Los DNI son ficticios (RGPD) pero válidos: con MySQL, la tabla cita exige que el paciente
     * exista, así que hay que darlos de alta antes con el CdU22.
     */
    static SolicitudRepositorio solicitudesDeEjemplo() {
        SolicitudRepositorioEnMemoria solicitudes = new SolicitudRepositorioEnMemoria();
        solicitudes.guardar(new SolicitudPrueba("SOL-001", "12345678Z", "Radiografía",
                PrioridadSolicitud.ORDINARIO, EstadoSolicitud.PENDIENTE));
        solicitudes.guardar(new SolicitudPrueba("SOL-002", "01234567L", "TAC",
                PrioridadSolicitud.URGENTE, EstadoSolicitud.PENDIENTE));
        solicitudes.guardar(new SolicitudPrueba("SOL-003", "87654321X", "Ecografía",
                PrioridadSolicitud.PREFERENTE, EstadoSolicitud.PENDIENTE));
        return solicitudes;
    }

    private static int puerto() {
        String puerto = System.getenv("PUERTO");

        return puerto == null || puerto.isBlank()
                ? PUERTO_POR_DEFECTO
                : Integer.parseInt(puerto);
    }
}
