package es.uclm.esi.iso2.radiodiagnostico.vista;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import es.uclm.esi.iso2.radiodiagnostico.prueba.DatosSolicitudSeguimiento;
import es.uclm.esi.iso2.radiodiagnostico.prueba.PrioridadSeguimiento;
import es.uclm.esi.iso2.radiodiagnostico.prueba.RepeticionSeguimiento;
import es.uclm.esi.iso2.radiodiagnostico.prueba.UnidadPeriodicidad;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import javafx.application.Platform;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextInputControl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * CdU13, unidades VA, VD, CF y CT. Diseño: docs/testing/CdU13-diseno-pruebas.md.
 * Los casos de decisiones y MC/DC repiten los de each use (apartados 8 y 9).
 * Si no hay pantalla (por ejemplo en un CI sin entorno gráfico) los tests se omiten.
 */
@DisplayName("CdU13 · VentanaSolicitarSeguimiento")
class VentanaSolicitarSeguimientoCdU13Test {

    private static final List<String> CAMPOS_FECHA = List.of("#campoFechaInicio", "#campoFechaLimite");
    private static final String MUY_LARGO = "x".repeat(256);

    private static final String[] FECHAS = {"", "   ", "20/11/2026", " 20/11/2026 ", "2026-11-20",
            "01/01/2027", "00/01/2027", "30/01/2027", "31/01/2027", "32/01/2027", "15/11/2027",
            "15/12/2027", "15/13/2027", "15/00/2027", "27/02/2027", "28/02/2027", "29/02/2027",
            "29/02/2028", "31/02/2027", "1".repeat(256)};
    /** Fecha que debe leerse; null en W1 y W2 (vacías) y en las que deben rechazarse. */
    private static final LocalDate[] FECHAS_ESPERADAS = {null, null, LocalDate.of(2026, 11, 20),
            LocalDate.of(2026, 11, 20), null, LocalDate.of(2027, 1, 1), null, LocalDate.of(2027, 1, 30),
            LocalDate.of(2027, 1, 31), null, LocalDate.of(2027, 11, 15), LocalDate.of(2027, 12, 15), null,
            null, LocalDate.of(2027, 2, 27), LocalDate.of(2027, 2, 28), null, LocalDate.of(2028, 2, 29),
            null, null};
    private static final String[] LIMITES = {"", "20/11/2027", " 20/11/2027 ", "20-11-2027", "31/02/2027",
            "29/02/2027", "29/02/2028"};
    private static final LocalDate[] LIMITES_ESPERADOS = {null, LocalDate.of(2027, 11, 20),
            LocalDate.of(2027, 11, 20), null, null, null, LocalDate.of(2028, 2, 29)};
    private static final String[] CADA = {"3", " 3 ", "", "tres", "2147483647", "2147483648", "-1", "3,5",
            "2147483646"};
    private static final Integer[] CADA_ESPERADO = {3, 3, null, null, 2147483647, null, -1, null,
            2147483646};
    private static final String[] REPETICIONES = {"4", " 4 ", "", "tres", "2147483647", "2147483648"};
    private static final Integer[] REPETICIONES_ESPERADAS = {4, 4, null, null, 2147483647, null};
    private static final String[] TIPO = {"TAC", null};
    private static final PrioridadSeguimiento[] PRIORIDAD = {PrioridadSeguimiento.URGENTE,
            PrioridadSeguimiento.PREFERENTE, PrioridadSeguimiento.ORDINARIO};
    private static final UnidadPeriodicidad[] UNIDAD = {UnidadPeriodicidad.SEMANAS,
            UnidadPeriodicidad.DIAS, UnidadPeriodicidad.MESES};

    private VentanaSolicitarSeguimiento ventana;

    @BeforeAll
    static void arrancarJavaFx() {
        JavaFxEnTests.arrancarOOmitir();
    }

    @BeforeEach
    void crearVentana() throws Exception {
        ventana = enHiloFx(VentanaSolicitarSeguimiento::new);
    }

    @Nested
    @DisplayName("VA, VD, CF y CT · Each use")
    class EachUse {

