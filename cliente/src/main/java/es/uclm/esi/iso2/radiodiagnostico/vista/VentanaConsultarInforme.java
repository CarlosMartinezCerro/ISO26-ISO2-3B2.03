package es.uclm.esi.iso2.radiodiagnostico.vista;

import es.uclm.esi.iso2.radiodiagnostico.informe.InformeRespuesta;
import es.uclm.esi.iso2.radiodiagnostico.informe.InformeResumen;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class VentanaConsultarInforme extends VBox implements VistaConsultarInforme {

    public static final String TITULO = "Radiodiagnóstico - Consultar informe";
    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final TextField campoDniPaciente = new TextField();
    private final Button botonListar = new Button("Consultar informes");
    private final Button botonVolver = new Button("Volver a la lista");
    private final Label etiquetaEstado = new Label();
    private final TableView<InformeResumen> tabla = new TableView<>();
    private final VBox detalle = new VBox(8);
    private final ScrollPane desplazamientoDetalle = new ScrollPane(detalle);
    private Runnable accionListar;
    private Runnable accionSeleccionar;

    public VentanaConsultarInforme() {
        setSpacing(10);
        setPadding(new Insets(16));

        campoDniPaciente.setId("campoDniPacienteInforme");
        campoDniPaciente.setPromptText("DNI del paciente");
        botonListar.setId("botonListarInformes");
        botonVolver.setId("botonVolverInformes");

        TableColumn<InformeResumen, String> columnaId =
                new TableColumn<>("ID");
        columnaId.setCellValueFactory(celda ->
                new SimpleStringProperty(Long.toString(celda.getValue().id())));

        TableColumn<InformeResumen, String> columnaEspecialidad =
                new TableColumn<>("Especialidad");
        columnaEspecialidad.setCellValueFactory(celda ->
                new SimpleStringProperty(celda.getValue().especialidad()));

        TableColumn<InformeResumen, String> columnaFecha =
                new TableColumn<>("Fecha");
        columnaFecha.setCellValueFactory(celda -> {
            var fecha = celda.getValue().fecha();
            return new SimpleStringProperty(
                    fecha == null ? "" : fecha.format(FORMATO_FECHA));
        });

        tabla.getColumns().addAll(columnaId, columnaEspecialidad, columnaFecha);
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabla.setPlaceholder(new Label("No hay informes para mostrar."));
        tabla.setRowFactory(ignorado -> {
            TableRow<InformeResumen> fila = new TableRow<>();
            fila.setOnMouseClicked(evento -> {
                if (evento.getClickCount() == 1
                        && !fila.isEmpty()
                        && accionSeleccionar != null) {
                    accionSeleccionar.run();
                }
            });
            return fila;
        });

        HBox busqueda = new HBox(8,
                new Label("DNI del paciente:"),
                campoDniPaciente,
                botonListar);
        HBox.setHgrow(campoDniPaciente, Priority.ALWAYS);

        botonListar.setOnAction(evento -> {
            if (accionListar != null) {
                accionListar.run();
            }
        });
        botonVolver.setOnAction(evento -> mostrarLista());

        detalle.setId("detalleInforme");
        desplazamientoDetalle.setId("desplazamientoDetalleInforme");
        desplazamientoDetalle.setFitToWidth(true);
        desplazamientoDetalle.setVisible(false);
        desplazamientoDetalle.setManaged(false);
        VBox.setVgrow(tabla, Priority.ALWAYS);
        VBox.setVgrow(desplazamientoDetalle, Priority.ALWAYS);
        getChildren().addAll(busqueda, etiquetaEstado, tabla, desplazamientoDetalle);
    }

    public void alPulsarListar(Runnable accion) {
        accionListar = accion;
    }

    public void alSeleccionarInforme(Runnable accion) {
        accionSeleccionar = accion;
    }

    @Override
    public String obtenerDniPaciente() {
        return campoDniPaciente.getText().trim();
    }

    @Override
    public InformeResumen obtenerInformeSeleccionado() {
        return tabla.getSelectionModel().getSelectedItem();
    }

    @Override
    public void mostrarInformes(List<InformeResumen> informes) {
        tabla.setItems(FXCollections.observableArrayList(informes));
        tabla.getSelectionModel().clearSelection();
        etiquetaEstado.setText("");
        mostrarLista();
    }

    @Override
    public void mostrarInforme(InformeRespuesta informe) {
        detalle.getChildren().setAll(
                new Label("Informe " + informe.id()),
                campoDetalle("DNI del paciente", informe.dniPaciente()),
                campoDetalle("DNI del médico", informe.dniMedico()),
                campoDetalle("Especialidad", informe.especialidad()),
                campoDetalle("Fecha", informe.fecha() == null
                        ? "" : informe.fecha().format(FORMATO_FECHA)),
                campoDetalle("Motivo de consulta", informe.motivoConsulta()),
                campoDetalle("Antecedentes", informe.antecedentes()),
                campoDetalle("Historial de la especialidad",
                        informe.historialEspecialidad()),
                campoDetalle("Consulta actual", informe.consultaActual()),
                campoDetalle("Diagnóstico", informe.diagnostico()),
                campoDetalle("Pruebas", informe.pruebas()),
                campoDetalle("Tratamiento", informe.tratamiento()),
                campoDetalle("Alta", informe.alta() ? "Sí" : "No"),
                campoDetalle("Anamnesis", informe.anamnesis()),
                botonVolver);
        tabla.setVisible(false);
        tabla.setManaged(false);
        desplazamientoDetalle.setVisible(true);
        desplazamientoDetalle.setManaged(true);
    }

    @Override
    public void mostrarMensaje(String mensaje) {
        etiquetaEstado.setText(mensaje);
    }

    @Override
    public void mostrarError(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle("Error al consultar informes");
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    private void mostrarLista() {
        desplazamientoDetalle.setVisible(false);
        desplazamientoDetalle.setManaged(false);
        tabla.setVisible(true);
        tabla.setManaged(true);
    }

    private static VBox campoDetalle(String etiqueta, String valor) {
        TextArea contenido = new TextArea(valor == null ? "" : valor);
        contenido.setEditable(false);
        contenido.setWrapText(true);
        contenido.setPrefRowCount(2);
        VBox campo = new VBox(3, new Label(etiqueta), contenido);
        return campo;
    }
}
