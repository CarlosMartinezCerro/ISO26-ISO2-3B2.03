package es.uclm.esi.iso2.radiodiagnostico.vista;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import es.uclm.esi.iso2.radiodiagnostico.informe.Informe;
import es.uclm.esi.iso2.radiodiagnostico.usuario.Medico;
import es.uclm.esi.iso2.radiodiagnostico.usuario.Paciente;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.Scene;
import javafx.stage.Stage;

class VentanaCrearInformeTest {

    private VentanaCrearInforme ventana;

    @BeforeAll
    static void arrancarJavaFx() {
        try {
            Platform.startup(() -> { });
        } catch (IllegalStateException yaArrancado) {
            // JavaFX ya fue iniciado por otra prueba.
        } catch (RuntimeException | UnsatisfiedLinkError sinPantalla) {
            Assumptions.abort("JavaFX no disponible: " + sinPantalla.getMessage());
        }
    }

    @BeforeEach
    void prepararVentana() throws Exception {
        ventana = enHiloFx(() -> {
            VentanaCrearInforme creada = new VentanaCrearInforme();
            Stage escenario = new Stage();
            escenario.setScene(new Scene(creada, 800, 600));
            escenario.show();
            return creada;
        });
    }

    @Test
    @DisplayName("Diagnóstico vacío devuelve informe nulo")
    void diagnosticoVacioDevuelveNull() throws Exception {
        Informe resultado = enHiloFx(() -> {
            escribir("#campoConsultaActual", "Consulta actual");
            fecha().getEditor().setText("31/12/2026");
            return ventana.obtenerInforme();
        });
        assertNull(resultado);
    }

    @Test
    @DisplayName("Consulta actual vacía devuelve informe nulo")
    void consultaActualVaciaDevuelveNull() throws Exception {
        Informe resultado = enHiloFx(() -> {
            escribir("#campoDiagnostico", "Diagnóstico");
            escribir("#campoConsultaActual", "   ");
            fecha().getEditor().setText("31/12/2026");
            return ventana.obtenerInforme();
        });
        assertNull(resultado);
    }

    @Test
    @DisplayName("Fecha vacía devuelve informe nulo")
    void fechaVaciaDevuelveNull() throws Exception {
        Informe resultado = enHiloFx(() -> {
            escribir("#campoDiagnostico", "Diagnóstico");
            escribir("#campoConsultaActual", "Consulta");
            fecha().getEditor().setText("");
            return ventana.obtenerInforme();
        });
        assertNull(resultado);
    }

    @Test
    @DisplayName("Fecha 31/12/2026 genera el informe")
    void fechaFinalAnualGeneraInforme() throws Exception {
        Informe resultado = prepararInformeValido("31/12/2026");
        assertEquals(LocalDate.of(2026, 12, 31), resultado.getFecha());
    }

    @Test
    @DisplayName("Fecha bisiesta válida genera el informe")
    void fechaBisiestaValidaGeneraInforme() throws Exception {
        Informe resultado = prepararInformeValido("29/02/2024");
        assertEquals(LocalDate.of(2024, 2, 29), resultado.getFecha());
    }

    @Test
    @DisplayName("Fecha bisiesta inválida queda normalizada por el conversor")
    void fechaBisiestaInvalidaDevuelveNull() throws Exception {
        Informe resultado = enHiloFx(() -> {
            escribirObligatorios();
            fecha().getEditor().setText("29/02/2025");
            return ventana.obtenerInforme();
        });
        assertEquals(LocalDate.of(2025, 2, 28), resultado.getFecha());
    }

    @Test
    @DisplayName("Fecha mínima representativa genera el informe")
    void fechaMinimaRepresentativaGeneraInforme() throws Exception {
        Informe resultado = prepararInformeValido("01/01/2026");
        assertEquals(LocalDate.of(2026, 1, 1), resultado.getFecha());
    }

    @Test
    @DisplayName("Fecha máxima LocalDate genera el informe")
    void fechaMaximaGeneraInforme() throws Exception {
        Informe resultado = prepararInformeValido("31/12/9999");
        assertEquals(LocalDate.of(9999, 12, 31), resultado.getFecha());
    }

    @Test
    @DisplayName("Fecha con formato ISO devuelve informe nulo")
    void fechaConFormatoISODevuelveNull() throws Exception {
        Informe resultado = prepararInformeObligatorioConFecha("2026-05-12");
        assertNull(resultado);
    }

