package es.uclm.esi.iso2.radiodiagnostico.vista;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.uclm.esi.iso2.radiodiagnostico.informe.InformeRespuesta;
import es.uclm.esi.iso2.radiodiagnostico.informe.InformeResumen;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class VentanaConsultarInformeTest {

    private VentanaConsultarInforme ventana;
    private Stage escenario;

    @BeforeAll
    static void arrancarJavaFx() {
        JavaFxEnTests.arrancarOOmitir();
    }

    @BeforeEach
    void preparar() throws Exception {
        enHiloFx(() -> {
            ventana = new VentanaConsultarInforme();
            escenario = new Stage();
            escenario.setScene(new Scene(ventana, 800, 600));
            escenario.show();
            return null;
        });
    }

    @AfterEach
    void cerrarEscenario() throws Exception {
        enHiloFx(() -> {
            if (escenario != null) {
                escenario.close();
            }
            return null;
        });
    }

    @Test
    void construyeControlesTablaYDetalleOculto() throws Exception {
        enHiloFx(() -> {
            assertTrue(ventana.lookup("#campoDniPacienteInforme") instanceof TextField);
            assertTrue(ventana.lookup("#botonListarInformes") instanceof Button);
            TableView<?> tabla = (TableView<?>) ventana.lookup(".table-view");
            assertEquals(List.of("ID", "Especialidad", "Fecha"),
                    tabla.getColumns().stream().map(TableColumn::getText).toList());
            assertEquals("No hay informes para mostrar.",
                    ((Label) tabla.getPlaceholder()).getText());
            assertFalse(ventana.lookup("#desplazamientoDetalleInforme").isVisible());
            return null;
        });
    }

    @Test
    void obtieneDniRecortadoYSeleccionActual() throws Exception {
        enHiloFx(() -> {
            ((TextField) ventana.lookup("#campoDniPacienteInforme"))
                    .setText(" 87654321B ");
            assertEquals("87654321B", ventana.obtenerDniPaciente());
            assertNull(ventana.obtenerInformeSeleccionado());

            InformeResumen resumen = new InformeResumen(
                    1, "Radiología", LocalDate.of(2026, 11, 19));
            ventana.mostrarInformes(List.of(resumen));
            TableView<InformeResumen> tabla = tabla();
            tabla.getSelectionModel().select(0);
            assertEquals(resumen, ventana.obtenerInformeSeleccionado());
            return null;
        });
    }

    @Test
    void muestraListaVaciaYDatosConFechaNulaOValida() throws Exception {
        enHiloFx(() -> {
            TableView<InformeResumen> tabla = tabla();
            tabla.getSelectionModel().select(0);
            ventana.mostrarMensaje("Estado anterior");
            ventana.mostrarInformes(List.of());
            assertTrue(tabla.getItems().isEmpty());
            assertNull(tabla.getSelectionModel().getSelectedItem());
            assertEquals("", estado().getText());
            assertTrue(tabla.isVisible());

            InformeResumen sinFecha = new InformeResumen(1, "Urgencias", null);
            InformeResumen conFecha = new InformeResumen(
                    2, "Radiología", LocalDate.of(2026, 11, 19));
            ventana.mostrarInformes(List.of(sinFecha, conFecha));
            assertEquals("", tabla.getColumns().get(2)
                    .getCellObservableValue(sinFecha).getValue());
            assertEquals("19/11/2026", tabla.getColumns().get(2)
                    .getCellObservableValue(conFecha).getValue());
            return null;
        });
    }

    @Test
    void presentaDetalleConValoresNulosYConDatos() throws Exception {
        enHiloFx(() -> {
            ventana.mostrarInforme(new InformeRespuesta(1, null, null, null,
                    null, null, null, null, null, null, null, false, null, null));
            VBox detalle = (VBox) ventana.lookup("#detalleInforme");
            assertFalse(tabla().isVisible());
            assertTrue(ventana.lookup("#desplazamientoDetalleInforme").isVisible());
            assertTrue(detalle.getChildren().stream()
                    .filter(TextArea.class::isInstance)
                    .map(TextArea.class::cast)
                    .allMatch(campo -> campo.getText().isEmpty()));
            assertEquals("No", textoCampo(detalle, "Alta"));

            ventana.mostrarInforme(new InformeRespuesta(2, "12345678A",
                    "87654321B", "Radiología", "Motivo", "Antecedentes",
                    "Historial", "Consulta", "Diagnóstico", "Pruebas",
                    "Tratamiento", true, LocalDate.of(2026, 11, 19),
                    "Anamnesis"));
            assertEquals("19/11/2026", textoCampo(detalle, "Fecha"));
            assertEquals("Sí", textoCampo(detalle, "Alta"));
            assertEquals("Diagnóstico", textoCampo(detalle, "Diagnóstico"));
            return null;
        });
    }

    @Test
    void muestraMensajeYBotonVolverRestauraLaLista() throws Exception {
        enHiloFx(() -> {
            ventana.mostrarMensaje("No hay informes");
            assertEquals("No hay informes", estado().getText());
            ventana.mostrarInforme(new InformeRespuesta(1, null, null, null,
                    null, null, null, null, null, null, null, false, null, null));
            assertTrue(ventana.lookup("#botonVolverInformes") instanceof Button);
            ((Button) ventana.lookup("#botonVolverInformes")).fire();
            assertTrue(tabla().isVisible());
            assertFalse(ventana.lookup("#desplazamientoDetalleInforme").isVisible());
            return null;
        });
    }

    @Test
    void accionesDeListarSeEjecutanSoloCuandoEstanRegistradas() throws Exception {
        AtomicInteger llamadas = new AtomicInteger();
        enHiloFx(() -> {
            ((Button) ventana.lookup("#botonListarInformes")).fire();
            ventana.alPulsarListar(llamadas::incrementAndGet);
            ((Button) ventana.lookup("#botonListarInformes")).fire();
            assertEquals(1, llamadas.get());
            return null;
        });
    }

    @Test
    @SuppressWarnings("unchecked")
    void seleccionDeFilaRequiereClicSimpleFilaValidaYAccion() throws Exception {
        AtomicInteger llamadas = new AtomicInteger();
        enHiloFx(() -> {
            ventana.mostrarInformes(List.of(
                    new InformeResumen(1, "Radiología", null)));
            TableView<InformeResumen> tabla = tabla();
            ventana.applyCss();
            ventana.layout();
            TableRow<InformeResumen> fila = tabla.lookupAll(".table-row-cell")
                    .stream()
                    .filter(TableRow.class::isInstance)
                    .map(nodo -> (TableRow<InformeResumen>) nodo)
                    .filter(celda -> !celda.isEmpty())
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("No se encontró la fila"));

            Event.fireEvent(fila, clic(1));
            assertEquals(0, llamadas.get());

            ventana.alSeleccionarInforme(llamadas::incrementAndGet);
            Event.fireEvent(fila, clic(2));
            assertEquals(0, llamadas.get());
            Event.fireEvent(fila, clic(1));
            assertEquals(1, llamadas.get());

            TableRow<InformeResumen> filaVacia = tabla.getRowFactory().call(tabla);
            Event.fireEvent(filaVacia, clic(1));
            assertEquals(1, llamadas.get());
            return null;
        });
    }

    @Test
    void mostrarErrorAbreAlertaYPuedeCerrarse() throws Exception {
        AtomicBoolean alertaMostrada = new AtomicBoolean();
        enHiloFx(() -> {
            Platform.runLater(() -> {
                for (Window ventanaAbierta : new ArrayList<>(Window.getWindows())) {
                    if (ventanaAbierta.isShowing()
                            && ventanaAbierta.getScene() != null
                            && ventanaAbierta.getScene().getRoot() instanceof DialogPane) {
                        alertaMostrada.set(true);
                        ((Stage) ventanaAbierta).close();
                    }
                }
            });
            ventana.mostrarError("Error de prueba");
            return null;
        });
        assertTrue(alertaMostrada.get());
    }

    private static MouseEvent clic(int numeroClics) {
        return new MouseEvent(MouseEvent.MOUSE_CLICKED,
                0, 0, 0, 0, MouseButton.PRIMARY, numeroClics,
                false, false, false, false,
                false, false, false, false,
                false, false, null);
    }

    private static String textoCampo(VBox detalle, String etiqueta) {
        for (javafx.scene.Node nodo : detalle.getChildren()) {
            if (nodo instanceof VBox campo
                    && campo.getChildren().get(0) instanceof Label nombre
                    && etiqueta.equals(nombre.getText())) {
                return ((TextArea) campo.getChildren().get(1)).getText();
            }
        }
        throw new AssertionError("No se encontró el campo " + etiqueta);
    }

    @SuppressWarnings("unchecked")
    private TableView<InformeResumen> tabla() {
        return (TableView<InformeResumen>) ventana.lookup(".table-view");
    }

    private Label estado() {
        return (Label) ((VBox) ventana).getChildren().get(1);
    }

    private static <T> T enHiloFx(Supplier<T> accion) throws Exception {
        CompletableFuture<T> resultado = new CompletableFuture<>();
        Platform.runLater(() -> {
            try {
                resultado.complete(accion.get());
            } catch (RuntimeException | AssertionError excepcion) {
                resultado.completeExceptionally(excepcion);
            }
        });
        return resultado.get(10, TimeUnit.SECONDS);
    }
}
