package es.uclm.esi.iso2.radiodiagnostico.modelo.paciente;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

public class ServicioPacientesImpl implements ServicioPacientes {

    private static final Pattern FORMATO_DNI = Pattern.compile("\\d{8}[A-Z]");
    private static final Pattern FORMATO_TELEFONO = Pattern.compile("\\d{9}");
    private static final String LETRAS_DNI = "TRWAGMYFPDXBNJZSQVHLCKE";

    private final PacienteRepositorio repositorio;
    private final Clock reloj;

    public ServicioPacientesImpl(PacienteRepositorio repositorio) {
        this(repositorio, Clock.systemDefaultZone());
    }

    public ServicioPacientesImpl(PacienteRepositorio repositorio, Clock reloj) {
        this.repositorio = Objects.requireNonNull(repositorio, "El repositorio es obligatorio");
        this.reloj = Objects.requireNonNull(reloj, "El reloj es obligatorio");
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

        String dni = datos.dni().trim().toUpperCase(Locale.ROOT);
        validarDni(dni);
        validarFechaNacimiento(datos.fechaNacimiento());
        String telefono = normalizarTelefono(datos.telefono());

        if (repositorio.buscarPorDni(dni).isPresent()) {
            throw new PacienteDuplicadoException("Ya existe un paciente con el DNI " + dni + ".");
        }

        Paciente paciente = new Paciente(dni, datos.nombre().trim(), datos.apellidos().trim(),
                datos.fechaNacimiento(), telefono);
        repositorio.guardar(paciente);
        return paciente;
    }

    @Override
    public Optional<Paciente> buscarPorDni(String dni) {
        if (dni == null) {
            return Optional.empty();
        }
        return repositorio.buscarPorDni(dni.trim().toUpperCase(Locale.ROOT));
    }

    private void validarTexto(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new DatosInvalidosException("El campo " + campo + " es obligatorio.");
        }
    }

    private void validarDni(String dni) {
        if (!FORMATO_DNI.matcher(dni).matches()) {
            throw new DatosInvalidosException(
                    "El campo dni debe tener 8 dígitos seguidos de una letra.");
        }
        int numero = Integer.parseInt(dni.substring(0, 8));
        if (dni.charAt(8) != LETRAS_DNI.charAt(numero % 23)) {
            throw new DatosInvalidosException("La letra del campo dni no es correcta.");
        }
    }

    private void validarFechaNacimiento(LocalDate fechaNacimiento) {
        if (fechaNacimiento.isAfter(LocalDate.now(reloj))) {
            throw new DatosInvalidosException(
                    "El campo fechaNacimiento no puede ser posterior a hoy.");
        }
    }

    private String normalizarTelefono(String telefono) {
        if (telefono == null || telefono.isBlank()) {
            return null;
        }
        String limpio = telefono.trim();
        if (!FORMATO_TELEFONO.matcher(limpio).matches()) {
            throw new DatosInvalidosException("El campo telefono debe tener 9 dígitos.");
        }
        return limpio;
    }
}
