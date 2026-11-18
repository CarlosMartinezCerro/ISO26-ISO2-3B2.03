package es.uclm.esi.iso2.radiodiagnostico.vista;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.uclm.esi.iso2.radiodiagnostico.informe.DatosImagen;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Prueba la lectura y el vaciado del formulario. Los diálogos (mostrarExito/mostrarError)
 * y el selector de ficheros no se prueban porque bloquean hasta que alguien los cierra.
 * Si no hay pantalla (por ejemplo en un CI sin entorno gráfico) los tests se omiten.
 */
class VentanaCrearImagenTest {

    @TempDir
    Path carpeta;

    private VentanaCrearImagen ventana;

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
        ventana = enHiloFx(VentanaCrearImagen::new);
    }

    @Test
    void obtenerDatosLeeLosCamposYElFichero() throws Exception {
        Path fichero = Files.write(carpeta.resolve("torax.dcm"), new byte[] {1, 2, 3});

        DatosImagen datos = enHiloFx(() -> {
            escribir("#campoInforme", " INF-001 ");
            escribir("#campoPaciente", "12345678Z");
            escribir("#campoMedico", "MED-001");
            ventana.seleccionarFichero(fichero);
            return ventana.obtenerDatos();
        });

        assertEquals("INF-001", datos.idInforme());
        assertEquals("12345678Z", datos.dniPaciente());
        assertEquals("Radiografía", datos.tipoPrueba());
        assertEquals("torax.dcm", datos.nombreFichero());
        assertArrayEquals(new byte[] {1, 2, 3}, datos.contenido());
    }

    @Test
    void sinFicheroDevuelveNulo() throws Exception {
        DatosImagen datos = enHiloFx(() -> {
            escribir("#campoInforme", "INF-001");
            return ventana.obtenerDatos();
        });

        assertNull(datos);
    }

    @Test
    void limpiarVaciaElFormularioYElFichero() throws Exception {
        Path fichero = Files.write(carpeta.resolve("torax.dcm"), new byte[] {1});

        String etiqueta = enHiloFx(() -> {
            escribir("#campoInforme", "INF-001");
            ventana.seleccionarFichero(fichero);
            ventana.limpiar();
            assertNull(ventana.obtenerDatos());
            assertEquals("", ((TextField) ventana.lookup("#campoInforme")).getText());
            return ((Label) ventana.lookup("#etiquetaFichero")).getText();
        });

        assertEquals(VentanaCrearImagen.SIN_FICHERO, etiqueta);
    }

    @Test
    void pulsarElBotonEjecutaLaAccionDelControlador() throws Exception {
        AtomicBoolean pulsado = new AtomicBoolean();

        enHiloFx(() -> {
            ventana.alPulsarCrear(() -> pulsado.set(true));
            ((Button) ventana.lookup("#botonCrear")).fire();
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
