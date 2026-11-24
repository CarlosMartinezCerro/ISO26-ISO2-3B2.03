package es.uclm.esi.iso2.radiodiagnostico.modelo.prueba;

import es.uclm.esi.iso2.radiodiagnostico.modelo.solicitud.PrioridadSolicitud;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Traduce los códigos del apartado 4.1 de docs/testing/CdU13-diseno-pruebas.md (por ejemplo
 * {@code "D1 M1 G1 T1 C1 I1 J1 P3 N1 U3 F3 K2 E1 B1"}) a valores de prueba. Todos los datos son
 * ficticios (RGPD). La fecha límite se recibe ya escrita: no se calcula con las reglas del código.
 */
final class CasoSeguimiento {

    private static final LocalDate H = LocalDate.of(2026, 11, 18);
    private static final ZoneId ZONA = ZoneId.of("Europe/Madrid");
    private static final String NULO = null;

    private static final String[] DNI_PACIENTE = {"12345678Z", " 12345678z ", NULO, "", "  ", "\u0001",
            "12345678A", "abc"};
    private static final String[] DNI_MEDICO = {"00000001R", " 00000001r ", NULO, "", "  "};
    private static final String[] TIPO = {"TAC", " Mamografía ", NULO, "", "  "};
    private static final String[] TIPO_ESPERADO = {"TAC", "Mamografía"};
    private static final String[] INDICACION = {"Control de nódulo pulmonar (caso ficticio)", " Cribado ",
            "a", NULO, "", "  ", "\u0001", "a".repeat(10_000)};
    private static final String[] JUSTIFICACION = {"Seguimiento de evolución (caso ficticio)", " Programa ",
            "a", NULO, "", "  ", "\u0001", "a".repeat(10_000)};
    private static final PrioridadSolicitud[] PRIORIDAD = {PrioridadSolicitud.URGENTE,
            PrioridadSolicitud.PREFERENTE, PrioridadSolicitud.ORDINARIO, null};
    private static final Integer[] CADA = {3, 1, 2, 0, -2, null, 2_147_483_647, -2_147_483_648};
    private static final UnidadPeriodicidad[] UNIDAD = {UnidadPeriodicidad.DIAS,
            UnidadPeriodicidad.SEMANAS, UnidadPeriodicidad.MESES, null};
    private static final LocalDate[] INICIO = {LocalDate.of(2026, 12, 1), LocalDate.of(2026, 11, 17), H,
            LocalDate.of(2026, 11, 19), null, LocalDate.MAX, LocalDate.of(2027, 1, 31)};
    private static final LocalDate[] HOY = {LocalDate.of(2026, 11, 17), H, LocalDate.of(2026, 11, 19)};
    /** E1 a E10; de E11 en adelante el fin es la fecha límite. */
    private static final Integer[] REPETICIONES = {4, 1, 2, 99, 100, 101, 0, -3, null, 3};

    final DatosSolicitudSeguimiento datos;
    final Repositorio repositorio;
    private final Clock reloj;
    private final Map<Character, Integer> codigos = new HashMap<>();

    CasoSeguimiento(String texto, LocalDate fechaLimite) {
        for (String codigo : texto.trim().split(" ")) {
            codigos.put(codigo.charAt(0), Integer.parseInt(codigo.substring(1)) - 1);
        }
        datos = new DatosSolicitudSeguimiento(DNI_PACIENTE[valor('D')], DNI_MEDICO[valor('M')],
                TIPO[valor('T')], INDICACION[valor('I')], JUSTIFICACION[valor('J')],
                PRIORIDAD[valor('P')], CADA[valor('N')], UNIDAD[valor('U')], INICIO[valor('F')],
                valor('E') < REPETICIONES.length ? REPETICIONES[valor('E')] : null, fechaLimite);
        reloj = Clock.fixed(HOY[valor('K')].atTime(LocalTime.NOON).atZone(ZONA).toInstant(), ZONA);
        repositorio = new Repositorio(valor('B') == 1);
    }

    ServicioSolicitudSeguimiento servicio() {
        return new ServicioSolicitudSeguimientoImpl(repositorio, new Catalogo(valor('C') + 1),
                new Registro(valor('G') + 1), reloj);
    }

    /** Solicitud que debe crearse, con el id y las repeticiones indicados; solo para casos válidos. */
    SolicitudSeguimiento esperada(String id, List<RepeticionSeguimiento> repeticiones) {
        return new SolicitudSeguimiento(id, "12345678Z", "00000001R", TIPO_ESPERADO[valor('T')],
                recortado(INDICACION, "Cribado", 'I'), recortado(JUSTIFICACION, "Programa", 'J'),
                datos.prioridad(), new PeriodicidadSeguimiento(datos.cada(), datos.unidad()),
                datos.fechaInicio(), datos.numeroRepeticiones(), datos.fechaLimite(), repeticiones);
    }

    /** El valor 2 es el único con espacios en los extremos. */
    private String recortado(String[] valores, String sinEspacios, char variable) {
        return valor(variable) == 1 ? sinEspacios : valores[valor(variable)];
    }

    private int valor(char variable) {
        return codigos.get(variable);
    }

    /** B1 guarda; B2 falla al guardar. */
    static final class Repositorio implements SolicitudSeguimientoRepositorio {
        final List<SolicitudSeguimiento> guardadas = new ArrayList<>();
        private final boolean falla;

        Repositorio(boolean falla) {
            this.falla = falla;
        }

        @Override
        public void guardar(SolicitudSeguimiento solicitud) {
            if (falla) {
                throw new IllegalStateException("No se pudo guardar la solicitud.");
            }
            guardadas.add(solicitud);
        }

        @Override
        public Optional<SolicitudSeguimiento> buscarPorId(String id) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<SolicitudSeguimiento> buscarPorPaciente(String dniPaciente) {
            throw new UnsupportedOperationException();
        }
    }

    /** C1 existe y admite; C2 no existe; C3 no admite seguimiento; C4 falla. */
    private record Catalogo(int estado) implements CatalogoTiposPrueba {
        @Override
        public boolean existe(String tipoPrueba) {
            fallarSiProcede();
            return estado != 2;
        }

        @Override
        public boolean admiteSeguimiento(String tipoPrueba) {
            fallarSiProcede();
            return estado == 1;
        }

        private void fallarSiProcede() {
            if (estado == 4) {
                throw new IllegalStateException("Catálogo no disponible.");
            }
        }
    }

    /** G1 es médico; G2 no lo es; G3 falla. */
    private record Registro(int estado) implements RegistroMedicos {
        @Override
        public boolean esMedico(String dni) {
            if (estado == 3) {
                throw new IllegalStateException("Registro no disponible.");
            }
            return estado == 1;
        }
    }
}
