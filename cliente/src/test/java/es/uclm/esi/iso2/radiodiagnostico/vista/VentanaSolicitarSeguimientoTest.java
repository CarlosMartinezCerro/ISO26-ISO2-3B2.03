package es.uclm.esi.iso2.radiodiagnostico.vista;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.uclm.esi.iso2.radiodiagnostico.modelo.prueba.DatosSolicitudSeguimiento;
import es.uclm.esi.iso2.radiodiagnostico.modelo.prueba.PrioridadSeguimiento;
import es.uclm.esi.iso2.radiodiagnostico.modelo.prueba.RepeticionSeguimiento;
import es.uclm.esi.iso2.radiodiagnostico.modelo.prueba.UnidadPeriodicidad;
import java.time.LocalDate;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Prueba la lectura del formulario del CdU13. Los diálogos no se prueban porque bloquean hasta
 * que alguien los cierra. Si no hay pantalla los tests se omiten.
 */
class VentanaSolicitarSeguimientoTest {

    private VentanaSolicitarSeguimiento ventana;

    @BeforeAll
    static void arrancarJavaFx() {
        JavaFxEnTests.arrancarOOmitir();
    }

    @BeforeEach
    void crearVentana() throws Exception {
        ventana = enHiloFx(VentanaSolicitarSeguimiento::new);
    }

    @Test
    void conRepeticionesSoloEnviaElNumeroDeRepeticiones() throws Exception {
        DatosSolicitudSeguimiento datos = enHiloFx(() -> {
            rellenarComun();
            escribir("#campoRepeticiones", " 4 ");
            fecha("#campoFechaLimite").getEditor().setText("20/11/2027");
            return ventana.obtenerDatos();
        });

        assertEquals(new DatosSolicitudSeguimiento("12345678Z", "00000001R", "TAC",
                "Indicación ficticia", "Justificación ficticia", PrioridadSeguimiento.URGENTE, 3,
                UnidadPeriodicidad.SEMANAS, LocalDate.of(2026, 11, 20), 4, null), datos);
    }

    @Test
    void conFechaLimiteSoloEnviaLaFechaLimite() throws Exception {
        DatosSolicitudSeguimiento datos = enHiloFx(() -> {
            rellenarComun();
            escribir("#campoRepeticiones", "4");
            ((RadioButton) ventana.lookup("#opcionFechaLimite")).setSelected(true);
            fecha("#campoFechaLimite").getEditor().setText("20/11/2027");
            return ventana.obtenerDatos();
        });

        assertNull(datos.numeroRepeticiones());
        assertEquals(LocalDate.of(2027, 11, 20), datos.fechaLimite());
    }

    @Test
    void soloSePuedeEscribirEnElCriterioDeFinElegido() throws Exception {
        boolean[] desactivados = enHiloFx(() -> {
            boolean limiteAlPrincipio = fecha("#campoFechaLimite").isDisabled();
            ((RadioButton) ventana.lookup("#opcionFechaLimite")).setSelected(true);
            return new boolean[] {limiteAlPrincipio,
                ((TextField) ventana.lookup("#campoRepeticiones")).isDisabled()};
        });

        assertTrue(desactivados[0]);
        assertTrue(desactivados[1]);
    }

    @Test
    void camposNumericosVaciosSeEnvianComoNulos() throws Exception {
        DatosSolicitudSeguimiento datos = enHiloFx(ventana::obtenerDatos);

        assertNull(datos.cada());
        assertNull(datos.numeroRepeticiones());
        assertNull(datos.fechaInicio());
        assertNull(datos.tipoPrueba());
        assertEquals(PrioridadSeguimiento.ORDINARIO, datos.prioridad());
        assertEquals(UnidadPeriodicidad.MESES, datos.unidad());
    }

    @Test
    void numeroMalEscritoDevuelveNulo() throws Exception {
        DatosSolicitudSeguimiento datos = enHiloFx(() -> {
            escribir("#campoCada", "tres");
            return ventana.obtenerDatos();
        });

        assertNull(datos);
    }

    @Test
    void fechaMalEscritaDevuelveNulo() throws Exception {
        DatosSolicitudSeguimiento datos = enHiloFx(() -> {
            fecha("#campoFechaInicio").getEditor().setText("2026-11-20");
            return ventana.obtenerDatos();
        });

        assertNull(datos);
    }

    @Test
    void describeCadaRepeticionConNumeroYFecha() {
        assertEquals("Nº 2 · 04/12/2026", VentanaSolicitarSeguimiento.describir(
                new RepeticionSeguimiento("SEG-1-2", "SEG-1", 2, LocalDate.of(2026, 12, 4),
                        "PENDIENTE_CITA")));
    }

    @Test
    void pulsarElBotonEjecutaLaAccionDelControlador() throws Exception {
        AtomicBoolean pulsado = new AtomicBoolean();

        enHiloFx(() -> {
            ventana.alPulsarSolicitar(() -> pulsado.set(true));
            ((Button) ventana.lookup("#botonSolicitar")).fire();
            return null;
        });

        assertTrue(pulsado.get());
    }

    @SuppressWarnings("unchecked")
    private void rellenarComun() {
        escribir("#campoDniPaciente", " 12345678Z ");
        escribir("#campoDniMedico", "00000001R");
        ((ComboBox<String>) ventana.lookup("#comboTipoPrueba")).setValue("TAC");
        ((TextArea) ventana.lookup("#campoIndicacion")).setText(" Indicación ficticia ");
        ((TextArea) ventana.lookup("#campoJustificacion")).setText("Justificación ficticia");
        ((ComboBox<PrioridadSeguimiento>) ventana.lookup("#comboPrioridad"))
                .setValue(PrioridadSeguimiento.URGENTE);
        escribir("#campoCada", "3");
        ((ComboBox<UnidadPeriodicidad>) ventana.lookup("#comboUnidad"))
                .setValue(UnidadPeriodicidad.SEMANAS);
        fecha("#campoFechaInicio").getEditor().setText("20/11/2026");
    }

    private void escribir(String id, String texto) {
        ((TextField) ventana.lookup(id)).setText(texto);
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
