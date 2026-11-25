package es.uclm.esi.iso2.radiodiagnostico.vista;

import java.util.List;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

public class VentanaSolicitarPrueba extends GridPane implements VistaSolicitarPrueba {

    public static final String TITULO = "Radiodiagnóstico - CU12: Solicitar prueba";
    private final ComboBox<String> comboPrueba = new ComboBox<>();
    private final ComboBox<String> comboPrioridad = new ComboBox<>();
    private final Button botonSolicitar = new Button("Solicitar prueba");

    public VentanaSolicitarPrueba() {
        setPadding(new Insets(16));
        setHgap(10);
        setVgap(10);
        comboPrueba.setId("comboPrueba");
        comboPrioridad.setId("comboPrioridad");
        botonSolicitar.setId("botonSolicitar");
        comboPrioridad.getItems().addAll("URGENTE", "PREFERENTE", "ORDINARIO");
        addRow(0, new Label("Tipo de prueba (obligatorio):"), comboPrueba);
        addRow(1, new Label("Prioridad (obligatorio):"), comboPrioridad);
        add(botonSolicitar, 1, 2);
    }

    public void alPulsarSolicitar(Runnable accion) {
        botonSolicitar.setOnAction(event -> accion.run());
    }

    @Override
    public void mostrarPruebas(List<String> pruebas) {
        comboPrueba.getItems().setAll(pruebas);
        comboPrueba.getSelectionModel().clearSelection();
    }

    @Override public String obtenerTipoPrueba() { return comboPrueba.getValue(); }
    @Override public String obtenerPrioridad() { return comboPrioridad.getValue(); }
    @Override public void establecerEnviando(boolean enviando) { botonSolicitar.setDisable(enviando); }

    @Override
    public void mostrarExito(String mensaje) {
        mostrarDialogo(Alert.AlertType.INFORMATION, "Solicitud recibida", mensaje);
    }

    @Override
    public void mostrarError(String mensaje) {
        mostrarDialogo(Alert.AlertType.ERROR, "No se pudo solicitar la prueba", mensaje);
    }

    private void mostrarDialogo(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo, mensaje);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.showAndWait();
    }
}
