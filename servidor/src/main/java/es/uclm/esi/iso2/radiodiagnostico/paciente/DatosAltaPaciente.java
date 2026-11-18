package es.uclm.esi.iso2.radiodiagnostico.paciente;

import java.time.LocalDate;

public record DatosAltaPaciente(String dni, String nombre, String apellidos,
                                LocalDate fechaNacimiento, String telefono) {
}