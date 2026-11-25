package es.uclm.esi.iso2.radiodiagnostico.controlador;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.uclm.esi.iso2.radiodiagnostico.prueba.ClienteSeguimientos;
import es.uclm.esi.iso2.radiodiagnostico.prueba.DatosSolicitudSeguimiento;
import es.uclm.esi.iso2.radiodiagnostico.prueba.ErrorComunicacionException;
import es.uclm.esi.iso2.radiodiagnostico.prueba.PrioridadSeguimiento;
import es.uclm.esi.iso2.radiodiagnostico.prueba.RepeticionSeguimiento;
import es.uclm.esi.iso2.radiodiagnostico.prueba.ResultadoSolicitudSeguimiento;
import es.uclm.esi.iso2.radiodiagnostico.prueba.UnidadPeriodicidad;
import es.uclm.esi.iso2.radiodiagnostico.vista.VistaSolicitarSeguimiento;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * CdU13, unidades CA y CC, con vista y cliente falsos. Diseño: docs/testing/CdU13-diseno-pruebas.md.
 * Los casos de decisiones y MC/DC repiten los de each use (apartados 8 y 9).
 */
@DisplayName("CdU13 · ControladorSolicitarSeguimientoImpl")
class ControladorSolicitarSeguimientoImplCdU13Test {

    private static final String NULO = null;
    private static final LocalDate LIMITE = LocalDate.of(2027, 2, 20);
    private static final String[] DNI_PACIENTE = {"12345678Z", NULO, "", " "};
    private static final String[] DNI_MEDICO = {"00000001R", NULO, "", " "};
    private static final String[] TIPO = {"TAC", NULO, "", " "};
    private static final String[] INDICACION = {"Indicación ficticia", NULO, "", " "};
    private static final String[] JUSTIFICACION = {"Justificación ficticia", NULO, "", " "};
    private static final PrioridadSeguimiento[] PRIORIDAD = {PrioridadSeguimiento.URGENTE, null};
    private static final Integer[] CADA = {3, 1, 2, 0, -1, null};
    private static final UnidadPeriodicidad[] UNIDAD = {UnidadPeriodicidad.MESES, null};
    private static final LocalDate[] INICIO = {LocalDate.of(2026, 11, 20), null};
    private static final Integer[] REPETICIONES = {4, 1, 2, 0, -3, null, null, 3, 0};
    private static final LocalDate[] FECHA_LIMITE = {null, null, null, null, null, null, LIMITE, LIMITE,
            LIMITE};

    private static final List<RepeticionSeguimiento> DOS = List.of(
            new RepeticionSeguimiento("SEG-1-1", "SEG-1", 1, LocalDate.of(2026, 11, 20), "PENDIENTE_CITA"),
            new RepeticionSeguimiento("SEG-1-2", "SEG-1", 2, LocalDate.of(2027, 2, 20), "PENDIENTE_CITA"));
    private static final String RECHAZO =
            "Solo un médico puede solicitar una prueba de seguimiento periódica.";

    private final VistaFalsa vista = new VistaFalsa();
    private final ClienteFalso cliente = new ClienteFalso();
    private final ControladorSolicitarSeguimiento controlador =
            new ControladorSolicitarSeguimientoImpl(vista, cliente);

    @Nested
    @DisplayName("CA y CC · Each use")
    class EachUse {

