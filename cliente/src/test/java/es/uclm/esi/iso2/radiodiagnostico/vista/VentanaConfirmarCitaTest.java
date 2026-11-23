package es.uclm.esi.iso2.radiodiagnostico.vista;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.uclm.esi.iso2.radiodiagnostico.modelo.cita.DatosConfirmacionCita;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Prueba la lectura del formulario. Los diálogos no se prueban porque bloquean hasta que
 * alguien los cierra. Si no hay pantalla (por ejemplo en el CI) los tests se omiten.
 */
class VentanaConfirmarCitaTest {

    private VentanaConfirmarCita ventana;

    @BeforeAll
    static void arrancarJavaFx() {
        JavaFxEnTests.arrancarOOmitir();
    }

    @BeforeEach
    void crearVentana() throws Exception {
        ventana = enHiloFx(VentanaConfirmarCita::new);
    }

    @Test
    void obtenerDatosLeeLosCamposSinEspacios() throws Exception {
        DatosConfirmacionCita datos = enHiloFx(() -> {
            escribir("#campoCita", " CITA-SOL-001 ");
            escribir("#campoDni", " 12345678Z ");
            return ventana.obtenerDatos();
        });

        assertEquals(new DatosConfirmacionCita("CITA-SOL-001", "12345678Z"), datos);
    }

    @Test
    void sinDniDevuelveNulo() throws Exception {
        DatosConfirmacionCita datos = enHiloFx(() -> {
            escribir("#campoCita", "CITA-SOL-001");
            return ventana.obtenerDatos();
        });

        assertNull(datos);
    }

    @Test
    void pulsarElBotonEjecutaLaAccionDelControlador() throws Exception {
        AtomicBoolean pulsado = new AtomicBoolean();

        enHiloFx(() -> {
            ventana.alPulsarConfirmar(() -> pulsado.set(true));
            ((Button) ventana.lookup("#botonConfirmar")).fire();
            return null;
        });

        assertTrue(pulsado.get());
    }

    private void escribir(String id, String texto) {
        ((TextField) ventana.lookup(id)).setText(texto);
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
