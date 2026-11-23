package es.uclm.esi.iso2.radiodiagnostico.informe;

import java.time.LocalDate;

/** Representacion REST de un informe, sin exponer entidades internas del dominio. */
public record InformeRespuesta(
        long id,
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

    public InformeRespuesta(String dniMedico, String dniPaciente, String especialidad,
                            String motivoConsulta, String antecedentes,
                            String historialEspecialidad, String consultaActual,
                            String diagnostico, String pruebas, String tratamiento,
                            boolean alta, LocalDate fecha, String anamnesis) {
        this(0L, dniMedico, dniPaciente, especialidad, motivoConsulta, antecedentes,
                historialEspecialidad, consultaActual, diagnostico, pruebas, tratamiento,
                alta, fecha, anamnesis);
    }

    public InformeRespuesta(String dniMedico, String dniPaciente, String especialidad,
                            String motivoConsulta, String antecedentes,
                            String historialEspecialidad, String consultaActual,
                            String diagnostico, String pruebas, String tratamiento,
                            boolean alta, LocalDate fecha, String anamnesis, long id) {
        this(id, dniMedico, dniPaciente, especialidad, motivoConsulta, antecedentes,
                historialEspecialidad, consultaActual, diagnostico, pruebas, tratamiento,
                alta, fecha, anamnesis);
    }
}
