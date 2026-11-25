package es.uclm.esi.iso2.radiodiagnostico.paciente;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Traduce los códigos del apartado 4.1 de docs/testing/CdU22-diseno-pruebas.md (por ejemplo
 * {@code "D1 N1 A1 F1 K2 T1 R1"}) a valores de prueba. Todos los datos son ficticios (RGPD).
 * El paciente esperado está escrito a mano: no se calcula con las reglas del código que se prueba.
 */
final class CasoAlta {

    static final LocalDate H = LocalDate.of(2026, 11, 18);
    private static final ZoneId ZONA = ZoneId.of("Europe/Madrid");
    private static final String NULO = null;

    private static final String[] DNI = {"12345678Z", " 12345678z ", "00000000T", "99999998T",
            "99999999R", NULO, "", " ", "1234567Z", "100000000W", "123456789", "X1234567L",
            "12345678A", "1".repeat(256)};
    private static final String[] DNI_ESPERADO =
            {"12345678Z", "12345678Z", "00000000T", "99999998T", "99999999R"};
    private static final String[] NOMBRE = {"Alex", " Alex ", "A", "a".repeat(254), "a".repeat(255),
            "a".repeat(256), NULO, "", " ", "\u0001"};
    private static final String[] NOMBRE_ESPERADO =
            {"Alex", "Alex", "A", "a".repeat(254), "a".repeat(255)};
    private static final String[] APELLIDOS = {"Ejemplo", " Ejemplo ", "A", "a".repeat(254),
            "a".repeat(255), "a".repeat(256), NULO, "", " ", "\u0001"};
    private static final String[] APELLIDOS_ESPERADOS =
            {"Ejemplo", "Ejemplo", "A", "a".repeat(254), "a".repeat(255)};
    private static final LocalDate[] FECHA = {LocalDate.of(1990, 5, 12), H.minusDays(1), H,
            H.plusDays(1), null, LocalDate.of(1, 1, 1)};
    private static final LocalDate[] HOY = {H.minusDays(1), H, H.plusDays(1)};
    private static final String[] TELEFONO = {"600000001", NULO, "", " ", " 600000001 ", "60000000",
            "6000000010", "60000000A", "+34600000001", "6".repeat(256)};
    private static final String[] TELEFONO_ESPERADO = {"600000001", NULO, NULO, NULO, "600000001"};

    final DatosAltaPaciente datos;
    final Clock reloj;
    final Repositorio repositorio;
    private final Map<Character, Integer> codigos = new HashMap<>();

    CasoAlta(String texto) {
        for (String codigo : texto.trim().split(" ")) {
            codigos.put(codigo.charAt(0), Integer.parseInt(codigo.substring(1)) - 1);
        }
        datos = new DatosAltaPaciente(DNI[valor('D')], NOMBRE[valor('N')], APELLIDOS[valor('A')],
                FECHA[valor('F')], TELEFONO[valor('T')]);
        reloj = Clock.fixed(HOY[valor('K')].atTime(LocalTime.NOON).atZone(ZONA).toInstant(), ZONA);
        repositorio = new Repositorio(valor('R') + 1, dniNormalizado());
    }

    /** Paciente que debe devolver y guardar el alta; solo para casos con todos los datos válidos. */
    Paciente pacienteEsperado() {
        return new Paciente(DNI_ESPERADO[valor('D')], NOMBRE_ESPERADO[valor('N')],
                APELLIDOS_ESPERADOS[valor('A')], FECHA[valor('F')], TELEFONO_ESPERADO[valor('T')]);
    }

    private String dniNormalizado() {
        return valor('D') < DNI_ESPERADO.length ? DNI_ESPERADO[valor('D')] : "12345678Z";
    }

    private int valor(char variable) {
        return codigos.get(variable);
    }

    /** Doble del repositorio en los estados R1 a R5 del diseño. */
    static final class Repositorio implements PacienteRepositorio {
        final Map<String, Paciente> pacientes = new HashMap<>();
        private final int estado;

        Repositorio(int estado, String dni) {
            this.estado = estado;
            if (estado == 2) {
                pacientes.put(dni, new Paciente(dni, "Sam", "Prueba",
                        LocalDate.of(1992, 8, 24), "600000002"));
            }
        }

        @Override
        public void guardar(Paciente paciente) {
            if (estado == 4) {
                throw new IllegalStateException("No se pudo guardar el paciente.");
            }
            if (estado == 5) {
                throw new PacienteDuplicadoException("Ya existe un paciente con el DNI "
                        + paciente.dni() + ".");
            }
            pacientes.put(paciente.dni(), paciente);
        }

        @Override
        public Optional<Paciente> buscarPorDni(String dni) {
            if (estado == 3) {
                throw new IllegalStateException("No se pudo buscar el paciente.");
            }
            return Optional.ofNullable(pacientes.get(dni));
        }
    }
}
