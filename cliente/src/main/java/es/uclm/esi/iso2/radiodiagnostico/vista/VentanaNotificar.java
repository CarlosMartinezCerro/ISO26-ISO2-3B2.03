package es.uclm.esi.iso2.radiodiagnostico.vista;

import es.uclm.esi.iso2.radiodiagnostico.notificacion.DatosNotificacion;
import es.uclm.esi.iso2.radiodiagnostico.notificacion.NotificacionEnviada;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Function;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/** Vista de escritorio (JavaFX, D-008) del CdU5. No contiene lógica: todo lo decide el controlador. */
public class VentanaNotificar extends BorderPane implements VistaNotificar {

    public static final String TITULO = "Radiodiagnóstico - CdU5: Notificar";
    static final List<String> CANALES = List.of("SMS", "APLICACION");
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final TextField campoDni = new TextField();
    private final ComboBox<String> comboCanal = new ComboBox<>(FXCollections.observableArrayList(CANALES));
    private final TextField campoAsunto = new TextField();
    private final TextArea areaMensaje = new TextArea();
    private final Button botonEnviar = new Button("Enviar notificación");
    private final Button botonHistorial = new Button("Ver historial");
    private final Label etiquetaEstado = new Label();
    private final TableView<NotificacionEnviada> tablaHistorial = new TableView<>();

    public VentanaNotificar() {
        setPadding(new Insets(16));

        campoDni.setId("campoDni");
        comboCanal.setId("comboCanal");
        campoAsunto.setId("campoAsunto");
        areaMensaje.setId("areaMensaje");
        botonEnviar.setId("botonEnviar");
        botonHistorial.setId("botonHistorial");
        etiquetaEstado.setId("etiquetaEstado");
        tablaHistorial.setId("tablaHistorial");

        campoDni.setPromptText("12345678Z");
        comboCanal.setPromptText("Elija canal");
        comboCanal.setButtonCell(new CeldaCanal());
        comboCanal.setCellFactory(lista -> new CeldaCanal());
        campoAsunto.setPromptText("Máximo 100 caracteres");
        areaMensaje.setPromptText("Máximo 500 caracteres");
        areaMensaje.setPrefRowCount(4);
        areaMensaje.setWrapText(true);
        botonEnviar.setDefaultButton(true);

        GridPane formulario = new GridPane();
        formulario.setHgap(10);
        formulario.setVgap(10);
        formulario.addRow(0, new Label("DNI del paciente:"), campoDni, botonHistorial);
        formulario.addRow(1, new Label("Canal:"), comboCanal);
        formulario.addRow(2, new Label("Asunto:"), campoAsunto);
        formulario.addRow(3, new Label("Mensaje:"), areaMensaje);
        GridPane.setColumnSpan(campoAsunto, 2);
        GridPane.setColumnSpan(areaMensaje, 2);
        formulario.add(botonEnviar, 1, 4);

        tablaHistorial.getColumns().add(columna("Fecha", n -> n.fechaEnvio() == null ? ""
                : FORMATO_FECHA.format(n.fechaEnvio())));
        tablaHistorial.getColumns().add(columna("Canal", n -> descripcion(n.canal())));
        tablaHistorial.getColumns().add(columna("Destino", NotificacionEnviada::destino));
        tablaHistorial.getColumns().add(columna("Asunto", NotificacionEnviada::asunto));
        tablaHistorial.getColumns().add(columna("Mensaje", NotificacionEnviada::mensaje));
        tablaHistorial.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tablaHistorial.setPrefHeight(200);
        tablaHistorial.setPlaceholder(new Label("Sin notificaciones"));

        VBox historial = new VBox(6, new HBox(10, new Label("Historial del paciente"), etiquetaEstado),
                tablaHistorial);
        historial.setPadding(new Insets(16, 0, 0, 0));

        setTop(formulario);
        setCenter(historial);
    }

    /** El controlador se engancha aquí a los botones (la vista no lo conoce). */
    public void alPulsarEnviar(Runnable accion) {
        botonEnviar.setOnAction(evento -> accion.run());
    }

    public void alPulsarHistorial(Runnable accion) {
        botonHistorial.setOnAction(evento -> accion.run());
    }

    @Override
    public DatosNotificacion obtenerDatos() {
        String canal = comboCanal.getValue();
        if (canal == null) {
            return null;
        }
        return new DatosNotificacion(campoDni.getText().trim(), canal, campoAsunto.getText().trim(),
                areaMensaje.getText().trim());
    }

    @Override
    public String obtenerDniPaciente() {
        return campoDni.getText().trim();
    }

    @Override
    public void mostrarHistorial(List<NotificacionEnviada> notificaciones, String mensaje) {
        tablaHistorial.getItems().setAll(notificaciones);
        etiquetaEstado.setText(mensaje);
    }

    @Override
    public void mostrarExito(String mensaje) {
        mostrarDialogo(Alert.AlertType.INFORMATION, "Notificación enviada", mensaje);
    }

    @Override
    public void mostrarError(String mensaje) {
        mostrarDialogo(Alert.AlertType.ERROR, "No se pudo notificar", mensaje);
    }

    @Override
    public void limpiarMensaje() {
        campoAsunto.clear();
        areaMensaje.clear();
        campoAsunto.requestFocus();
    }

    /** Texto que ve el usuario para cada canal ("APLICACION" se muestra como "Aplicación"). */
    static String descripcion(String canal) {
        return "APLICACION".equals(canal) ? "Aplicación" : canal;
    }

    private void mostrarDialogo(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo, mensaje);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.showAndWait();
    }

    private static TableColumn<NotificacionEnviada, String> columna(String titulo,
                                                                   Function<NotificacionEnviada, String> valor) {
        TableColumn<NotificacionEnviada, String> columna = new TableColumn<>(titulo);
        columna.setCellValueFactory(fila -> new ReadOnlyStringWrapper(valor.apply(fila.getValue())));
        return columna;
    }

    /** Celda del desplegable de canales. */
    static final class CeldaCanal extends ListCell<String> {
        @Override
        public void updateItem(String canal, boolean vacia) {
            super.updateItem(canal, vacia);
            setText(vacia || canal == null ? null : descripcion(canal));
        }
    }
}
