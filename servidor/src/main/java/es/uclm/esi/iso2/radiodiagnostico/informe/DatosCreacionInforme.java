package es.uclm.esi.iso2.radiodiagnostico.informe;

import java.time.LocalDate;

/** Datos REST necesarios para crear un informe. */
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
        String historialEspecialidad,
        DatosPersona medico,
        DatosPersona paciente) {

    /** Datos personales minimos usados por JSON REST anidados. */
    public record DatosPersona(String dni) {
    }

    public String dniMedicoReal() {
        return dniMedico != null ? dniMedico
                : medico != null ? medico.dni() : null;
    }

    public String dniPacienteReal() {
        return dniPaciente != null ? dniPaciente
                : paciente != null ? paciente.dni() : null;
    }
}
