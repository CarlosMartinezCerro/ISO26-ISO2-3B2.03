package es.uclm.esi.iso2.radiodiagnostico.vista;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.uclm.esi.iso2.radiodiagnostico.paciente.DatosAltaPaciente;
import java.time.LocalDate;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Prueba la lectura y el vaciado del formulario. Los diálogos (mostrarExito/mostrarError)
 * no se prueban porque bloquean hasta que alguien los cierra.
 * Si no hay pantalla (por ejemplo en un CI sin entorno gráfico) los tests se omiten.
 */
class VentanaAltaPacienteTest {

    private VentanaAltaPaciente ventana;

    @BeforeAll
    static void arrancarJavaFx() {
        try {
            Platform.startup(() -> { });
        } catch (IllegalStateException yaArrancado) {
            // Otro test ya arrancó JavaFX.
        } catch (RuntimeException | UnsatisfiedLinkError sinPantalla) {
            Assumptions.abort("JavaFX no disponible: " + sinPantalla.getMessage());
        }
    }

    @BeforeEach
    void crearVentana() throws Exception {
        ventana = enHiloFx(VentanaAltaPaciente::new);
    }

    @Test
    void obtenerDatosLeeLosCamposSinEspacios() throws Exception {
        DatosAltaPaciente datos = enHiloFx(() -> {
            escribir("#campoDni", " 12345678Z ");
            escribir("#campoNombre", " Alex ");
            escribir("#campoApellidos", "Ejemplo");
            fecha().getEditor().setText("12/05/1990");
            escribir("#campoTelefono", "600000001");
            return ventana.obtenerDatos();
        });

        assertEquals(new DatosAltaPaciente("12345678Z", "Alex", "Ejemplo",
                LocalDate.of(1990, 5, 12), "600000001"), datos);
    }

    @Test
    void fechaVaciaSeEnviaComoNula() throws Exception {
        DatosAltaPaciente datos = enHiloFx(() -> {
            escribir("#campoDni", "12345678Z");
            return ventana.obtenerDatos();
        });

        assertNull(datos.fechaNacimiento());
    }

    @Test
    void fechaMalEscritaDevuelveNulo() throws Exception {
        DatosAltaPaciente datos = enHiloFx(() -> {
            fecha().getEditor().setText("12-05-1990");
            return ventana.obtenerDatos();
        });

        assertNull(datos);
    }

    @Test
    void limpiarVaciaTodosLosCampos() throws Exception {
        DatosAltaPaciente datos = enHiloFx(() -> {
            escribir("#campoDni", "12345678Z");
            escribir("#campoNombre", "Alex");
            escribir("#campoApellidos", "Ejemplo");
            fecha().setValue(LocalDate.of(1990, 5, 12));
            escribir("#campoTelefono", "600000001");
            ventana.limpiar();
            return ventana.obtenerDatos();
        });

        assertEquals(new DatosAltaPaciente("", "", "", null, ""), datos);
    }

    @Test
    void pulsarElBotonEjecutaLaAccionDelControlador() throws Exception {
        AtomicBoolean pulsado = new AtomicBoolean();

        enHiloFx(() -> {
            ventana.alPulsarAlta(() -> pulsado.set(true));
            ((Button) ventana.lookup("#botonAlta")).fire();
            return null;
        });

        assertTrue(pulsado.get());
    }

    private void escribir(String id, String texto) {
        ((TextField) ventana.lookup(id)).setText(texto);
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
