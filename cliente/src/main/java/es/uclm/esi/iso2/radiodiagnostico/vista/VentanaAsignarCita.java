package es.uclm.esi.iso2.radiodiagnostico.vista;

import es.uclm.esi.iso2.radiodiagnostico.modelo.cita.DatosAsignacionCita;
import es.uclm.esi.iso2.radiodiagnostico.modelo.cita.SolicitudPendiente;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.function.Function;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;

/** Vista de escritorio (JavaFX, D-008) del CdU25. No contiene lógica: todo lo decide el controlador. */
public class VentanaAsignarCita extends BorderPane implements VistaAsignarCita {

    /** Admite la hora con uno o dos dígitos: "9:30" y "09:30". */
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("H:mm");

    public static final String TITULO = "Radiodiagnóstico - CdU25: Asignar cita solicitada";

    private final TableView<SolicitudPendiente> tablaSolicitudes = new TableView<>();
    private final DatePicker campoFecha = new DatePicker();
    private final TextField campoHora = new TextField();
    private final TextField campoSala = new TextField();
    private final Button botonAsignar = new Button("Asignar cita");
    private final Button botonRecargar = new Button("Recargar");

    public VentanaAsignarCita() {
        setPadding(new Insets(16));

        tablaSolicitudes.setId("tablaSolicitudes");
        campoFecha.setId("campoFecha");
        campoHora.setId("campoHora");
        campoSala.setId("campoSala");
        botonAsignar.setId("botonAsignar");
        botonRecargar.setId("botonRecargar");

        tablaSolicitudes.getColumns().add(columna("Solicitud", SolicitudPendiente::id));
        tablaSolicitudes.getColumns().add(columna("Paciente", SolicitudPendiente::dniPaciente));
        tablaSolicitudes.getColumns().add(columna("Tipo de prueba", SolicitudPendiente::tipoPrueba));
        tablaSolicitudes.getColumns().add(columna("Prioridad", SolicitudPendiente::prioridad));
        tablaSolicitudes.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tablaSolicitudes.setPrefHeight(260);

        campoHora.setPromptText("09:30");
        campoHora.setPrefColumnCount(5);
        campoSala.setPromptText("SALA-RX-1");
        botonAsignar.setDefaultButton(true);

        HBox datos = new HBox(10, new Label("Fecha:"), campoFecha, new Label("Hora:"), campoHora,
                new Label("Sala:"), campoSala, botonAsignar, botonRecargar);
        datos.setPadding(new Insets(10, 0, 0, 0));
        datos.setAlignment(Pos.CENTER_LEFT);
        // Que ninguna etiqueta ni botón se recorte ("Fec...") aunque la ventana sea estrecha.
        datos.getChildren().forEach(nodo -> ((Region) nodo).setMinWidth(Region.USE_PREF_SIZE));

        setCenter(tablaSolicitudes);
        setBottom(datos);
    }

    /** El controlador se engancha aquí a los botones (la vista no lo conoce). */
    public void alPulsarAsignar(Runnable accion) {
        botonAsignar.setOnAction(evento -> accion.run());
    }

    public void alPulsarRecargar(Runnable accion) {
        botonRecargar.setOnAction(evento -> accion.run());
    }

    @Override
    public void mostrarSolicitudes(List<SolicitudPendiente> solicitudes) {
        tablaSolicitudes.getItems().setAll(solicitudes);
    }

    /** @return los datos introducidos, o null si falta la solicitud o la fecha, o la hora está mal escrita */
    @Override
    public DatosAsignacionCita obtenerDatos() {
        SolicitudPendiente solicitud = tablaSolicitudes.getSelectionModel().getSelectedItem();
        LocalDate fecha = fechaEscrita();
        if (solicitud == null || fecha == null) {
            return null;
        }
        try {
            LocalTime hora = LocalTime.parse(campoHora.getText().trim(), FORMATO_HORA);
            return new DatosAsignacionCita(solicitud.id(), fecha.atTime(hora), campoSala.getText().trim());
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /** Fecha del selector, también si se ha escrito a mano y no se ha pulsado Intro. */
    private LocalDate fechaEscrita() {
        String texto = campoFecha.getEditor().getText();
        if (texto != null && !texto.isBlank()) {
            try {
                return campoFecha.getConverter().fromString(texto.trim());
            } catch (RuntimeException e) {
                return null;
            }
        }
        return campoFecha.getValue();
    }

    @Override
    public void mostrarExito(String mensaje) {
        mostrarDialogo(Alert.AlertType.INFORMATION, "Cita asignada", mensaje);
    }

    @Override
    public void mostrarError(String mensaje) {
        mostrarDialogo(Alert.AlertType.ERROR, "No se pudo asignar la cita", mensaje);
    }

    private static TableColumn<SolicitudPendiente, String> columna(
            String titulo, Function<SolicitudPendiente, String> valor) {
        TableColumn<SolicitudPendiente, String> columna = new TableColumn<>(titulo);
        columna.setCellValueFactory(celda -> new ReadOnlyStringWrapper(valor.apply(celda.getValue())));
        return columna;
    }

    private void mostrarDialogo(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo, mensaje);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.showAndWait();
    }
}