        @ParameterizedTest(name = "{0}")
        @DisplayName("VA: obtenerDatos")
        @CsvSource(delimiter = '|', value = {
                "VA-EU-01 | W1 D2 M2 T2 I2 J2 P3 Q3 U3 O1 R3 Z1 | DATOS",
                "VA-EU-02 | W2 D4 M4 T1 I4 J4 P2 Q2 U2 O1 R1 Z1 | DATOS",
                "VA-EU-04 | W4 D1 M3 T1 I1 J3 P1 Q1 U1 O2 R1 Z3 | DATOS",
                "VA-EU-06 | W6 D1 M1 T1 I1 J1 P1 Q5 U1 O1 R5 Z1 | DATOS",
                "VA-EU-07 | W7 D1 M1 T1 I1 J1 P1 Q1 U1 O1 R1 Z1 | NULO",
                "VA-EU-08 | W8 D1 M1 T1 I1 J1 P1 Q9 U1 O1 R1 Z1 | DATOS",
                "VA-EU-09 | W9 D1 M1 T1 I1 J1 P1 Q7 U1 O1 R1 Z1 | DATOS",
                "VA-EU-10 | W10 D1 M1 T1 I1 J1 P1 Q1 U1 O1 R1 Z1 | NULO",
                "VA-EU-11 | W11 D1 M1 T1 I1 J1 P1 Q1 U1 O2 R1 Z7 | DATOS",
                "VA-EU-12 | W12 D1 M1 T1 I1 J1 P1 Q1 U1 O2 R4 Z2 | DATOS",
                "VA-EU-13 | W13 D1 M1 T1 I1 J1 P1 Q1 U1 O1 R1 Z1 | NULO",
                "VA-EU-14 | W14 D1 M1 T1 I1 J1 P1 Q1 U1 O1 R1 Z1 | NULO",
                "VA-EU-15 | W15 D1 M1 T1 I1 J1 P1 Q1 U1 O1 R1 Z4 | DATOS",
                "VA-EU-16 | W16 D1 M1 T1 I1 J1 P1 Q1 U1 O1 R3 Z1 | DATOS",
                "VA-EU-18 | W18 D1 M1 T1 I1 J1 P1 Q1 U1 O2 R1 Z1 | DATOS",
                "VA-EU-20 | W20 D1 M1 T1 I1 J1 P1 Q1 U1 O1 R1 Z1 | NULO",
                "VA-EU-22 | W3 D1 M1 T1 I1 J1 P1 Q6 U1 O1 R1 Z1 | NULO",
                "VA-EU-23 | W3 D1 M1 T1 I1 J1 P1 Q8 U1 O1 R1 Z1 | NULO",
                "VA-EU-24 | W3 D1 M1 T1 I1 J1 P1 Q1 U1 O1 R4 Z1 | NULO",
                "VA-EU-25 | W3 D1 M1 T1 I1 J1 P1 Q1 U1 O1 R6 Z1 | NULO",
                "VA-EU-26 | W3 D1 M1 T1 I1 J1 P1 Q1 U1 O2 R1 Z4 | NULO",
                "VA-EU-29 | W3 D3 M1 T1 I3 J1 P1 Q1 U1 O2 R1 Z2 | DATOS"
        })
        void obtenerDatosEachUse(String id, String entradas, String esperado) throws Exception {
            comprobar(id, entradas, esperado);
        }

        @ParameterizedTest(name = "{0}")
        @DisplayName("Posible fallo PX-1: fechas imposibles")
        @CsvSource(delimiter = '|', value = {
                "VA-EU-17 | W17 D1 M1 T1 I1 J1 P1 Q1 U1 O1 R1 Z1 | NULO",
                "VA-EU-19 | W19 D1 M1 T1 I1 J1 P1 Q1 U1 O1 R1 Z1 | NULO",
                "VA-EU-27 | W3 D1 M1 T1 I1 J1 P1 Q1 U1 O2 R1 Z5 | NULO",
                "VA-EU-28 | W3 D1 M1 T1 I1 J1 P1 Q1 U1 O2 R1 Z6 | NULO"
        })
        void posibleFalloPx1EachUse(String id, String entradas, String esperado) throws Exception {
            comprobar(id, entradas, esperado);
        }

        @Test
        @DisplayName("VD-EU-02: repetición null")
        void describirNuloEachUse() {
            assertThrows(NullPointerException.class, () -> VentanaSolicitarSeguimiento.describir(null));
        }

