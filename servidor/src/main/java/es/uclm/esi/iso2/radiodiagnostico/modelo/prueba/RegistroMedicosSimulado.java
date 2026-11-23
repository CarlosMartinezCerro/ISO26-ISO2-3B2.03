package es.uclm.esi.iso2.radiodiagnostico.modelo.prueba;

import java.util.Locale;
import java.util.Set;

/** Registro de médicos con DNIs ficticios, mientras no exista la gestión de usuarios. */
public class RegistroMedicosSimulado implements RegistroMedicos {

    /** DNIs ficticios (no corresponden a personas reales). */
    public static final Set<String> MEDICOS_FICTICIOS = Set.of("00000001R", "00000002W");

    private final Set<String> medicos;

    public RegistroMedicosSimulado() {
        this(MEDICOS_FICTICIOS);
    }

    public RegistroMedicosSimulado(Set<String> medicos) {
        this.medicos = Set.copyOf(medicos);
    }

    @Override
    public boolean esMedico(String dni) {
        return dni != null && medicos.contains(dni.trim().toUpperCase(Locale.ROOT));
    }
}
