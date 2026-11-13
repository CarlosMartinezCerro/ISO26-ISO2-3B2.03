package es.uclm.esi.iso2.radiodiagnostico.paciente;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class PacienteRepositorioEnMemoria implements PacienteRepositorio {

    private final Map<String, Paciente> pacientes = new HashMap<>();

    @Override
    public void guardar(Paciente paciente) {
        pacientes.put(paciente.dni(), paciente);
    }

    @Override
    public Optional<Paciente> buscarPorDni(String dni) {
        return Optional.ofNullable(pacientes.get(dni));
    }
}
