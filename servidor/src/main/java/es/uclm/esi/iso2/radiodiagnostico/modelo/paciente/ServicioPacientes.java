package es.uclm.esi.iso2.radiodiagnostico.modelo.paciente;

import java.util.Optional;

/** Contrato del servidor para el CdU22 · Dar de alta paciente. */
public interface ServicioPacientes {

    /**
     * Da de alta a un paciente. El DNI se guarda en mayúsculas y sin espacios.
     *
     * @throws DatosInvalidosException si falta algún dato obligatorio o su formato es incorrecto
     *         (DNI de 8 dígitos y letra de control, teléfono de 9 dígitos, fecha de nacimiento
     *         no posterior a hoy)
     * @throws PacienteDuplicadoException si ya existe un paciente con ese DNI
     */
    Paciente darDeAlta(DatosAltaPaciente datos);

    Optional<Paciente> buscarPorDni(String dni);
}