        @Test
        @DisplayName("VD-EU-03: repetición sin fecha")
        void describirSinFechaEachUse() {
            RepeticionSeguimiento sinFecha =
                    new RepeticionSeguimiento("SEG-1-2", "SEG-1", 2, null, "PENDIENTE_CITA");

            assertThrows(NullPointerException.class,
                    () -> VentanaSolicitarSeguimiento.describir(sinFecha));
        }

        @Test
        @DisplayName("CF-EU-01 = CF-DC-01 = CF-MC-01: texto null")
        void textoNuloEachUse() throws Exception {
            comprobarConversion(null, null);
        }

        @Test
        @DisplayName("CF-EU-02: texto vacío")
        void textoVacioEachUse() throws Exception {
            comprobarConversion(null, "");
        }

        @Test
        @DisplayName("CF-EU-03 = CF-MC-02: texto en blanco")
        void textoEnBlancoEachUse() throws Exception {
            comprobarConversion(null, "   ");
        }

        @Test
        @DisplayName("CF-EU-04 = CF-DC-02 = CF-MC-03: texto válido")
        void textoValidoEachUse() throws Exception {
            comprobarConversion(LocalDate.of(2026, 11, 20), "20/11/2026");
        }

        @Test
        @DisplayName("CF-EU-05: texto válido con espacios")
        void textoValidoConEspaciosEachUse() throws Exception {
            comprobarConversion(LocalDate.of(2026, 11, 20), " 20/11/2026 ");
        }

        @Test
        @DisplayName("CF-EU-06: texto mal escrito")
        void textoMalEscritoEachUse() throws Exception {
            comprobarConversion(null, "2026-11-20");
        }

        @Test
        @DisplayName("CF-EU-07 · Posible fallo PX-1: fecha imposible")
        void posibleFalloPx1EnElConversorEachUse() throws Exception {
            comprobarConversion(null, "31/02/2027");
        }

        @Test
        @DisplayName("CT-EU-01 = CT-DC-01: fecha null")
        void fechaNulaEachUse() throws Exception {
            comprobarTexto("", null);
        }

        @Test
        @DisplayName("CT-EU-02 = CT-DC-02: fecha presente")
        void fechaPresenteEachUse() throws Exception {
            comprobarTexto("20/11/2026", LocalDate.of(2026, 11, 20));
        }
    }

    @Nested
    @DisplayName("VA, VD, CF y CT · Pairwise")
    class Pairwise {

        @ParameterizedTest(name = "{0}")
        @DisplayName("VA-PW-001 a VA-PW-182")
        @CsvFileSource(resources = "/cdu13/va-pairwise.csv", delimiter = '|')
        void paresPairwise(String id, String entradas, String esperado) throws Exception {
            comprobar(id, entradas, esperado);
        }

        @ParameterizedTest(name = "{0}")
        @DisplayName("Posible fallo PX-1: fechas imposibles")
        @CsvFileSource(resources = "/cdu13/va-pairwise-px1.csv", delimiter = '|')
        void posibleFalloPx1Pairwise(String id, String entradas, String esperado) throws Exception {
            comprobar(id, entradas, esperado);
        }
    }

    private void comprobar(String id, String entradas, String esperado) throws Exception {
        Map<Character, Integer> codigos = new HashMap<>();
        for (String codigo : entradas.trim().split(" ")) {
            codigos.put(codigo.charAt(0), Integer.parseInt(codigo.substring(1)) - 1);
        }
        rellenar(codigos);
        List<Object> antes = contenido();

        DatosSolicitudSeguimiento datos = enHiloFx(ventana::obtenerDatos);

        if (esperado.equals("NULO")) {
            assertNull(datos, id);
        } else {
            boolean porRepeticiones = codigos.get('O') == 0;
            assertEquals(new DatosSolicitudSeguimiento(
                    textoEsperado("12345678Z", codigos.get('D')),
                    textoEsperado("00000001R", codigos.get('M')), TIPO[codigos.get('T')],
                    textoEsperado("Indicación ficticia", codigos.get('I')),
                    textoEsperado("Justificación ficticia", codigos.get('J')),
                    PRIORIDAD[codigos.get('P')], CADA_ESPERADO[codigos.get('Q')],
                    UNIDAD[codigos.get('U')], FECHAS_ESPERADAS[codigos.get('W')],
                    porRepeticiones ? REPETICIONES_ESPERADAS[codigos.get('R')] : null,
                    porRepeticiones ? null : LIMITES_ESPERADOS[codigos.get('Z')]), datos, id);
        }
        assertEquals(antes, contenido(), id + ": los campos no deben cambiar");
    }

