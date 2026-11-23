package es.uclm.esi.iso2.radiodiagnostico.modelo.informe;

import java.time.LocalDate;

/** Respuesta REST de un informe. */
public record InformeRespuesta(
        String dniMedico,
        String dniPaciente,
        String especialidad,
        String motivoConsulta,
        String antecedentes,
        String historialEspecialidad,
        String consultaActual,
        String diagnostico,
        String pruebas,
        String tratamiento,
        boolean alta,
        LocalDate fecha,
        String anamnesis) {
}
