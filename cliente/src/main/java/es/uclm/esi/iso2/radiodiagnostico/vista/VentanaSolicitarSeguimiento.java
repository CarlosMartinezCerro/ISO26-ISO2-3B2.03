package es.uclm.esi.iso2.radiodiagnostico.vista;

import es.uclm.esi.iso2.radiodiagnostico.modelo.prueba.DatosSolicitudSeguimiento;
import es.uclm.esi.iso2.radiodiagnostico.modelo.prueba.PrioridadSeguimiento;
import es.uclm.esi.iso2.radiodiagnostico.modelo.prueba.RepeticionSeguimiento;
import es.uclm.esi.iso2.radiodiagnostico.modelo.prueba.UnidadPeriodicidad;
import java.time.LocalDate;
import java.time.chrono.IsoEra;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoField;
import java.util.List;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;

/** Vista de escritorio (JavaFX, D-008) del CdU13. No contiene lógica: todo lo decide el controlador. */
public class VentanaSolicitarSeguimiento extends GridPane implements VistaSolicitarSeguimiento {

    public static final String TITULO =
            "Radiodiagnóstico - CdU13: Solicitar prueba de seguimiento periódica";
    /** STRICT para rechazar fechas imposibles (31/02); con yyyy exige una era y se fija la actual. */
    private static final DateTimeFormatter FORMATO_FECHA = new DateTimeFormatterBuilder()
            .appendPattern("dd/MM/yyyy")
            .parseDefaulting(ChronoField.ERA, IsoEra.CE.getValue())
            .toFormatter()
            .withResolverStyle(ResolverStyle.STRICT);

    private final TextField campoDniPaciente = new TextField();
    private final TextField campoDniMedico = new TextField();
    private final ComboBox<String> comboTipoPrueba = new ComboBox<>();
    private final TextArea campoIndicacion = new TextArea();
    private final TextArea campoJustificacion = new TextArea();
    private final ComboBox<PrioridadSeguimiento> comboPrioridad = new ComboBox<>();
    private final TextField campoCada = new TextField();
    private final ComboBox<UnidadPeriodicidad> comboUnidad = new ComboBox<>();
    private final DatePicker campoFechaInicio = new DatePicker();
    private final ToggleGroup grupoFin = new ToggleGroup();
    private final RadioButton opcionRepeticiones = new RadioButton("Número de repeticiones:");
    private final RadioButton opcionFechaLimite = new RadioButton("Fecha límite:");
    private final TextField campoRepeticiones = new TextField();
    private final DatePicker campoFechaLimite = new DatePicker();
    private final Button botonSolicitar = new Button("Solicitar seguimiento");
    private final ListView<String> listaRepeticiones = new ListView<>();

