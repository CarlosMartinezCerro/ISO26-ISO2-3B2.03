package es.uclm.esi.iso2.radiodiagnostico.vista;

import es.uclm.esi.iso2.radiodiagnostico.prueba.RepeticionSeguimiento;
import es.uclm.esi.iso2.radiodiagnostico.prueba.SeguimientoPeriodico;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

/**
 * Vista JavaFX del CdU11 · Consultar prueba de seguimiento periódica (D-008).
 * Arriba se listan los seguimientos del paciente y, al seleccionar uno, abajo se ven sus
 * repeticiones. No contiene lógica de negocio: la consulta la decide el controlador.
 */
public class VentanaConsultarSeguimientos extends GridPane implements VistaConsultarSeguimientos {

    public static final String TITULO = "Radiodiagnóstico - Consultar pruebas de seguimiento";
    static final String SIN_FECHA = "-";

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final TextField campoDni = new TextField();
    private final Button botonConsultar = new Button("Consultar seguimientos");
    private final Button botonLimpiar = new Button("Limpiar");
    private final TableView<SeguimientoPeriodico> tablaSeguimientos = new TableView<>();
    private final TableView<RepeticionSeguimiento> tablaRepeticiones = new TableView<>();

    public VentanaConsultarSeguimientos() {
        setPadding(new Insets(16));
        setHgap(10);
        setVgap(10);

        campoDni.setId("campoDniSeguimiento");
        campoDni.setPromptText("DNI del paciente");
        botonConsultar.setId("botonConsultarSeguimientos");
        botonConsultar.setDefaultButton(true);
        botonLimpiar.setId("botonLimpiarSeguimientos");
        botonLimpiar.setOnAction(evento -> limpiar());
        tablaSeguimientos.setId("tablaSeguimientos");
        tablaRepeticiones.setId("tablaRepeticiones");

        agregarColumna(tablaSeguimientos, "Tipo de prueba", SeguimientoPeriodico::tipoPrueba);
        agregarColumna(tablaSeguimientos, "Periodicidad", SeguimientoPeriodico::periodicidad);
        agregarColumna(tablaSeguimientos, "Prioridad", SeguimientoPeriodico::prioridad);
        agregarColumna(tablaSeguimientos, "Inicio", (SeguimientoPeriodico s) -> fecha(s.fechaInicio()));
        agregarColumna(tablaSeguimientos, "Fin", (SeguimientoPeriodico s) -> fecha(s.fechaFin()));
        agregarColumna(tablaSeguimientos, "Próxima", (SeguimientoPeriodico s) -> fecha(s.proximaFecha()));
        agregarColumna(tablaSeguimientos, "Citadas",
                (SeguimientoPeriodico s) -> s.repeticionesCitadas() + "/" + s.repeticionesTotales());
        agregarColumna(tablaSeguimientos, "Estado", (SeguimientoPeriodico s) -> textoEstado(s.estado()));
        agregarColumna(tablaSeguimientos, "Médico", SeguimientoPeriodico::dniMedico);
        tablaSeguimientos.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tablaSeguimientos.setPlaceholder(new Label("Sin pruebas de seguimiento"));
        tablaSeguimientos.setPrefHeight(220);

        agregarColumna(tablaRepeticiones, "Nº", (RepeticionSeguimiento r) -> String.valueOf(r.numero()));
        agregarColumna(tablaRepeticiones, "Fecha prevista",
                (RepeticionSeguimiento r) -> fecha(r.fechaPrevista()));
        agregarColumna(tablaRepeticiones, "Estado", (RepeticionSeguimiento r) -> textoEstado(r.estado()));
        tablaRepeticiones.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tablaRepeticiones.setPlaceholder(new Label("Seleccione un seguimiento para ver sus repeticiones"));
        tablaRepeticiones.setPrefHeight(160);

        tablaSeguimientos.getSelectionModel().selectedItemProperty().addListener(
                (observable, anterior, seleccionado) -> mostrarRepeticiones(seleccionado));

        add(new HBox(10, new Label("DNI paciente:"), campoDni, botonConsultar, botonLimpiar), 0, 0);
        add(tablaSeguimientos, 0, 1);
        add(new Label("Repeticiones del seguimiento seleccionado"), 0, 2);
        add(tablaRepeticiones, 0, 3);
        setPrefWidth(820);
    }

    public void alPulsarConsultar(Runnable accion) {
        botonConsultar.setOnAction(evento -> accion.run());
    }

    @Override
    public String obtenerDniPaciente() {
        return campoDni.getText() == null ? "" : campoDni.getText().trim();
    }

    @Override
    public void mostrarSeguimientos(List<SeguimientoPeriodico> seguimientos) {
        tablaSeguimientos.setItems(FXCollections.observableArrayList(seguimientos));
        if (seguimientos.isEmpty()) {
            tablaRepeticiones.setItems(FXCollections.observableArrayList());
        } else {
            tablaSeguimientos.getSelectionModel().selectFirst();
        }
    }

    @Override
    public void mostrarMensaje(String mensaje) {
        mostrarDialogo(Alert.AlertType.INFORMATION, "Pruebas de seguimiento", mensaje);
    }

    @Override
    public void mostrarError(String mensaje) {
        mostrarDialogo(Alert.AlertType.ERROR, "Error en la consulta", mensaje);
    }

    @Override
    public void limpiar() {
        campoDni.clear();
        tablaSeguimientos.setItems(FXCollections.observableArrayList());
        tablaRepeticiones.setItems(FXCollections.observableArrayList());
    }

    /** Pasa "EN_CURSO" o "PENDIENTE_CITA" a "En curso" o "Pendiente cita". */
    static String textoEstado(String estado) {
        if (estado == null || estado.isBlank()) {
            return SIN_FECHA;
        }
        String texto = estado.replace('_', ' ').toLowerCase(Locale.ROOT);
        return Character.toUpperCase(texto.charAt(0)) + texto.substring(1);
    }

    static String fecha(LocalDate fecha) {
        return fecha == null ? SIN_FECHA : fecha.format(FORMATO_FECHA);
    }

    private void mostrarRepeticiones(SeguimientoPeriodico seguimiento) {
        List<RepeticionSeguimiento> repeticiones =
                seguimiento == null ? List.of() : seguimiento.repeticiones();
        tablaRepeticiones.setItems(FXCollections.observableArrayList(repeticiones));
    }

    private static <T> void agregarColumna(TableView<T> tabla, String titulo,
                                           Function<T, String> valor) {
        TableColumn<T, String> columna = new TableColumn<>(titulo);
        columna.setCellValueFactory(d -> new SimpleStringProperty(valor.apply(d.getValue())));
        tabla.getColumns().add(columna);
    }

    private void mostrarDialogo(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}
