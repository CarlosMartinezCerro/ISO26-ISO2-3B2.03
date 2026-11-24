package es.uclm.esi.iso2.radiodiagnostico.controlador;

import java.time.LocalDate;
import java.util.Objects;
import java.util.List;
import java.util.Map;

import es.uclm.esi.iso2.radiodiagnostico.informe.DatosCreacionInforme;
import es.uclm.esi.iso2.radiodiagnostico.informe.DatosInformeInvalidosException;
import es.uclm.esi.iso2.radiodiagnostico.informe.DniPacienteInvalidoException;
import es.uclm.esi.iso2.radiodiagnostico.informe.HistorialClinicoNoDisponibleException;
import es.uclm.esi.iso2.radiodiagnostico.informe.Informe;
import es.uclm.esi.iso2.radiodiagnostico.informe.InformeNoEncontradoException;
import es.uclm.esi.iso2.radiodiagnostico.informe.InformeRespuesta;
import es.uclm.esi.iso2.radiodiagnostico.informe.InformeResumen;
import es.uclm.esi.iso2.radiodiagnostico.informe.ResultadoAntecedentes;
import es.uclm.esi.iso2.radiodiagnostico.informe.ResultadoCreacionInforme;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

/** API REST para crear informes y consultar antecedentes. */
public class RutasInforme implements Rutas {

    public static final String RUTA_INFORMES = "/informes";
    public static final String RUTA_ANTECEDENTES = "/informes/antecedentes";
    static final String PETICION_MAL_FORMADA = "La peticion de informe no tiene un formato valido.";
    static final String SISTEMA_NO_DISPONIBLE =
            "El sistema externo de historial clinico no esta disponible.";

    private final ControladorInforme controlador;

    public RutasInforme(ControladorInforme controlador) {
        this.controlador = Objects.requireNonNull(controlador, "El controlador es obligatorio");
    }

    @Override
    public void registrar(Javalin app) {
        app.post(RUTA_INFORMES, this::crearInforme);
        app.get(RUTA_ANTECEDENTES, this::obtenerAntecedentes);
        app.get(RUTA_INFORMES, this::listarInformes);
        app.get(RUTA_INFORMES + "/{idInforme}", this::obtenerInforme);
    }

    private void crearInforme(Context ctx) {
        DatosCreacionInforme datos;
        try {
            datos = ctx.bodyAsClass(DatosCreacionInforme.class);
        } catch (Exception excepcion) {
            ctx.status(HttpStatus.BAD_REQUEST).json(
                    new ResultadoCreacionInforme(false, PETICION_MAL_FORMADA, null));
            return;
        }

        if (datos == null
                || textoAusente(datos.dniPacienteReal())
                || textoAusente(datos.especialidad())
                || textoAusente(datos.consultaActual())
                || textoAusente(datos.diagnostico())
                || datos.fecha() == null) {
            ctx.status(HttpStatus.BAD_REQUEST).json(new ResultadoCreacionInforme(
                    false, "dniPaciente, especialidad, consultaActual, diagnostico y fecha son obligatorios.",
                    null));
            return;
        }

        try {
            Informe informe = new Informe(
                    textoAusente(datos.dniMedicoReal()) ? "" : datos.dniMedicoReal().trim(),
                    datos.dniPacienteReal().trim(),
                    datos.especialidad().trim(),
                    valor(datos.motivoConsulta()),
                    datos.consultaActual().trim(),
                    datos.diagnostico().trim(),
                    valor(datos.pruebas()),
                    valor(datos.tratamiento()),
                    Boolean.TRUE.equals(datos.alta()),
                    datos.fecha(),
                    valor(datos.anamnesis()));
            informe = informe.conAntecedentes(
                    valor(datos.antecedentes()),
                    valor(datos.historialEspecialidad()));

            Informe creado = controlador.crearInforme(informe);
            ctx.status(HttpStatus.CREATED).json(new ResultadoCreacionInforme(
                    true, "Informe creado correctamente.", aRespuesta(creado)));
        } catch (DatosInformeInvalidosException
                 | DniPacienteInvalidoException excepcion) {
            ctx.status(HttpStatus.BAD_REQUEST).json(new ResultadoCreacionInforme(
                    false, excepcion.getMessage(), null));
        } catch (HistorialClinicoNoDisponibleException excepcion) {
            ctx.status(HttpStatus.SERVICE_UNAVAILABLE).json(new ResultadoCreacionInforme(
                    false, SISTEMA_NO_DISPONIBLE, null));
        } catch (IllegalStateException excepcion) {
            ctx.status(HttpStatus.SERVICE_UNAVAILABLE).json(new ResultadoCreacionInforme(
                    false, SISTEMA_NO_DISPONIBLE, null));
        }
    }

