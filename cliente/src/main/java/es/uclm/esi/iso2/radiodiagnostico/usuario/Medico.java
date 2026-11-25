package es.uclm.esi.iso2.radiodiagnostico.usuario;

import java.time.LocalDate;

public record Medico(String nombre, String apellido, String dni, String numeroTelefono,
                     String numeroSeguridadSocial, LocalDate fechaNacimiento,
                     String domicilio, String especialidad) {
}
