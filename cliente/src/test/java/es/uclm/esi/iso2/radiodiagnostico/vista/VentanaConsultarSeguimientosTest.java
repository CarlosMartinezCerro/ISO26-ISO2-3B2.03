package es.uclm.esi.iso2.radiodiagnostico.vista;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.uclm.esi.iso2.radiodiagnostico.prueba.PrioridadSeguimiento;
import es.uclm.esi.iso2.radiodiagnostico.prueba.RepeticionSeguimiento;
import es.uclm.esi.iso2.radiodiagnostico.prueba.SeguimientoPeriodico;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import javafx.application.Platform;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Prueba la lectura del DNI, el relleno de las tablas y el vaciado de la vista del CdU11.
 * Los diálogos no se prueban porque bloquean hasta que alguien los cierra.
 * Si no hay pantalla (por ejemplo en el CI) los tests se omiten.
 */
class VentanaConsultarSeguimientosTest {

    private static final RepeticionSeguimiento PRIMERA = new RepeticionSeguimiento("SEG-1-1", "SEG-1",
            1, LocalDate.of(2026, 11, 20), "CITADA");
    private static final RepeticionSeguimiento SEGUNDA = new RepeticionSeguimiento("SEG-1-2", "SEG-1",
            2, LocalDate.of(2026, 12, 4), "PENDIENTE_CITA");
    private static final SeguimientoPeriodico SEGUIMIENTO = new SeguimientoPeriodico("SEG-1",
            "12345678Z", "00000001R", "TAC", "Indicación ficticia", PrioridadSeguimiento.URGENTE,
            "Cada 2 semanas",
            LocalDate.of(2026, 11, 20), LocalDate.of(2026, 12, 4), 2, 1,
            LocalDate.of(2026, 12, 4), "EN_CURSO", List.of(PRIMERA, SEGUNDA));

    private VentanaConsultarSeguimientos ventana;

    @BeforeAll
    static void arrancarJavaFx() {
        JavaFxEnTests.arrancarOOmitir();
    }

    @BeforeEach
    void crearVentana() throws Exception {
        ventana = enHiloFx(VentanaConsultarSeguimientos::new);
    }

    @Test
    void obtenerDniQuitaLosEspacios() throws Exception {
        String dni = enHiloFx(() -> {
            ((TextField) ventana.lookup("#campoDniSeguimiento")).setText("  12345678Z ");
            return ventana.obtenerDniPaciente();
        });

        assertEquals("12345678Z", dni);
    }

    @Test
    void mostrarSeguimientosSeleccionaElPrimeroYMuestraSusRepeticiones() throws Exception {
        List<?> repeticiones = enHiloFx(() -> {
            ventana.mostrarSeguimientos(List.of(SEGUIMIENTO));
            return List.copyOf(tabla("#tablaRepeticiones").getItems());
        });
        int filas = enHiloFx(() -> tabla("#tablaSeguimientos").getItems().size());

        assertEquals(1, filas);
        assertEquals(List.of(PRIMERA, SEGUNDA), repeticiones);
    }

    @Test
    void limpiarVaciaElDniYLasTablas() throws Exception {
        boolean vacia = enHiloFx(() -> {
            ((TextField) ventana.lookup("#campoDniSeguimiento")).setText("12345678Z");
            ventana.mostrarSeguimientos(List.of(SEGUIMIENTO));
            ventana.limpiar();
            return ventana.obtenerDniPaciente().isEmpty()
                    && tabla("#tablaSeguimientos").getItems().isEmpty()
                    && tabla("#tablaRepeticiones").getItems().isEmpty();
        });

        assertTrue(vacia);
    }

    @Test
    void formateaFechasYEstados() {
        assertEquals("04/12/2026", VentanaConsultarSeguimientos.fecha(LocalDate.of(2026, 12, 4)));
        assertEquals(VentanaConsultarSeguimientos.SIN_FECHA, VentanaConsultarSeguimientos.fecha(null));
        assertEquals("En curso", VentanaConsultarSeguimientos.textoEstado("EN_CURSO"));
        assertEquals("Pendiente cita", VentanaConsultarSeguimientos.textoEstado("PENDIENTE_CITA"));
        assertEquals("Urgente", VentanaConsultarSeguimientos.textoEstado(PrioridadSeguimiento.URGENTE.name()));
    }

    private TableView<?> tabla(String id) {
        return (TableView<?>) ventana.lookup(id);
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
