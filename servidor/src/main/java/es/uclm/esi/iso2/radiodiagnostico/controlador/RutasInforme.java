package es.uclm.esi.iso2.radiodiagnostico.controlador;

import java.time.LocalDate;
import java.util.Objects;

import es.uclm.esi.iso2.radiodiagnostico.modelo.informe.DatosCreacionInforme;
import es.uclm.esi.iso2.radiodiagnostico.modelo.informe.Informe;
import es.uclm.esi.iso2.radiodiagnostico.modelo.informe.InformeRespuesta;
import es.uclm.esi.iso2.radiodiagnostico.modelo.informe.ResultadoAntecedentes;
import es.uclm.esi.iso2.radiodiagnostico.modelo.informe.ResultadoCreacionInforme;
import es.uclm.esi.iso2.radiodiagnostico.modelo.usuario.Medico;
import es.uclm.esi.iso2.radiodiagnostico.modelo.usuario.Paciente;
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
            Paciente paciente = new Paciente("", "", datos.dniPacienteReal().trim(),
                    "", "", null, "");
            String dniMedico = datos.dniMedicoReal();
            Medico medico = textoAusente(dniMedico)
                    ? null
                    : new Medico("", "", dniMedico.trim(), "", "",
                            null, "", datos.especialidad().trim());
            Informe informe = new Informe(
                    medico,
                    paciente,
                    datos.especialidad().trim(),
                    valor(datos.motivoConsulta()),
                    datos.consultaActual().trim(),
                    datos.diagnostico().trim(),
                    valor(datos.pruebas()),
                    valor(datos.tratamiento()),
                    Boolean.TRUE.equals(datos.alta()),
                    datos.fecha(),
                    valor(datos.anamnesis()));
            informe.setAntecedentes(valor(datos.antecedentes()));
            informe.setHistorialEspecialidad(valor(datos.historialEspecialidad()));

            Informe creado = controlador.crearInforme(informe);
            ctx.status(HttpStatus.CREATED).json(new ResultadoCreacionInforme(
                    true, "Informe creado correctamente.", aRespuesta(creado)));
        } catch (IllegalArgumentException excepcion) {
            ctx.status(HttpStatus.BAD_REQUEST).json(new ResultadoCreacionInforme(
                    false, excepcion.getMessage(), null));
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
            Paciente paciente = new Paciente("", "", dni.trim(), "", "", null, "");
            Informe informe = new Informe(null, paciente, especialidad.trim(), "", "", "", "",
                    "", false, LocalDate.now(), "");
            String antecedentes = controlador.obtenerAntecedentes(informe);
            ctx.status(HttpStatus.OK).json(new ResultadoAntecedentes(
                    true, "Antecedentes obtenidos correctamente.",
                    antecedentes, informe.obtenerHistorial()));
        } catch (IllegalArgumentException excepcion) {
            ctx.status(HttpStatus.BAD_REQUEST).json(new ResultadoAntecedentes(
                    false, excepcion.getMessage(), null, null));
        } catch (IllegalStateException excepcion) {
            ctx.status(HttpStatus.SERVICE_UNAVAILABLE).json(new ResultadoAntecedentes(
                    false, SISTEMA_NO_DISPONIBLE, null, null));
        }
    }

    private static InformeRespuesta aRespuesta(Informe informe) {
        if (informe == null) {
            return null;
        }
        String dniMedico = informe.getMedico() == null ? null : informe.getMedico().getDni();
        String dniPaciente = informe.getPaciente() == null ? null : informe.getPaciente().getDni();
        return new InformeRespuesta(
                dniMedico,
                dniPaciente,
                informe.getEspecialidad(),
                informe.getMotivoConsulta(),
                informe.obtenerAntecedentes(),
                informe.obtenerHistorial(),
                informe.getConsultaActual(),
                informe.getDiagnostico(),
                informe.getPruebas(),
                informe.gettratamiento(),
                informe.isAlta(),
                informe.getFecha(),
                informe.getAnamnesis());
    }

    private static boolean textoAusente(String texto) {
        return texto == null || texto.isBlank();
    }

    private static String valor(String texto) {
        return texto == null ? "" : texto.trim();
    }
}