        @ParameterizedTest(name = "{0}")
        @DisplayName("CA: solicitar")
        @CsvSource(delimiter = '|', value = {
                "CA-EU-02 | A1 M1 T1 I1 J1 P1 N1 U1 F1 E7 C1 | REP",
                "CA-EU-06 | A1 M1 T1 I1 J1 P1 N1 U1 F1 E3 C5 | IAE",
                "CA-EU-07 | A1 M1 T1 I1 J1 P1 N2 U1 F1 E2 C1 | REP",
                "CA-EU-08 | A1 M1 T1 I1 J1 P1 N3 U1 F1 E1 C1 | REP",
                "CA-EU-09 | A1 M1 T1 I1 J1 P1 N1 U1 F1 E8 C2 | RECHAZO",
                "CA-EU-10 | A2 M1 T1 I1 J1 P1 N1 U1 F1 E3 C1 | ERR(dniPaciente)",
                "CA-EU-11 | A3 M1 T1 I1 J1 P1 N1 U1 F1 E3 C1 | ERR(dniPaciente)",
                "CA-EU-13 | A1 M2 T1 I1 J1 P1 N1 U1 F1 E3 C1 | ERR(dniMedico)",
                "CA-EU-15 | A1 M4 T1 I1 J1 P1 N1 U1 F1 E3 C1 | ERR(dniMedico)",
                "CA-EU-17 | A1 M1 T3 I1 J1 P1 N1 U1 F1 E3 C1 | ERR(tipoPrueba)",
                "CA-EU-18 | A1 M1 T4 I1 J1 P1 N1 U1 F1 E3 C1 | ERR(tipoPrueba)",
                "CA-EU-19 | A1 M1 T1 I2 J1 P1 N1 U1 F1 E3 C1 | ERR(indicacion)",
                "CA-EU-21 | A1 M1 T1 I4 J1 P1 N1 U1 F1 E3 C1 | ERR(indicacion)",
                "CA-EU-23 | A1 M1 T1 I1 J3 P1 N1 U1 F1 E3 C1 | ERR(justificacion)",
                "CA-EU-24 | A1 M1 T1 I1 J4 P1 N1 U1 F1 E3 C1 | ERR(justificacion)",
                "CA-EU-29 | A1 M1 T1 I1 J1 P1 N1 U2 F1 E3 C1 | ERR(periodicidad)",
                "CA-EU-32 | A1 M1 T1 I1 J1 P1 N1 U1 F1 E5 C1 | ERR(fin)",
                "CA-EU-34 | A1 M1 T1 I1 J1 P1 N1 U1 F1 E9 C1 | ERR(fin)"
        })
        void solicitarEachUse(String id, String entradas, String esperado) {
            comprobar(id, entradas, esperado);
        }

        @ParameterizedTest(name = "{0}")
        @DisplayName("Posible fallo PX-3: respuesta nula del cliente")
        @CsvSource(delimiter = '|', value = {
                "CA-EU-05 | A1 M1 T1 I1 J1 P1 N1 U1 F1 E3 C4 | ERROR"
        })
        void posibleFalloPx3EachUse(String id, String entradas, String esperado) {
            comprobar(id, entradas, esperado);
        }
    }

    @Nested
    @DisplayName("CA y CC · Pairwise")
    class Pairwise {

        @ParameterizedTest(name = "{0}")
        @DisplayName("CA-PW-01 a CA-PW-55")
        @CsvFileSource(resources = "/cdu13/ca-pairwise.csv", delimiter = '|')
        void paresPairwise(String id, String entradas, String esperado) {
            comprobar(id, entradas, esperado);
        }

        @Test
        @DisplayName("CC-PW-04: ambos son obligatorios")
        void ambosNulosPairwise() {
            assertThrows(NullPointerException.class,
                    () -> new ControladorSolicitarSeguimientoImpl(null, null));
        }
    }

    private void comprobar(String id, String entradas, String esperado) {
        Map<Character, Integer> codigos = new HashMap<>();
        for (String codigo : entradas.trim().split(" ")) {
            codigos.put(codigo.charAt(0), Integer.parseInt(codigo.substring(1)) - 1);
        }
        vista.datos = datos(codigos);
        cliente.codigo = codigos.get('C') + 1;

        if (esperado.equals("IAE")) {
            assertThrows(IllegalArgumentException.class, controlador::solicitar, id);
        } else {
            assertDoesNotThrow(controlador::solicitar, id);
        }

        switch (esperado) {
            case "REP" -> {
                assertEquals("Solicitud registrada.", vista.mensaje, id);
                assertEquals(DOS, vista.repeticiones, id);
                assertNull(vista.error, id);
            }
            case "RECHAZO" -> assertEquals(RECHAZO, vista.error, id);
            case "SIN_CONEXION" -> assertEquals(
                    "No se ha podido conectar con el servidor. Inténtelo de nuevo más tarde.",
                    vista.error, id);
            case "ERROR" -> assertNotNull(vista.error, id + ": debe mostrarse un error");
            case "IAE" -> assertNull(vista.error, id);
            default -> assertTrue(mensajes(esperado, codigos).contains(vista.error),
                    () -> id + ": se esperaba " + esperado + " y se ha mostrado " + vista.error);
        }
        if (!esperado.equals("REP")) {
            assertNull(vista.repeticiones, id + ": no debe mostrar repeticiones");
        }
        assertEquals(esperado.startsWith("ERR(") ? List.of() : List.of(vista.datos),
                cliente.recibidos, id + ": peticiones al servidor");
    }

