package es.uclm.esi.iso2.radiodiagnostico.paciente;

import java.util.Objects;
import java.util.Optional;

public class ServicioPacientesImpl implements ServicioPacientes {

    private final PacienteRepositorio repositorio;

    public ServicioPacientesImpl(PacienteRepositorio repositorio) {
        this.repositorio = Objects.requireNonNull(repositorio, "El repositorio es obligatorio");
    }

    @Override
    public Paciente darDeAlta(DatosAltaPaciente datos) {
        if (datos == null) {
            throw new DatosInvalidosException("Los datos del paciente son obligatorios.");
        }
        validarTexto(datos.dni(), "dni");
        validarTexto(datos.nombre(), "nombre");
        validarTexto(datos.apellidos(), "apellidos");
        if (datos.fechaNacimiento() == null) {
            throw new DatosInvalidosException("El campo fechaNacimiento es obligatorio.");
        }
        if (repositorio.buscarPorDni(datos.dni()).isPresent()) {
            throw new PacienteDuplicadoException(
                    "Ya existe un paciente con el DNI " + datos.dni() + ".");
        }

        Paciente paciente = new Paciente(datos.dni(), datos.nombre(), datos.apellidos(),
                datos.fechaNacimiento(), datos.telefono());
        repositorio.guardar(paciente);
        return paciente;
    }

    @Override
    public Optional<Paciente> buscarPorDni(String dni) {
        return repositorio.buscarPorDni(dni);
    }

    private void validarTexto(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new DatosInvalidosException("El campo " + campo + " es obligatorio.");
        }
    }
}
