package es.uclm.esi.iso2.radiodiagnostico.vista;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import es.uclm.esi.iso2.radiodiagnostico.informe.Informe;
import es.uclm.esi.iso2.radiodiagnostico.usuario.Medico;
import es.uclm.esi.iso2.radiodiagnostico.usuario.Paciente;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

public class VentanaCrearInforme extends VBox implements VistaCrearInforme {

    public static final String TITULO = "Radiodiagnóstico - Crear informe";
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final Medico medico;
    private final Paciente paciente;
    private final TextField campoDniMedico = new TextField();
    private final TextField campoDniPaciente = new TextField();
    private final TextField campoEspecialidad = new TextField();
    private final TextField campoMotivoConsulta = new TextField();
    private final TextArea campoAntecedentes = crearArea();
    private final TextArea campoHistorialEspecialidad = crearArea();
    private final TextArea campoConsultaActual = crearArea();
    private final TextArea campoDiagnostico = crearArea();
    private final TextArea campoPruebas = crearArea();
    private final TextArea campoTratamiento = crearArea();
    private final TextArea campoAnamnesis = crearArea();
    private final DatePicker campoFecha = new DatePicker(LocalDate.now());
    private final Button botonCargarAntecedentes = new Button("Cargar antecedentes");
    private final Button botonCrear = new Button("Crear informe");

    public VentanaCrearInforme() {
        this(null, null);
    }

    public VentanaCrearInforme(Medico medico, Paciente paciente) {
        this.medico = medico;
        this.paciente = paciente;

        setSpacing(10);
        setPadding(new Insets(16));

        GridPane formulario = new GridPane();
        formulario.setHgap(10);
        formulario.setVgap(8);

        campoDniMedico.setId("campoDniMedico");
        campoDniPaciente.setId("campoDniPaciente");
        campoEspecialidad.setId("campoEspecialidad");
        campoMotivoConsulta.setId("campoMotivoConsulta");
        campoAntecedentes.setId("campoAntecedentes");
        campoHistorialEspecialidad.setId("campoHistorialEspecialidad");
        campoConsultaActual.setId("campoConsultaActual");
        campoDiagnostico.setId("campoDiagnostico");
        campoPruebas.setId("campoPruebas");
        campoTratamiento.setId("campoTratamiento");
        campoAnamnesis.setId("campoAnamnesis");
        campoFecha.setId("campoFecha");
        botonCrear.setId("botonCrearInforme");
        botonCargarAntecedentes.setId("botonCargarAntecedentes");

        campoDniMedico.setPromptText("DNI del médico");
        campoDniPaciente.setPromptText("DNI del paciente");
        campoEspecialidad.setPromptText("Especialidad");
        campoMotivoConsulta.setPromptText("Motivo de consulta");
        campoFecha.setPromptText("dd/mm/aaaa");
        campoFecha.setConverter(new ConversorFecha());
        if (medico != null && medico.dni() != null) {
            campoDniMedico.setText(medico.dni());
        }
        if (paciente != null && paciente.dni() != null) {
            campoDniPaciente.setText(paciente.dni());
        }
        if (medico != null && medico.especialidad() != null) {
            campoEspecialidad.setText(medico.especialidad());
        }

        int fila = 0;
        agregarCampo(formulario, fila++, "DNI del médico:", campoDniMedico);
        agregarCampo(formulario, fila++, "DNI del paciente:", campoDniPaciente);
        agregarCampo(formulario, fila++, "Especialidad:", campoEspecialidad);
        formulario.add(botonCargarAntecedentes, 1, fila++);
        agregarCampo(formulario, fila++, "Motivo de consulta:", campoMotivoConsulta);
        agregarCampo(formulario, fila++, "Antecedentes:", campoAntecedentes);
        agregarCampo(formulario, fila++, "Historial de la especialidad:", campoHistorialEspecialidad);
        agregarCampo(formulario, fila++, "Consulta actual:", campoConsultaActual);
        agregarCampo(formulario, fila++, "Diagnóstico:", campoDiagnostico);
        agregarCampo(formulario, fila++, "Pruebas:", campoPruebas);
        agregarCampo(formulario, fila++, "Tratamiento:", campoTratamiento);
        agregarCampo(formulario, fila++, "Anamnesis:", campoAnamnesis);
        agregarCampo(formulario, fila++, "Fecha:", campoFecha);
        formulario.add(botonCrear, 1, fila + 1);
        botonCrear.setDefaultButton(true);
        habilitarCamposInforme(false);

        ScrollPane desplazamiento = new ScrollPane(formulario);
        desplazamiento.setFitToWidth(true);
        VBox.setVgrow(desplazamiento, javafx.scene.layout.Priority.ALWAYS);
        getChildren().add(desplazamiento);
    }