    private static DatosSolicitudSeguimiento datos(Map<Character, Integer> codigos) {
        return new DatosSolicitudSeguimiento(DNI_PACIENTE[codigos.get('A')], DNI_MEDICO[codigos.get('M')],
                TIPO[codigos.get('T')], INDICACION[codigos.get('I')], JUSTIFICACION[codigos.get('J')],
                PRIORIDAD[codigos.get('P')], CADA[codigos.get('N')], UNIDAD[codigos.get('U')],
                INICIO[codigos.get('F')], REPETICIONES[codigos.get('E')], FECHA_LIMITE[codigos.get('E')]);
    }

    /** Mensajes admitidos para, por ejemplo, "ERR(dniPaciente, fin)": el de cualquiera de los campos. */
    private static Set<String> mensajes(String esperado, Map<Character, Integer> codigos) {
        Set<String> mensajes = new HashSet<>();
        for (String campo : esperado.substring(4, esperado.length() - 1).split(", ")) {
            switch (campo) {
                case "dniPaciente" -> mensajes.add("Indique el DNI del paciente.");
                case "dniMedico" -> mensajes.add("Indique el DNI del médico.");
                case "tipoPrueba" -> mensajes.add("Seleccione el tipo de prueba.");
                case "indicacion" -> mensajes.add("Indique la indicación clínica.");
                case "justificacion" -> mensajes.add("Indique la justificación.");
                case "prioridad" -> mensajes.add("Seleccione la prioridad.");
                case "fechaInicio" -> mensajes.add("Indique la fecha de inicio.");
                case "periodicidad" -> {
                    Integer cada = CADA[codigos.get('N')];
                    if (cada == null || UNIDAD[codigos.get('U')] == null) {
                        mensajes.add("Indique cada cuánto se repite la prueba.");
                    }
                    if (cada != null && cada <= 0) {
                        mensajes.add("La periodicidad debe ser un número mayor que 0.");
                    }
                }
                case "fin" -> mensajes.add(REPETICIONES[codigos.get('E')] == null
                        ? "Indique el número de repeticiones o la fecha límite del seguimiento."
                        : "El número de repeticiones debe ser mayor que 0.");
                default -> throw new IllegalArgumentException(campo);
            }
        }
        return mensajes;
    }

    private static final class VistaFalsa implements VistaSolicitarSeguimiento {
        DatosSolicitudSeguimiento datos;
        String mensaje;
        List<RepeticionSeguimiento> repeticiones;
        String error;

        @Override
        public DatosSolicitudSeguimiento obtenerDatos() {
            return datos;
        }

        @Override
        public void mostrarRepeticiones(String texto, List<RepeticionSeguimiento> lista) {
            mensaje = texto;
            repeticiones = lista;
        }

        @Override
        public void mostrarError(String texto) {
            error = texto;
        }
    }

    private static final class ClienteFalso implements ClienteSeguimientos {
        final List<DatosSolicitudSeguimiento> recibidos = new ArrayList<>();
        int codigo;

        @Override
        public ResultadoSolicitudSeguimiento solicitar(DatosSolicitudSeguimiento datos) {
            recibidos.add(datos);
            return switch (codigo) {
                case 1 -> new ResultadoSolicitudSeguimiento(true, "Solicitud registrada.", "SEG-1", DOS);
                case 2 -> new ResultadoSolicitudSeguimiento(false, RECHAZO, null, null);
                case 3 -> throw new ErrorComunicacionException("Servidor no disponible",
                        new IOException("Conexión rechazada"));
                case 4 -> null;
                case 5 -> throw new IllegalArgumentException("fallo no previsto");
                default -> throw new IllegalStateException("C" + codigo);
            };
        }
    }
}
