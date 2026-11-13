package es.uclm.esi.iso2.radiodiagnostico.paciente;

import java.util.Optional;

public interface ServicioPacientes {

    /**
     * Da de alta a un paciente.
     *
     * @throws DatosInvalidosException si falta algún dato obligatorio o es incorrecto
     * @throws PacienteDuplicadoException si ya existe un paciente con ese DNI
     */
    Paciente darDeAlta(DatosAltaPaciente datos);

    Optional<Paciente> buscarPorDni(String dni);
}