    public VentanaSolicitarSeguimiento() {
        setPadding(new Insets(16));
        setHgap(10);
        setVgap(10);

        campoDniPaciente.setId("campoDniPaciente");
        campoDniMedico.setId("campoDniMedico");
        comboTipoPrueba.setId("comboTipoPrueba");
        campoIndicacion.setId("campoIndicacion");
        campoJustificacion.setId("campoJustificacion");
        comboPrioridad.setId("comboPrioridad");
        campoCada.setId("campoCada");
        comboUnidad.setId("comboUnidad");
        campoFechaInicio.setId("campoFechaInicio");
        opcionRepeticiones.setId("opcionRepeticiones");
        opcionFechaLimite.setId("opcionFechaLimite");
        campoRepeticiones.setId("campoRepeticiones");
        campoFechaLimite.setId("campoFechaLimite");
        botonSolicitar.setId("botonSolicitar");
        listaRepeticiones.setId("listaRepeticiones");

        campoDniPaciente.setPromptText("12345678Z");
        campoDniMedico.setPromptText("00000001R");
        // Mismos tipos de prueba que ofrece el CdU17.
        comboTipoPrueba.getItems().addAll(
                "Radiografía", "Ecografía", "Mamografía", "TAC", "Resonancia magnética");
        comboTipoPrueba.setPromptText("Seleccione un tipo");
        campoIndicacion.setPrefRowCount(2);
        campoIndicacion.setWrapText(true);
        campoJustificacion.setPrefRowCount(2);
        campoJustificacion.setWrapText(true);
        comboPrioridad.getItems().addAll(PrioridadSeguimiento.values());
        comboPrioridad.setValue(PrioridadSeguimiento.ORDINARIO);
        campoCada.setPromptText("N");
        campoCada.setPrefColumnCount(4);
        comboUnidad.getItems().addAll(UnidadPeriodicidad.values());
        comboUnidad.setValue(UnidadPeriodicidad.MESES);
        campoFechaInicio.setPromptText("dd/mm/aaaa");
        campoFechaInicio.setConverter(new ConversorFecha());
        campoFechaLimite.setPromptText("dd/mm/aaaa");
        campoFechaLimite.setConverter(new ConversorFecha());
        campoRepeticiones.setPrefColumnCount(4);

        opcionRepeticiones.setToggleGroup(grupoFin);
        opcionFechaLimite.setToggleGroup(grupoFin);
        opcionRepeticiones.setSelected(true);
        // Solo se puede escribir en el criterio de fin elegido.
        campoFechaLimite.disableProperty().bind(opcionRepeticiones.selectedProperty());
        campoRepeticiones.disableProperty().bind(opcionFechaLimite.selectedProperty());

        listaRepeticiones.setPrefHeight(150);
        listaRepeticiones.setPlaceholder(new Label("Aún no se han generado repeticiones."));

        addRow(0, new Label("DNI paciente:"), campoDniPaciente);
        addRow(1, new Label("DNI médico:"), campoDniMedico);
        addRow(2, new Label("Tipo de prueba:"), comboTipoPrueba);
        addRow(3, new Label("Indicación clínica:"), campoIndicacion);
        addRow(4, new Label("Justificación:"), campoJustificacion);
        addRow(5, new Label("Prioridad:"), comboPrioridad);
        addRow(6, new Label("Cada:"), new HBox(10, campoCada, comboUnidad));
        addRow(7, new Label("Fecha de inicio:"), campoFechaInicio);
        addRow(8, new Label("Fin del seguimiento:"), new HBox(10, opcionRepeticiones, campoRepeticiones));
        addRow(9, new Label(), new HBox(10, opcionFechaLimite, campoFechaLimite));
        add(botonSolicitar, 1, 10);
        addRow(11, new Label("Repeticiones:"), listaRepeticiones);
        botonSolicitar.setDefaultButton(true);
    }

    /** El controlador se engancha aquí al botón (la vista no lo conoce). */
    public void alPulsarSolicitar(Runnable accion) {
        botonSolicitar.setOnAction(evento -> accion.run());
    }

    @Override
    public DatosSolicitudSeguimiento obtenerDatos() {
        try {
            boolean porRepeticiones = opcionRepeticiones.isSelected();
            return new DatosSolicitudSeguimiento(
                    campoDniPaciente.getText().trim(),
                    campoDniMedico.getText().trim(),
                    comboTipoPrueba.getValue(),
                    campoIndicacion.getText().trim(),
                    campoJustificacion.getText().trim(),
                    comboPrioridad.getValue(),
                    leerEntero(campoCada),
                    comboUnidad.getValue(),
                    leerFecha(campoFechaInicio),
                    porRepeticiones ? leerEntero(campoRepeticiones) : null,
                    porRepeticiones ? null : leerFecha(campoFechaLimite));
        } catch (NumberFormatException | DateTimeParseException e) {
            return null;
        }
    }

    @Override
    public void mostrarRepeticiones(String mensaje, List<RepeticionSeguimiento> repeticiones) {
        listaRepeticiones.getItems().setAll(repeticiones.stream()
                .map(VentanaSolicitarSeguimiento::describir)
                .toList());
        mostrarDialogo(Alert.AlertType.INFORMATION, "Seguimiento solicitado", mensaje);
    }

    @Override
    public void mostrarError(String mensaje) {
        listaRepeticiones.getItems().clear();
        mostrarDialogo(Alert.AlertType.ERROR, "No se pudo solicitar el seguimiento", mensaje);
    }

    /** Texto de una repetición en la lista, por ejemplo "Nº 1 · 20/11/2026". */
    static String describir(RepeticionSeguimiento repeticion) {
        return "Nº " + repeticion.numero() + " · " + FORMATO_FECHA.format(repeticion.fechaPrevista());
    }

    List<String> repeticionesMostradas() {
        return List.copyOf(listaRepeticiones.getItems());
    }

    private static Integer leerEntero(TextField campo) {
        String texto = campo.getText().trim();
        return texto.isEmpty() ? null : Integer.valueOf(texto);
    }

    private static LocalDate leerFecha(DatePicker campo) {
        String texto = campo.getEditor().getText().trim();
        return texto.isEmpty() ? null : LocalDate.parse(texto, FORMATO_FECHA);
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
