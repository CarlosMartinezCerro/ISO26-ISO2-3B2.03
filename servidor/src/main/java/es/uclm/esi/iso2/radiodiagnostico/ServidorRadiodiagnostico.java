package es.uclm.esi.iso2.radiodiagnostico;

import es.uclm.esi.iso2.radiodiagnostico.api.ApiRest;
import es.uclm.esi.iso2.radiodiagnostico.cita.CitaRepositorioEnMemoria;
import es.uclm.esi.iso2.radiodiagnostico.cita.ServicioCitasImpl;
import es.uclm.esi.iso2.radiodiagnostico.controlador.ControladorCitas;
import es.uclm.esi.iso2.radiodiagnostico.controlador.ControladorImagenes;
import es.uclm.esi.iso2.radiodiagnostico.informe.ImagenRepositorio;
import es.uclm.esi.iso2.radiodiagnostico.informe.ImagenRepositorioEnMemoria;
import es.uclm.esi.iso2.radiodiagnostico.informe.ImagenRepositorioMySql;
import es.uclm.esi.iso2.radiodiagnostico.informe.ServicioImagenesImpl;
import es.uclm.esi.iso2.radiodiagnostico.informe.SistemaPacsSimulado;
import es.uclm.esi.iso2.radiodiagnostico.solicitud.EstadoSolicitud;
import es.uclm.esi.iso2.radiodiagnostico.solicitud.PrioridadSolicitud;
import es.uclm.esi.iso2.radiodiagnostico.solicitud.SolicitudPrueba;
import es.uclm.esi.iso2.radiodiagnostico.solicitud.SolicitudRepositorioEnMemoria;
import io.javalin.Javalin;
import java.util.Map;

/**
 * Arranca el servidor REST (D-009) con Javalin.
 * Puerto: variable de entorno PUERTO (por defecto 8080).
 * Imágenes: en MySQL si está definida MYSQL_URL (D-010); si no, en memoria.
 * Citas y solicitudes: en memoria con solicitudes ficticias, hasta tener su repositorio MySQL.
 */
public final class ServidorRadiodiagnostico {

    static final int PUERTO_POR_DEFECTO = 8080;

    private ServidorRadiodiagnostico() {
    }

    public static void main(String[] args) {
        iniciar(System.getenv());
    }

    /** Crea la API según las variables de entorno (PUERTO, MYSQL_URL) y arranca Javalin. */
    static Javalin iniciar(Map<String, String> entorno) {
        int puerto = puerto(entorno.get("PUERTO"));
        Javalin servidor = arrancar(crearApi(entorno.get("MYSQL_URL") != null), puerto);
        System.out.println("Servidor de radiodiagnóstico escuchando en http://localhost:" + puerto + "/api");
        return servidor;
    }

    /** Declara las rutas (en Javalin 7 se hace en config.routes) y arranca. */
    public static Javalin arrancar(ApiRest api, int puerto) {
        String json = "application/json; charset=utf-8";
        return Javalin.create(config -> {
            config.routes.get("/api/solicitudes/pendientes", ctx -> {
                ctx.contentType(json);
                ctx.result(api.solicitudesPendientes());
            });
            config.routes.post("/api/citas", ctx -> {
                ctx.contentType(json);
                ctx.result(api.asignarCita(ctx.body()));
            });
            config.routes.get("/api/pacientes/{dni}/imagenes", ctx -> {
                ctx.contentType(json);
                ctx.result(api.imagenesPaciente(ctx.header(ApiRest.CABECERA_MEDICO), ctx.pathParam("dni")));
            });
            config.routes.get("/api/imagenes/{id}", ctx -> {
                ctx.contentType(json);
                ctx.result(api.consultarImagen(ctx.header(ApiRest.CABECERA_MEDICO), ctx.pathParam("id")));
            });
            config.routes.post("/api/imagenes", ctx -> {
                ctx.contentType(json);
                ctx.result(api.crearImagen(ctx.body()));
            });
        }).start(puerto);
    }

    static ApiRest crearApi(boolean conMySql) {
        SolicitudRepositorioEnMemoria solicitudes = new SolicitudRepositorioEnMemoria();
        solicitudes.guardar(new SolicitudPrueba("SOL-001", "FICTICIO-001", "Radiografía",
                PrioridadSolicitud.ORDINARIO, EstadoSolicitud.PENDIENTE));
        solicitudes.guardar(new SolicitudPrueba("SOL-002", "FICTICIO-002", "TAC",
                PrioridadSolicitud.URGENTE, EstadoSolicitud.PENDIENTE));
        solicitudes.guardar(new SolicitudPrueba("SOL-003", "FICTICIO-003", "Ecografía",
                PrioridadSolicitud.PREFERENTE, EstadoSolicitud.PENDIENTE));
        ControladorCitas citas = new ControladorCitas(
                new ServicioCitasImpl(solicitudes, new CitaRepositorioEnMemoria()));

        ImagenRepositorio repositorioImagenes = conMySql
                ? new ImagenRepositorioMySql()
                : new ImagenRepositorioEnMemoria();
        ControladorImagenes imagenes = new ControladorImagenes(
                new ServicioImagenesImpl(repositorioImagenes, new SistemaPacsSimulado()));
        return new ApiRest(citas, imagenes);
    }

    static int puerto(String valor) {
        if (valor == null || valor.isBlank()) {
            return PUERTO_POR_DEFECTO;
        }
        return Integer.parseInt(valor.trim());
    }
}
