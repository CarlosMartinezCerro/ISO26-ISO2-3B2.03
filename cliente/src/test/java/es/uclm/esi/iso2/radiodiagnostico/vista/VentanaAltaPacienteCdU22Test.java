package es.uclm.esi.iso2.radiodiagnostico.vista;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import es.uclm.esi.iso2.radiodiagnostico.paciente.DatosAltaPaciente;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import javafx.application.Platform;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * CdU22, unidades VA, VL, CF y CT. Diseño: docs/testing/CdU22-diseno-pruebas.md.
 * Si no hay pantalla (por ejemplo en un CI sin entorno gráfico) los tests se omiten.
 */
@DisplayName("CdU22 · VentanaAltaPaciente")
class VentanaAltaPacienteCdU22Test {

    private static final List<String> CAMPOS_TEXTO =
            List.of("#campoDni", "#campoNombre", "#campoApellidos", "#campoTelefono");
    private static final List<String> TEXTOS = List.of("12345678Z", "Alex", "Ejemplo", "600000001");
    private static final String[] FECHAS = {"", "   ", "12/05/1990", " 12/05/1990 ", "12-05-1990",
            "01/01/2000", "00/01/2000", "30/01/2000", "31/01/2000", "32/01/2000", "15/11/2000",
            "15/12/2000", "15/13/2000", "15/00/2000", "27/02/2001", "28/02/2001", "29/02/2001",
            "29/02/2000", "31/02/2000", "1".repeat(256)};
    /** Fecha que debe leerse; null en W1 y W2 (vacías) y en las que deben rechazarse. */
    private static final LocalDate[] FECHAS_ESPERADAS = {null, null, LocalDate.of(1990, 5, 12),
            LocalDate.of(1990, 5, 12), null, LocalDate.of(2000, 1, 1), null,
            LocalDate.of(2000, 1, 30), LocalDate.of(2000, 1, 31), null, LocalDate.of(2000, 11, 15),
            LocalDate.of(2000, 12, 15), null, null, LocalDate.of(2001, 2, 27),
            LocalDate.of(2001, 2, 28), null, LocalDate.of(2000, 2, 29), null, null};

    private VentanaAltaPaciente ventana;

    @BeforeAll
    static void arrancarJavaFx() {
        JavaFxEnTests.arrancarOOmitir();
    }

    @BeforeEach
    void crearVentana() throws Exception {
        ventana = enHiloFx(VentanaAltaPaciente::new);
    }

    @Nested
    @DisplayName("VA, VL, CF y CT · Each use")
    class EachUse {

        @ParameterizedTest(name = "{0}")
        @DisplayName("VA: obtenerDatos")
        @CsvSource(delimiter = '|', value = {
                "VA-EU-01 = VA-DC-01 | W1 X1 X2 X2 X2 | DATOS",
                "VA-EU-02 | W2 X4 X4 X4 X4 | DATOS",
                "VA-EU-04 | W4 X2 X1 X3 X3 | DATOS",
                "VA-EU-06 = VA-PW-21 | W6 X1 X1 X1 X1 | DATOS",
                "VA-EU-07 = VA-PW-25 | W7 X1 X1 X1 X1 | NULO",
                "VA-EU-08 = VA-PW-29 | W8 X1 X1 X1 X1 | DATOS",
                "VA-EU-09 = VA-PW-33 | W9 X1 X1 X1 X1 | DATOS",
                "VA-EU-10 = VA-PW-37 | W10 X1 X1 X1 X1 | NULO",
                "VA-EU-11 = VA-PW-41 | W11 X1 X1 X1 X1 | DATOS",
                "VA-EU-12 = VA-PW-45 | W12 X1 X1 X1 X1 | DATOS",
                "VA-EU-13 = VA-PW-49 | W13 X1 X1 X1 X1 | NULO",
                "VA-EU-14 = VA-PW-53 | W14 X1 X1 X1 X1 | NULO",
                "VA-EU-15 = VA-PW-57 | W15 X1 X1 X1 X1 | DATOS",
                "VA-EU-16 = VA-PW-61 | W16 X1 X1 X1 X1 | DATOS",
                "VA-EU-18 = VA-PW-69 | W18 X1 X1 X1 X1 | DATOS",
                "VA-EU-20 = VA-PW-77 | W20 X1 X1 X1 X1 | NULO"})
        void obtenerDatosEachUse(String id, String entradas, String esperado) throws Exception {
            comprobar(id, entradas, esperado);
        }

        @ParameterizedTest(name = "{0}")
        @DisplayName("Posible fallo PF-1: fechas imposibles")
        @CsvSource(delimiter = '|', value = {
                "VA-EU-17 = VA-PW-65 | W17 X1 X1 X1 X1 | NULO",
                "VA-EU-19 = VA-PW-73 | W19 X1 X1 X1 X1 | NULO"})
        void posibleFalloPf1EachUse(String id, String entradas, String esperado) throws Exception {
            comprobar(id, entradas, esperado);
        }

        @Test
        @DisplayName("VL-EU-02: limpiar un formulario vacío")
        void limpiarVacioEachUse() throws Exception {
            comprobarLimpiar();
        }

        @Test
        @DisplayName("VL-EU-03: limpiar con la fecha mal escrita")
        void limpiarConFechaMalEscritaEachUse() throws Exception {
            rellenar("W5 X1 X1 X1 X1");

            comprobarLimpiar();
        }

        @Test
        @DisplayName("CF-EU-01 = CF-DC-01 = CF-MC-01: texto null")
        void textoNuloEachUse() throws Exception {
            assertNull(convertir(null));
        }

        @Test
        @DisplayName("CF-EU-02: texto vacío")
        void textoVacioEachUse() throws Exception {
            assertNull(convertir(""));
        }