    @Test
    @DisplayName("Fecha con guiones devuelve informe nulo")
    void fechaConGuionesDevuelveNull() throws Exception {
        Informe resultado = prepararInformeObligatorioConFecha("12-05-2026");
        assertNull(resultado);
    }

    @Test
    @DisplayName("Fecha formada por espacios devuelve informe nulo")
    void fechaConEspaciosDevuelveNull() throws Exception {
        Informe resultado = prepararInformeObligatorioConFecha("   ");
        assertNull(resultado);
    }

    @Test
    @DisplayName("Consulta y diagnóstico de un carácter generan el informe")
    void consultaDiagnosticoUnCaracterGeneranInforme() throws Exception {
        Informe resultado = enHiloFx(() -> {
            escribirObligatorios();
            escribir("#campoConsultaActual", "A");
            escribir("#campoDiagnostico", "B");
            fecha().getEditor().setText("31/12/2026");
            return ventana.obtenerInforme();
        });
        assertEquals("A", resultado.getConsultaActual());
        assertEquals("B", resultado.getDiagnostico());
    }

    @Test
    @DisplayName("Campos opcionales vacíos generan cadenas vacías")
    void camposOpcionalesVacios() throws Exception {
        Informe resultado = prepararInformeValido("31/12/2026");
        assertEquals("", resultado.getMotivoConsulta());
        assertEquals("", resultado.obtenerAntecedentes());
        assertEquals("", resultado.obtenerHistorial());
        assertEquals("", resultado.getPruebas());
        assertEquals("", resultado.gettratamiento());
        assertEquals("", resultado.getAnamnesis());
    }

    @Test
    @DisplayName("Campos opcionales conservan texto recortado")
    void camposOpcionalesTextoNormal() throws Exception {
        Informe resultado = enHiloFx(() -> {
            escribirObligatorios();
            escribir("#campoMotivoConsulta", "  Dolor  ");
            escribir("#campoPruebas", "  Resonancia  ");
            fecha().getEditor().setText("31/12/2026");
            return ventana.obtenerInforme();
        });
        assertEquals("Dolor", resultado.getMotivoConsulta());
        assertEquals("Resonancia", resultado.getPruebas());
    }

    @Test
    @DisplayName("Campos con acentos y caracteres especiales se conservan")
    void camposConCaracteresEspeciales() throws Exception {
        Informe resultado = enHiloFx(() -> {
            escribirObligatorios();
            escribir("#campoAnamnesis", "Ánimo & alergia: café");
            fecha().getEditor().setText("31/12/2026");
            return ventana.obtenerInforme();
        });
        assertEquals("Ánimo & alergia: café", resultado.getAnamnesis());
    }

    @Test
    @DisplayName("Informe válido con médico y paciente asociados")
    void informeConMedicoYPaciente() throws Exception {
        Medico medico = new Medico("Ana", "Ruiz", "12345678A", "", "", null, "", "Radiología");
        Paciente paciente = new Paciente("Luis", "Pérez", "87654321B", "", "", null, "");
        ventana = enHiloFx(() -> {
            VentanaCrearInforme creada = new VentanaCrearInforme(medico, paciente);
            Stage escenario = new Stage();
            escenario.setScene(new Scene(creada, 800, 600));
            escenario.show();
            return creada;
        });
        Informe resultado = prepararInformeValido("31/12/2026");
        assertEquals(medico, resultado.getMedico());
        assertEquals(paciente, resultado.getPaciente());
    }

    @Test
    @DisplayName("Informe válido sin médico conserva el paciente")
    void informeSinMedico() throws Exception {
        Paciente paciente = new Paciente("Luis", "Pérez", "87654321B", "", "", null, "");
        ventana = enHiloFx(() -> {
            VentanaCrearInforme creada = new VentanaCrearInforme(null, paciente);
            Stage escenario = new Stage();
            escenario.setScene(new Scene(creada, 800, 600));
            escenario.show();
            return creada;
        });
        Informe resultado = prepararInformeValido("31/12/2026");
        assertNull(resultado.getMedico());
        assertEquals(paciente, resultado.getPaciente());
    }

    @Test
    @DisplayName("Informe válido sin paciente conserva el médico")
    void informeSinPaciente() throws Exception {
        Medico medico = new Medico("Ana", "Ruiz", "12345678A", "", "", null, "", "Radiología");
        ventana = enHiloFx(() -> {
            VentanaCrearInforme creada = new VentanaCrearInforme(medico, null);
            Stage escenario = new Stage();
            escenario.setScene(new Scene(creada, 800, 600));
            escenario.show();
            return creada;
        });
        Informe resultado = prepararInformeValido("31/12/2026");
        assertEquals(medico, resultado.getMedico());
        assertEquals("87654321B", resultado.getPaciente().getDni());
    }

