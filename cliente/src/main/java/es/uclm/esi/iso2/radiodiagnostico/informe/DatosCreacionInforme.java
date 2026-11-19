package es.uclm.esi.iso2.radiodiagnostico.informe;

import java.time.LocalDate;

/** Datos enviados por el cliente para crear un informe. */
public record DatosCreacionInforme(
        String dniMedico,
        String dniPaciente,
        String especialidad,
        String motivoConsulta,
        String consultaActual,
        String diagnostico,
        String pruebas,
        String tratamiento,
        Boolean alta,
        LocalDate fecha,
        String anamnesis,
        String antecedentes,
        String historialEspecialidad) {
}