        @Test
        @DisplayName("CF-EU-03 = CF-MC-02: texto en blanco")
        void textoEnBlancoEachUse() throws Exception {
            assertNull(convertir("   "));
        }

        @Test
        @DisplayName("CF-EU-04 = CF-DC-02 = CF-MC-03: texto válido")
        void textoValidoEachUse() throws Exception {
            assertEquals(LocalDate.of(1990, 5, 12), convertir("12/05/1990"));
        }

        @Test
        @DisplayName("CF-EU-05: texto válido con espacios")
        void textoValidoConEspaciosEachUse() throws Exception {
            assertEquals(LocalDate.of(1990, 5, 12), convertir(" 12/05/1990 "));
        }

        @Test
        @DisplayName("CF-EU-06: texto mal escrito")
        void textoMalEscritoEachUse() throws Exception {
            assertNull(convertir("12-05-1990"));
        }

        @Test
        @DisplayName("CF-EU-07 · Posible fallo PF-1: fecha imposible")
        void posibleFalloPf1EnElConversorEachUse() throws Exception {
            assertNull(convertir("31/02/2000"));
        }

        @Test
        @DisplayName("CT-EU-01 = CT-DC-01: fecha null")
        void fechaNulaEachUse() throws Exception {
            assertEquals("", enHiloFx(() -> fecha().getConverter().toString(null)));
        }

        @Test
        @DisplayName("CT-EU-02 = CT-DC-02: fecha presente")
        void fechaPresenteEachUse() throws Exception {
            assertEquals("12/05/1990",
                    enHiloFx(() -> fecha().getConverter().toString(LocalDate.of(1990, 5, 12))));
        }
    }

    @Nested
    @DisplayName("VA, VL, CF y CT · Pairwise")
    class Pairwise {

        @ParameterizedTest(name = "{0}")
        @DisplayName("VA-PW-01 a VA-PW-80")
        @CsvFileSource(resources = "/cdu22/va-pairwise.csv", delimiter = '|')
        void paresPairwise(String id, String entradas, String esperado) throws Exception {
            comprobar(id, entradas, esperado);
        }

        @ParameterizedTest(name = "{0}")
        @DisplayName("Posible fallo PF-1: fechas imposibles")
        @CsvSource(delimiter = '|', value = {
                "VA-PW-66 | W17 X2 X2 X2 X2 | NULO",
                "VA-PW-67 | W17 X3 X3 X3 X3 | NULO",
                "VA-PW-68 | W17 X4 X4 X4 X4 | NULO",
                "VA-PW-74 | W19 X2 X2 X2 X2 | NULO",
                "VA-PW-75 | W19 X3 X3 X3 X3 | NULO",
                "VA-PW-76 | W19 X4 X4 X4 X4 | NULO"})
        void posibleFalloPf1Pairwise(String id, String entradas, String esperado) throws Exception {
            comprobar(id, entradas, esperado);
        }
    }

    private void comprobar(String id, String entradas, String esperado) throws Exception {
        rellenar(entradas);
        List<String> antes = textos();

        DatosAltaPaciente datos = enHiloFx(ventana::obtenerDatos);

        if (esperado.equals("NULO")) {
            assertNull(datos, id);
        } else {
            String[] codigos = entradas.trim().split(" ");
            assertEquals(new DatosAltaPaciente(textoEsperado(codigos[1], 0),
                    textoEsperado(codigos[2], 1), textoEsperado(codigos[3], 2),
                    FECHAS_ESPERADAS[indice(codigos[0])], textoEsperado(codigos[4], 3)), datos, id);
        }
        assertEquals(antes, textos(), id + ": los campos no deben cambiar");
    }

    private void comprobarLimpiar() throws Exception {
        DatosAltaPaciente datos = enHiloFx(() -> {
            ventana.limpiar();
            return ventana.obtenerDatos();
        });

        assertEquals(new DatosAltaPaciente("", "", "", null, ""), datos);
        assertEquals(List.of("", "", "", "", ""), textos());
    }

    private LocalDate convertir(String texto) throws Exception {
        return enHiloFx(() -> fecha().getConverter().fromString(texto));
    }

    /** Entradas en el orden fecha, dni, nombre, apellidos y teléfono. */
    private void rellenar(String entradas) throws Exception {
        String[] codigos = entradas.trim().split(" ");
        enHiloFx(() -> {
            fecha().getEditor().setText(FECHAS[indice(codigos[0])]);
            for (int i = 0; i < CAMPOS_TEXTO.size(); i++) {
                campo(i).setText(switch (codigos[i + 1]) {
                    case "X1" -> TEXTOS.get(i);
                    case "X2" -> "";
                    case "X3" -> " " + TEXTOS.get(i) + " ";
                    case "X4" -> "x".repeat(256);
                    default -> throw new IllegalArgumentException(entradas);
                });
            }
            return null;
        });
    }

    private static String textoEsperado(String codigo, int campo) {
        return switch (codigo) {
            case "X1", "X3" -> TEXTOS.get(campo);
            case "X2" -> "";
            default -> "x".repeat(256);
        };
    }

    private List<String> textos() throws Exception {
        return enHiloFx(() -> List.of(fecha().getEditor().getText(), campo(0).getText(),
                campo(1).getText(), campo(2).getText(), campo(3).getText()));
    }

    private static int indice(String codigo) {
        return Integer.parseInt(codigo.substring(1)) - 1;
    }

    private TextField campo(int i) {
        return (TextField) ventana.lookup(CAMPOS_TEXTO.get(i));
    }

    private DatePicker fecha() {
        return (DatePicker) ventana.lookup("#campoFechaNacimiento");
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