    private void obtenerAntecedentes(Context ctx) {
        String dni = ctx.queryParam("dniPaciente");
        String especialidad = ctx.queryParam("especialidad");
        if (textoAusente(dni) || textoAusente(especialidad)) {
            ctx.status(HttpStatus.BAD_REQUEST).json(new ResultadoAntecedentes(
                    false, "dniPaciente y especialidad son obligatorios.", null, null));
            return;
        }

        try {
            Informe informe = new Informe(
                    0L, "", dni.trim(), especialidad.trim(), "", "",
                    "", "", "", "", "", false, LocalDate.now(), "");
            Informe actualizado = controlador.obtenerAntecedentes(informe);
            ctx.status(HttpStatus.OK).json(new ResultadoAntecedentes(
                    true, "Antecedentes obtenidos correctamente.",
                    actualizado.antecedentes(), actualizado.historialEspecialidad()));
        } catch (DatosInformeInvalidosException
                 | DniPacienteInvalidoException excepcion) {
            ctx.status(HttpStatus.BAD_REQUEST).json(new ResultadoAntecedentes(
                    false, excepcion.getMessage(), null, null));
        } catch (HistorialClinicoNoDisponibleException excepcion) {
            ctx.status(HttpStatus.SERVICE_UNAVAILABLE).json(new ResultadoAntecedentes(
                    false, SISTEMA_NO_DISPONIBLE, null, null));
        } catch (IllegalStateException excepcion) {
            ctx.status(HttpStatus.SERVICE_UNAVAILABLE).json(new ResultadoAntecedentes(
                    false, SISTEMA_NO_DISPONIBLE, null, null));
        }
    }

    private void listarInformes(Context ctx) {
        String dni = ctx.queryParam("dniPaciente");
        if (textoAusente(dni)) {
            ctx.status(HttpStatus.BAD_REQUEST).json(Map.of(
                    "correcto", false,
                    "mensaje", "dniPaciente es obligatorio."));
            return;
        }
        try {
            List<InformeResumen> informes = controlador.listarInformes(dni.trim());
            ctx.status(HttpStatus.OK).json(informes);
        } catch (DniPacienteInvalidoException
                 | DatosInformeInvalidosException excepcion) {
            ctx.status(HttpStatus.BAD_REQUEST).json(Map.of(
                    "correcto", false, "mensaje", excepcion.getMessage()));
        } catch (HistorialClinicoNoDisponibleException excepcion) {
            ctx.status(HttpStatus.SERVICE_UNAVAILABLE).json(Map.of(
                    "correcto", false, "mensaje", SISTEMA_NO_DISPONIBLE));
        } catch (IllegalStateException excepcion) {
            ctx.status(HttpStatus.SERVICE_UNAVAILABLE).json(Map.of(
                    "correcto", false, "mensaje", SISTEMA_NO_DISPONIBLE));
        }
    }

    private void obtenerInforme(Context ctx) {
        final long id;
        try {
            id = Long.parseLong(ctx.pathParam("idInforme").trim());
        } catch (Exception excepcion) {
            ctx.status(HttpStatus.BAD_REQUEST).json(Map.of(
                    "correcto", false, "mensaje", "idInforme no es valido."));
            return;
        }
        if (id <= 0) {
            ctx.status(HttpStatus.BAD_REQUEST).json(Map.of(
                    "correcto", false, "mensaje", "idInforme no es valido."));
            return;
        }
        try {
            Informe informe = controlador.obtenerInforme(id);
            ctx.status(HttpStatus.OK).json(aRespuesta(informe));
        } catch (InformeNoEncontradoException excepcion) {
            ctx.status(HttpStatus.NOT_FOUND).json(Map.of(
                    "correcto", false, "mensaje", excepcion.getMessage()));
        } catch (DatosInformeInvalidosException
                 | DniPacienteInvalidoException excepcion) {
            ctx.status(HttpStatus.BAD_REQUEST).json(Map.of(
                    "correcto", false, "mensaje", excepcion.getMessage()));
        } catch (HistorialClinicoNoDisponibleException excepcion) {
            ctx.status(HttpStatus.SERVICE_UNAVAILABLE).json(Map.of(
                    "correcto", false, "mensaje", SISTEMA_NO_DISPONIBLE));
        } catch (IllegalStateException excepcion) {
            ctx.status(HttpStatus.SERVICE_UNAVAILABLE).json(Map.of(
                    "correcto", false, "mensaje", SISTEMA_NO_DISPONIBLE));
        }
    }

    private static InformeRespuesta aRespuesta(Informe informe) {
        if (informe == null) {
            return null;
        }
        return new InformeRespuesta(
                informe.id(),
                informe.dniMedico(),
                informe.dniPaciente(),
                informe.especialidad(),
                informe.motivoConsulta(),
                informe.antecedentes(),
                informe.historialEspecialidad(),
                informe.consultaActual(),
                informe.diagnostico(),
                informe.pruebas(),
                informe.tratamiento(),
                informe.alta(),
                informe.fecha(),
                informe.anamnesis());
    }

    private static boolean textoAusente(String texto) {
        return texto == null || texto.isBlank();
    }

    private static String valor(String texto) {
        return texto == null ? "" : texto.trim();
    }
}