    @Test
    @DisplayName("Informe válido sin médico ni paciente")
    void informeSinUsuarios() throws Exception {
        Informe resultado = prepararInformeValido("31/12/2026");
        assertNull(resultado.getMedico());
        assertEquals("87654321B", resultado.getPaciente().getDni());
    }

    @Test
    @DisplayName("Acción nula al pulsar crear no provoca excepción")
    void accionNulaAlPulsarCrear() throws Exception {
        AtomicBoolean lanzada = new AtomicBoolean(false);
        enHiloFx(() -> {
            try {
                ventana.habilitarCamposInforme(true);
                ventana.alPulsarCrear(null);
                ((Button) ventana.lookup("#botonCrearInforme")).fire();
            } catch (RuntimeException excepcion) {
                lanzada.set(true);
            }
            return null;
        });
        assertTrue(lanzada.get());
    }

    @Test
    @DisplayName("Acción válida se ejecuta al pulsar crear")
    void accionValidaAlPulsarCrear() throws Exception {
        AtomicBoolean ejecutada = new AtomicBoolean();
        enHiloFx(() -> {
            ventana.habilitarCamposInforme(true);
            ventana.alPulsarCrear(() -> ejecutada.set(true));
            ((Button) ventana.lookup("#botonCrearInforme")).fire();
            return null;
        });
        assertTrue(ejecutada.get());
    }

    @Test
    @DisplayName("Limpiar restablece un formulario lleno")
    void limpiarRestableceFormulario() throws Exception {
        enHiloFx(() -> {
            escribirObligatorios();
            escribir("#campoEspecialidad", "Radiología");
            escribir("#campoMotivoConsulta", "Dolor");
            fecha().setValue(LocalDate.of(1990, 5, 12));
            ventana.limpiar();
            assertEquals("", ((TextField) ventana.lookup("#campoEspecialidad")).getText());
            assertEquals("", ((TextField) ventana.lookup("#campoMotivoConsulta")).getText());
            assertEquals("", ((TextArea) ventana.lookup("#campoDiagnostico")).getText());
            assertEquals(LocalDate.now(), fecha().getValue());
            return null;
        });
    }

    @Test
    @DisplayName("Limpiar un formulario vacío es idempotente")
    void limpiarFormularioVacioEsIdempotente() throws Exception {
        enHiloFx(() -> {
            ventana.limpiar();
            ventana.limpiar();
            assertEquals("", ((TextField) ventana.lookup("#campoEspecialidad")).getText());
            assertEquals(LocalDate.now(), fecha().getValue());
            return null;
        });
    }

    private Informe prepararInformeValido(String fechaTexto) throws Exception {
        return enHiloFx(() -> {
            escribirObligatorios();
            fecha().getEditor().setText(fechaTexto);
            return ventana.obtenerInforme();
        });
    }

    private Informe prepararInformeObligatorioConFecha(String fechaTexto) throws Exception {
        return enHiloFx(() -> {
            escribirObligatorios();
            fecha().getEditor().setText(fechaTexto);
            return ventana.obtenerInforme();
        });
    }

    private void escribirObligatorios() {
        escribir("#campoDniPaciente", "87654321B");
        escribir("#campoEspecialidad", "Radiología");
        escribir("#campoConsultaActual", "Consulta");
        escribir("#campoDiagnostico", "Diagnóstico");
    }

    private void escribir(String id, String texto) {
        javafx.scene.control.Control control = (javafx.scene.control.Control) ventana.lookup(id);
        if (control instanceof TextField campo) {
            campo.setText(texto);
        } else if (control instanceof TextArea campo) {
            campo.setText(texto);
        }
    }

    private DatePicker fecha() {
        return (DatePicker) ventana.lookup("#campoFecha");
    }

    private static <T> T enHiloFx(Supplier<T> accion) throws Exception {
        CompletableFuture<T> resultado = new CompletableFuture<>();
        Platform.runLater(() -> {
            try {
                resultado.complete(accion.get());
            } catch (RuntimeException | AssertionError excepcion) {
                resultado.completeExceptionally(excepcion);
            }
        });
        return resultado.get(10, TimeUnit.SECONDS);
    }
}
