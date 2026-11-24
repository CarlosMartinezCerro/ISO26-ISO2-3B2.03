package es.uclm.esi.iso2.radiodiagnostico.vista;

import es.uclm.esi.iso2.radiodiagnostico.paciente.DatosAltaPaciente;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;

/** Vista de escritorio (JavaFX, D-008) del CdU22. No contiene lógica: todo lo decide el controlador. */
public class VentanaAltaPaciente extends GridPane implements VistaAltaPaciente {

    public static final String TITULO = "Radiodiagnóstico - CdU22: Dar de alta paciente";
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final TextField campoDni = new TextField();
    private final TextField campoNombre = new TextField();
    private final TextField campoApellidos = new TextField();
    private final DatePicker campoFechaNacimiento = new DatePicker();
    private final TextField campoTelefono = new TextField();
    private final Button botonAlta = new Button("Dar de alta");

    public VentanaAltaPaciente() {
        setPadding(new Insets(16));
        setHgap(10);
        setVgap(10);

        campoDni.setId("campoDni");
        campoNombre.setId("campoNombre");
        campoApellidos.setId("campoApellidos");
        campoFechaNacimiento.setId("campoFechaNacimiento");
        campoTelefono.setId("campoTelefono");
        botonAlta.setId("botonAlta");

        campoDni.setPromptText("12345678Z");
        campoTelefono.setPromptText("Opcional, 9 dígitos");
        campoFechaNacimiento.setPromptText("dd/mm/aaaa");
        campoFechaNacimiento.setConverter(new ConversorFecha());

        addRow(0, new Label("DNI:"), campoDni);
        addRow(1, new Label("Nombre:"), campoNombre);
        addRow(2, new Label("Apellidos:"), campoApellidos);
        addRow(3, new Label("Fecha de nacimiento:"), campoFechaNacimiento);
        addRow(4, new Label("Teléfono:"), campoTelefono);
        add(botonAlta, 1, 5);
        botonAlta.setDefaultButton(true);
    }

    /** El controlador se engancha aquí al botón (la vista no lo conoce). */
    public void alPulsarAlta(Runnable accion) {
        botonAlta.setOnAction(evento -> accion.run());
    }

    @Override
    public DatosAltaPaciente obtenerDatos() {
        LocalDate fechaNacimiento;
        String textoFecha = campoFechaNacimiento.getEditor().getText().trim();
        if (textoFecha.isEmpty()) {
            fechaNacimiento = null;
        } else {
            try {
                fechaNacimiento = LocalDate.parse(textoFecha, FORMATO_FECHA);
            } catch (DateTimeParseException e) {
                return null;
            }
        }
        return new DatosAltaPaciente(campoDni.getText().trim(), campoNombre.getText().trim(),
                campoApellidos.getText().trim(), fechaNacimiento, campoTelefono.getText().trim());
    }

    @Override
    public void mostrarExito(String mensaje) {
        mostrarDialogo(Alert.AlertType.INFORMATION, "Paciente dado de alta", mensaje);
    }

    @Override
    public void mostrarError(String mensaje) {
        mostrarDialogo(Alert.AlertType.ERROR, "No se pudo dar de alta", mensaje);
    }

    @Override
    public void limpiar() {
        campoDni.clear();
        campoNombre.clear();
        campoApellidos.clear();
        campoFechaNacimiento.setValue(null);
        campoFechaNacimiento.getEditor().clear();
        campoTelefono.clear();
        campoDni.requestFocus();
    }

    private void mostrarDialogo(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo, mensaje);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.showAndWait();
    }

    /** Muestra y lee las fechas como dd/mm/aaaa; si el texto no es válido no cambia el valor. */
    private static final class ConversorFecha extends StringConverter<LocalDate> {
        @Override
        public String toString(LocalDate fecha) {
            return fecha == null ? "" : FORMATO_FECHA.format(fecha);
        }

        @Override
        public LocalDate fromString(String texto) {
            if (texto == null || texto.isBlank()) {
                return null;
            }
            try {
                return LocalDate.parse(texto.trim(), FORMATO_FECHA);
            } catch (DateTimeParseException e) {
                return null;
            }
        }
    }
}
