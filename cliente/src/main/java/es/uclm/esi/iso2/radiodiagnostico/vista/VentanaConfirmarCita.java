package es.uclm.esi.iso2.radiodiagnostico.vista;

import es.uclm.esi.iso2.radiodiagnostico.modelo.cita.DatosConfirmacionCita;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

/** Vista de escritorio (JavaFX, D-008) del CdU2. No contiene lógica: todo lo decide el controlador. */
public class VentanaConfirmarCita extends GridPane implements VistaConfirmarCita {

    public static final String TITULO = "Radiodiagnóstico - CdU2: Confirmar cita";

    private final TextField campoCita = new TextField();
    private final TextField campoDni = new TextField();
    private final Button botonConfirmar = new Button("Confirmar cita");

    public VentanaConfirmarCita() {
        setPadding(new Insets(16));
        setHgap(10);
        setVgap(10);

        campoCita.setId("campoCita");
        campoDni.setId("campoDni");
        botonConfirmar.setId("botonConfirmar");

        campoCita.setPromptText("CITA-SOL-001");
        campoDni.setPromptText("12345678Z");

        addRow(0, new Label("Código de la cita:"), campoCita);
        addRow(1, new Label("DNI:"), campoDni);
        add(botonConfirmar, 1, 2);
        botonConfirmar.setDefaultButton(true);
    }

    /** El controlador se engancha aquí al botón (la vista no lo conoce). */
    public void alPulsarConfirmar(Runnable accion) {
        botonConfirmar.setOnAction(evento -> accion.run());
    }

    @Override
    public DatosConfirmacionCita obtenerDatos() {
        String idCita = campoCita.getText().trim();
        String dni = campoDni.getText().trim();
        if (idCita.isEmpty() || dni.isEmpty()) {
            return null;
        }
        return new DatosConfirmacionCita(idCita, dni);
    }

    @Override
    public void mostrarExito(String mensaje) {
        mostrarDialogo(Alert.AlertType.INFORMATION, "Cita confirmada", mensaje);
    }

    @Override
    public void mostrarError(String mensaje) {
        mostrarDialogo(Alert.AlertType.ERROR, "No se pudo confirmar", mensaje);
    }

    private void mostrarDialogo(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo, mensaje);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.showAndWait();
    }
}