    private void comprobarConversion(LocalDate esperada, String texto) throws Exception {
        for (String campo : CAMPOS_FECHA) {
            assertEquals(esperada, enHiloFx(() -> fecha(campo).getConverter().fromString(texto)), campo);
        }
    }

    private void comprobarTexto(String esperado, LocalDate fecha) throws Exception {
        for (String campo : CAMPOS_FECHA) {
            assertEquals(esperado, enHiloFx(() -> fecha(campo).getConverter().toString(fecha)), campo);
        }
    }

    private void rellenar(Map<Character, Integer> codigos) throws Exception {
        enHiloFx(() -> {
            texto("#campoDniPaciente").setText(textoEscrito("12345678Z", codigos.get('D')));
            texto("#campoDniMedico").setText(textoEscrito("00000001R", codigos.get('M')));
            combo("#comboTipoPrueba").setValue(TIPO[codigos.get('T')]);
            texto("#campoIndicacion").setText(textoEscrito("Indicación ficticia", codigos.get('I')));
            texto("#campoJustificacion").setText(textoEscrito("Justificación ficticia", codigos.get('J')));
            combo("#comboPrioridad").setValue(PRIORIDAD[codigos.get('P')]);
            texto("#campoCada").setText(CADA[codigos.get('Q')]);
            combo("#comboUnidad").setValue(UNIDAD[codigos.get('U')]);
            fecha("#campoFechaInicio").getEditor().setText(FECHAS[codigos.get('W')]);
            ((RadioButton) ventana.lookup(codigos.get('O') == 0 ? "#opcionRepeticiones"
                    : "#opcionFechaLimite")).setSelected(true);
            texto("#campoRepeticiones").setText(REPETICIONES[codigos.get('R')]);
            fecha("#campoFechaLimite").getEditor().setText(LIMITES[codigos.get('Z')]);
            return null;
        });
    }

    private static String textoEscrito(String texto, int valor) {
        return switch (valor) {
            case 0 -> texto;
            case 1 -> "";
            case 2 -> " " + texto + " ";
            default -> MUY_LARGO;
        };
    }

    private static String textoEsperado(String texto, int valor) {
        return switch (valor) {
            case 0, 2 -> texto;
            case 1 -> "";
            default -> MUY_LARGO;
        };
    }

    private List<Object> contenido() throws Exception {
        return enHiloFx(() -> Arrays.asList(
                texto("#campoDniPaciente").getText(), texto("#campoDniMedico").getText(),
                combo("#comboTipoPrueba").getValue(), texto("#campoIndicacion").getText(),
                texto("#campoJustificacion").getText(), combo("#comboPrioridad").getValue(),
                texto("#campoCada").getText(), combo("#comboUnidad").getValue(),
                fecha("#campoFechaInicio").getEditor().getText(),
                ((RadioButton) ventana.lookup("#opcionRepeticiones")).isSelected(),
                texto("#campoRepeticiones").getText(),
                fecha("#campoFechaLimite").getEditor().getText()));
    }

    private TextInputControl texto(String id) {
        return (TextInputControl) ventana.lookup(id);
    }

    @SuppressWarnings("unchecked") // Cada desplegable recibe valores de su propio tipo.
    private ComboBox<Object> combo(String id) {
        return (ComboBox<Object>) ventana.lookup(id);
    }

    private DatePicker fecha(String id) {
        return (DatePicker) ventana.lookup(id);
    }

    /** Ejecuta la acción en el hilo de JavaFX y espera su resultado. */
    private static <T> T enHiloFx(Supplier<T> accion) throws Exception {
        CompletableFuture<T> resultado = new CompletableFuture<>();
        Platform.runLater(() -> {
            try {
                resultado.complete(accion.get());
            } catch (RuntimeException | AssertionError e) {
                resultado.completeExceptionally(e);
            }
        });
        return resultado.get(10, TimeUnit.SECONDS);
    }
}
