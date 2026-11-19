
package es.uclm.esi.iso2.radiodiagnostico.vista;

import es.uclm.esi.iso2.radiodiagnostico.cita.CitaPaciente;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.GridPane;

public class VentanaConsultarCitas extends GridPane
        implements VistaConsultarCitas {

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final Button botonConsultar =
            new Button("Consultar mis citas");

    private final TableView<CitaPaciente> tabla =
            new TableView<>();

    public VentanaConsultarCitas() {
        setPadding(new Insets(16));
        setHgap(10);
        setVgap(10);

        botonConsultar.setId("botonConsultarCitas");

        TableColumn<CitaPaciente, String> columnaId =
                new TableColumn<>("ID");

        columnaId.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().id()));

        TableColumn<CitaPaciente, String> columnaPrueba =
                new TableColumn<>("Tipo de prueba");

        columnaPrueba.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().tipoPrueba()));

        TableColumn<CitaPaciente, String> columnaFecha =
                new TableColumn<>("Fecha y hora");

        columnaFecha.setCellValueFactory(d ->
                new SimpleStringProperty(
                        d.getValue().fechaHora().format(FORMATO_FECHA)));

        TableColumn<CitaPaciente, String> columnaSala =
                new TableColumn<>("Sala");

        columnaSala.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().sala()));

        TableColumn<CitaPaciente, String> columnaEstado =
                new TableColumn<>("Estado");

        columnaEstado.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().estado()));

        tabla.getColumns().addAll(
                columnaId,
                columnaPrueba,
                columnaFecha,
                columnaSala,
                columnaEstado
        );

        tabla.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY);

        add(botonConsultar, 0, 0);
        add(tabla, 0, 1);

        botonConsultar.setDefaultButton(true);
    }

    public void alPulsarConsultar(Runnable accion) {
        botonConsultar.setOnAction(evento -> accion.run());
    }

    @Override
    public void mostrarCitas(List<CitaPaciente> citas) {
        tabla.setItems(
                FXCollections.observableArrayList(citas));
    }

    @Override
    public void mostrarMensaje(String mensaje) {
        mostrarDialogo(
                Alert.AlertType.INFORMATION,
                "Consulta de citas",
                mensaje
        );
    }

    @Override
    public void mostrarError(String mensaje) {
        mostrarDialogo(
                Alert.AlertType.ERROR,
                "Error en la consulta",
                mensaje
        );
    }

    private void mostrarDialogo(
            Alert.AlertType tipo,
            String titulo,
            String mensaje) {

        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}

