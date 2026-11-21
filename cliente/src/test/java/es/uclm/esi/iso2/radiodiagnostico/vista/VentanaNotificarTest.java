package es.uclm.esi.iso2.radiodiagnostico.vista;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import es.uclm.esi.iso2.radiodiagnostico.notificacion.DatosNotificacion;
import es.uclm.esi.iso2.radiodiagnostico.notificacion.NotificacionEnviada;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Prueba el formulario y el historial del CdU5. Los diálogos (mostrarExito/mostrarError)
 * no se prueban porque bloquean hasta que alguien los cierra.
 * Si no hay pantalla (por ejemplo en un CI sin entorno gráfico) los tests se omiten.
 */
class VentanaNotificarTest {

    private static final NotificacionEnviada NOTIFICACION = new NotificacionEnviada("NOT-001", "12345678Z",
            "GENERAL", "SMS", "612345678", "Recordatorio", "Traiga la tarjeta.", LocalDateTime.of(2026, 11, 21, 10, 0));

    private VentanaNotificar ventana;

    @BeforeAll
    static void arrancarJavaFx() {
        JavaFxEnTests.arrancarOOmitir();
    }

    @BeforeEach
    void crearVentana() throws Exception {
        ventana = enHiloFx(VentanaNotificar::new);
    }

    @Test
    void obtenerDatosLeeElFormularioSinEspacios() throws Exception {
        DatosNotificacion datos = enHiloFx(() -> {
            rellenar(" 12345678Z ", "APLICACION", " Aviso ", " Su prueba está lista. ");
            return ventana.obtenerDatos();
        });

        assertEquals(new DatosNotificacion("12345678Z", "APLICACION", "Aviso", "Su prueba está lista."), datos);
    }

    @Test
    void sinCanalDevuelveNulo() throws Exception {
        DatosNotificacion datos = enHiloFx(() -> {
            rellenar("12345678Z", null, "Aviso", "Hola");
            return ventana.obtenerDatos();
        });

        assertNull(datos);
    }

    @Test
    void obtenerDniPacienteQuitaEspacios() throws Exception {
        String dni = enHiloFx(() -> {
            rellenar(" 12345678Z ", null, "", "");
            return ventana.obtenerDniPaciente();
        });

        assertEquals("12345678Z", dni);
    }

    @Test
    void ofreceLosDosCanales() throws Exception {
        List<String> canales = enHiloFx(() -> List.copyOf(combo().getItems()));

        assertEquals(List.of("SMS", "APLICACION"), canales);
        assertEquals("Aplicación", VentanaNotificar.descripcion("APLICACION"));
        assertEquals("SMS", VentanaNotificar.descripcion("SMS"));
    }

    @Test
    void mostrarHistorialRellenaLaTablaYElEstado() throws Exception {
        String[] estado = new String[1];
        int filas = enHiloFx(() -> {
            ventana.mostrarHistorial(List.of(NOTIFICACION), "1 notificación(es) encontradas.");
            estado[0] = ((Label) ventana.lookup("#etiquetaEstado")).getText();
            return tabla().getItems().size();
        });

        assertEquals(1, filas);
        assertEquals("1 notificación(es) encontradas.", estado[0]);
    }

    @Test
    void lasColumnasMuestranLosDatosDeLaNotificacion() throws Exception {
        List<Object> celdas = enHiloFx(() -> {
            ventana.mostrarHistorial(List.of(NOTIFICACION, new NotificacionEnviada("NOT-002", "12345678Z",
                    "GENERAL", "APLICACION", "12345678Z", "Sin fecha", "x", null)), "");
            return List.of(
                    tabla().getColumns().get(0).getCellData(0),
                    tabla().getColumns().get(1).getCellData(0),
                    tabla().getColumns().get(2).getCellData(0),
                    tabla().getColumns().get(3).getCellData(0),
                    tabla().getColumns().get(4).getCellData(0),
                    tabla().getColumns().get(0).getCellData(1),
                    tabla().getColumns().get(1).getCellData(1));
        });

        assertEquals(List.of("21/11/2026 10:00", "SMS", "612345678", "Recordatorio", "Traiga la tarjeta.", "", "Aplicación"),
                celdas);
    }

    @Test
    void limpiarMensajeVaciaAsuntoYMensajePeroNoElDni() throws Exception {
        DatosNotificacion datos = enHiloFx(() -> {
            rellenar("12345678Z", "SMS", "Aviso", "Hola");
            ventana.limpiarMensaje();
            return ventana.obtenerDatos();
        });

        assertEquals(new DatosNotificacion("12345678Z", "SMS", "", ""), datos);
    }

    @Test
    void laCeldaDelDesplegableMuestraElNombreLegible() throws Exception {
        List<String> textos = enHiloFx(() -> {
            VentanaNotificar.CeldaCanal celda = new VentanaNotificar.CeldaCanal();
            celda.updateItem("APLICACION", false);
            String aplicacion = celda.getText();
            celda.updateItem(null, true);
            return java.util.Arrays.asList(aplicacion, celda.getText());
        });

        assertEquals(java.util.Arrays.asList("Aplicación", null), textos);
    }

    @Test
    void losBotonesEjecutanLasAccionesDelControlador() throws Exception {
        AtomicInteger envios = new AtomicInteger();
        AtomicInteger historiales = new AtomicInteger();

        enHiloFx(() -> {
            ventana.alPulsarEnviar(envios::incrementAndGet);
            ventana.alPulsarHistorial(historiales::incrementAndGet);
            ((Button) ventana.lookup("#botonEnviar")).fire();
            ((Button) ventana.lookup("#botonHistorial")).fire();
            ((Button) ventana.lookup("#botonHistorial")).fire();
            return null;
        });

        assertEquals(1, envios.get());
        assertEquals(2, historiales.get());
    }

    private void rellenar(String dni, String canal, String asunto, String mensaje) {
        ((TextField) ventana.lookup("#campoDni")).setText(dni);
        combo().setValue(canal);
        ((TextField) ventana.lookup("#campoAsunto")).setText(asunto);
        ((TextArea) ventana.lookup("#areaMensaje")).setText(mensaje);
    }

    @SuppressWarnings("unchecked")
    private ComboBox<String> combo() {
        return (ComboBox<String>) ventana.lookup("#comboCanal");
    }

    @SuppressWarnings("unchecked")
    private TableView<NotificacionEnviada> tabla() {
        return (TableView<NotificacionEnviada>) ventana.lookup("#tablaHistorial");
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
