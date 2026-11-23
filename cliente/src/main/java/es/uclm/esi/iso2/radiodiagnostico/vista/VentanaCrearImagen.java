package es.uclm.esi.iso2.radiodiagnostico.vista;

import es.uclm.esi.iso2.radiodiagnostico.modelo.informe.DatosImagen;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;

/** Vista de escritorio (JavaFX, D-008) del CdU17. No contiene lógica: todo lo decide el controlador. */
public class VentanaCrearImagen extends GridPane implements VistaCrearImagen {

    public static final String TITULO = "Radiodiagnóstico - CdU17: Crear imagen";
    static final String SIN_FICHERO = "Ningún fichero seleccionado";

    private final TextField campoInforme = new TextField();
    private final TextField campoPaciente = new TextField();
    private final TextField campoMedico = new TextField();
    private final ComboBox<String> comboTipoPrueba = new ComboBox<>();
    private final Label etiquetaFichero = new Label(SIN_FICHERO);
    private final Button botonSeleccionar = new Button("Seleccionar imagen...");
    private final Button botonCrear = new Button("Crear imagen");
    private Path ficheroSeleccionado;

    public VentanaCrearImagen() {
        setPadding(new Insets(16));
        setHgap(10);
        setVgap(10);

        campoInforme.setId("campoInforme");
        campoPaciente.setId("campoPaciente");
        campoMedico.setId("campoMedico");
        comboTipoPrueba.setId("comboTipoPrueba");
        etiquetaFichero.setId("etiquetaFichero");
        botonSeleccionar.setId("botonSeleccionar");
        botonCrear.setId("botonCrear");

        campoInforme.setPromptText("INF-001");
        campoPaciente.setPromptText("12345678Z");
        campoMedico.setPromptText("MED-001");
        comboTipoPrueba.getItems().addAll(
                "Radiografía", "Ecografía", "Mamografía", "TAC", "Resonancia magnética");
        comboTipoPrueba.getSelectionModel().selectFirst();

        addRow(0, new Label("Informe:"), campoInforme);
        addRow(1, new Label("DNI paciente:"), campoPaciente);
        addRow(2, new Label("Médico:"), campoMedico);
        addRow(3, new Label("Tipo de prueba:"), comboTipoPrueba);
        addRow(4, botonSeleccionar, etiquetaFichero);
        add(botonCrear, 1, 5);
        botonCrear.setDefaultButton(true);

        botonSeleccionar.setOnAction(evento -> elegirFichero());
    }

    /** El controlador se engancha aquí al botón (la vista no lo conoce). */
    public void alPulsarCrear(Runnable accion) {
        botonCrear.setOnAction(evento -> accion.run());
    }

    private void elegirFichero() {
        FileChooser selector = new FileChooser();
        selector.setTitle("Seleccionar imagen médica");
        selector.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                "Imágenes médicas (*.dcm, *.png, *.jpg, *.jpeg)", "*.dcm", "*.png", "*.jpg", "*.jpeg"));
        File fichero = selector.showOpenDialog(getScene() == null ? null : getScene().getWindow());
        if (fichero != null) {
            seleccionarFichero(fichero.toPath());
        }
    }

    /** Separado del diálogo para poder probarlo sin abrir el selector de ficheros. */
    void seleccionarFichero(Path fichero) {
        ficheroSeleccionado = fichero;
        etiquetaFichero.setText(fichero.getFileName().toString());
    }

    @Override
    public DatosImagen obtenerDatos() {
        if (ficheroSeleccionado == null) {
            return null;
        }
        try {
            return new DatosImagen(
                    campoInforme.getText().trim(),
                    campoPaciente.getText().trim(),
                    campoMedico.getText().trim(),
                    comboTipoPrueba.getValue(),
                    ficheroSeleccionado.getFileName().toString(),
                    Files.readAllBytes(ficheroSeleccionado));
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public void mostrarExito(String mensaje) {
        mostrarDialogo(Alert.AlertType.INFORMATION, "Imagen creada", mensaje);
    }

    @Override
    public void mostrarError(String mensaje) {
        mostrarDialogo(Alert.AlertType.ERROR, "No se pudo crear la imagen", mensaje);
    }

    @Override
    public void limpiar() {
        campoInforme.clear();
        campoPaciente.clear();
        campoMedico.clear();
        comboTipoPrueba.getSelectionModel().selectFirst();
        ficheroSeleccionado = null;
        etiquetaFichero.setText(SIN_FICHERO);
        campoInforme.requestFocus();
    }

    private void mostrarDialogo(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo, mensaje);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.showAndWait();
    }
}
