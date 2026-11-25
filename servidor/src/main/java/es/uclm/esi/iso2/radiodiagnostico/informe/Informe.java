package es.uclm.esi.iso2.radiodiagnostico.informe;

import java.time.LocalDate;

public record Informe(
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

    public Informe(String dniMedico, String dniPaciente, String especialidad,
                   String motivoConsulta, String consultaActual, String diagnostico,
                   String pruebas, String tratamiento, boolean alta,
                   LocalDate fecha, String anamnesis) {
        this(0L, dniMedico, dniPaciente, especialidad, motivoConsulta, "",
                "", consultaActual, diagnostico, pruebas, tratamiento,
                alta, fecha, anamnesis);
    }

    public Informe conId(long nuevoId) {
        return new Informe(nuevoId, dniMedico, dniPaciente, especialidad,
                motivoConsulta, antecedentes, historialEspecialidad,
                consultaActual, diagnostico, pruebas, tratamiento,
                alta, fecha, anamnesis);
    }

    public Informe conAntecedentes(String nuevosAntecedentes,
                                   String nuevoHistorialEspecialidad) {
        return new Informe(id, dniMedico, dniPaciente, especialidad,
                motivoConsulta, nuevosAntecedentes, nuevoHistorialEspecialidad,
                consultaActual, diagnostico, pruebas, tratamiento,
                alta, fecha, anamnesis);
    }
}
