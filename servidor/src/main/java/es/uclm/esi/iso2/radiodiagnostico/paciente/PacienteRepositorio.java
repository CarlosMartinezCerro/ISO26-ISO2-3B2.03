package es.uclm.esi.iso2.radiodiagnostico.paciente;

import java.util.Optional;

public interface PacienteRepositorio {

    void guardar(Paciente paciente);

    Optional<Paciente> buscarPorDni(String dni);
}