    public void alPulsarCrear(Runnable accion) {
        botonCrear.setOnAction(evento -> accion.run());
    }

    public void alPulsarCargarAntecedentes(Runnable accion) {
        botonCargarAntecedentes.setOnAction(evento -> accion.run());
    }

    @Override
    public String obtenerDniPaciente() {
        return campoDniPaciente.getText().trim();
    }

    @Override
    public String obtenerEspecialidad() {
        return campoEspecialidad.getText().trim();
    }

    @Override
    public void cargarAntecedentes(String antecedentes, String historialEspecialidad) {
        campoAntecedentes.setText(antecedentes == null ? "" : antecedentes);
        campoHistorialEspecialidad.setText(
                historialEspecialidad == null ? "" : historialEspecialidad);
    }

    @Override
    public void habilitarCamposInforme(boolean habilitado) {
        campoMotivoConsulta.setDisable(!habilitado);
        campoAntecedentes.setDisable(!habilitado);
        campoHistorialEspecialidad.setDisable(!habilitado);
        campoConsultaActual.setDisable(!habilitado);
        campoDiagnostico.setDisable(!habilitado);
        campoPruebas.setDisable(!habilitado);
        campoTratamiento.setDisable(!habilitado);
        campoAnamnesis.setDisable(!habilitado);
        campoFecha.setDisable(!habilitado);
        botonCrear.setDisable(!habilitado);
    }

    @Override
    public Informe obtenerInforme() {
        if (campoDniPaciente.getText().trim().isEmpty()
                || campoEspecialidad.getText().trim().isEmpty()
                || campoDiagnostico.getText().trim().isEmpty()
                || campoConsultaActual.getText().trim().isEmpty()) {
            return null;
        }

        LocalDate fecha = obtenerFecha();
        if (fecha == null) {
            return null;
        }

        String dniMedico = campoDniMedico.getText().trim();
        String dniPaciente = campoDniPaciente.getText().trim();
        Informe informe = new Informe(dniMedico, dniPaciente,
                campoEspecialidad.getText().trim(),
                campoMotivoConsulta.getText().trim(), campoConsultaActual.getText().trim(),
                campoDiagnostico.getText().trim(), campoPruebas.getText().trim(),
                campoTratamiento.getText().trim(), false, fecha,
                campoAnamnesis.getText().trim());
        return informe.conAntecedentes(
                campoAntecedentes.getText().trim(),
                campoHistorialEspecialidad.getText().trim());
    }

    @Override
    public void mostrarExito(String mensaje) {
        mostrarDialogo(Alert.AlertType.INFORMATION, "Informe creado", mensaje);
    }

    @Override
    public void mostrarError(String mensaje) {
        mostrarDialogo(Alert.AlertType.ERROR, "No se pudo crear el informe", mensaje);
    }

    @Override
    public void limpiar() {
        campoDniMedico.clear();
        campoDniPaciente.clear();
        campoEspecialidad.clear();
        campoMotivoConsulta.clear();
        campoAntecedentes.clear();
        campoHistorialEspecialidad.clear();
        campoConsultaActual.clear();
        campoDiagnostico.clear();
        campoPruebas.clear();
        campoTratamiento.clear();
        campoAnamnesis.clear();
        campoFecha.setValue(LocalDate.now());
        habilitarCamposInforme(false);
        campoDniPaciente.requestFocus();
    }

    private static TextArea crearArea() {
        TextArea area = new TextArea();
        area.setWrapText(true);
        area.setPrefRowCount(2);
        return area;
    }

    private static void agregarCampo(GridPane formulario, int fila, String etiqueta,
            javafx.scene.control.Control campo) {
        formulario.add(new Label(etiqueta), 0, fila);
        formulario.add(campo, 1, fila);
        GridPane.setFillWidth(campo, true);
        GridPane.setHgrow(campo, javafx.scene.layout.Priority.ALWAYS);
    }

    private LocalDate obtenerFecha() {
        String texto = campoFecha.getEditor().getText().trim();
        if (texto.isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(texto, FORMATO_FECHA);
        } catch (DateTimeParseException excepcion) {
            return null;
        }
    }

    private void mostrarDialogo(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo, mensaje);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.showAndWait();
    }

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
            } catch (DateTimeParseException excepcion) {
                return null;
            }
        }
    }
}
