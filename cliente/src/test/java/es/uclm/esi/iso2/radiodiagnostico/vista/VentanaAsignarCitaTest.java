package es.uclm.esi.iso2.radiodiagnostico.vista;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import es.uclm.esi.iso2.radiodiagnostico.modelo.cita.DatosAsignacionCita;
import es.uclm.esi.iso2.radiodiagnostico.modelo.cita.SolicitudPendiente;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Prueba la tabla y la lectura del formulario del CdU25. Los diálogos (mostrarExito/mostrarError)
 * no se prueban porque bloquean hasta que alguien los cierra.
 * Si no hay pantalla (por ejemplo en un CI sin entorno gráfico) los tests se omiten.
 */
class VentanaAsignarCitaTest {

    private static final List<SolicitudPendiente> SOLICITUDES = List.of(
            new SolicitudPendiente("SOL-002", "FICTICIO-002", "TAC", "URGENTE"),
            new SolicitudPendiente("SOL-001", "FICTICIO-001", "Radiografía", "ORDINARIO"));

    private VentanaAsignarCita ventana;

    @BeforeAll
    static void arrancarJavaFx() {
        JavaFxEnTests.arrancarOOmitir();
    }

    @BeforeEach
    void crearVentana() throws Exception {
        ventana = enHiloFx(() -> {
            VentanaAsignarCita nueva = new VentanaAsignarCita();
            nueva.mostrarSolicitudes(SOLICITUDES);
            return nueva;
        });
    }

    @Test
    void muestraLasSolicitudesEnLaTabla() throws Exception {
        int filas = enHiloFx(() -> tabla().getItems().size());

        assertEquals(2, filas);
    }

    @Test
    void obtenerDatosLeeLaSolicitudSeleccionadaYElFormulario() throws Exception {
        DatosAsignacionCita datos = enHiloFx(() -> {
            tabla().getSelectionModel().select(1);
            rellenar(LocalDate.of(2026, 12, 1), " 09:30 ", " SALA-RX-1 ");
            return ventana.obtenerDatos();
        });

        assertEquals(new DatosAsignacionCita("SOL-001", LocalDateTime.of(2026, 12, 1, 9, 30), "SALA-RX-1"), datos);
    }

    @Test
    void admiteLaHoraConUnSoloDigito() throws Exception {
        DatosAsignacionCita datos = enHiloFx(() -> {
            tabla().getSelectionModel().select(1);
            rellenar(LocalDate.of(2026, 12, 1), "9:30", "SALA-RX-1");
            return ventana.obtenerDatos();
        });

        assertEquals(LocalDateTime.of(2026, 12, 1, 9, 30), datos.fechaHora());
    }

    @Test
    void leeLaFechaEscritaAManoAunqueNoSeHayaPulsadoIntro() throws Exception {
        DatosAsignacionCita datos = enHiloFx(() -> {
            tabla().getSelectionModel().select(1);
            rellenar(null, "10:00", "SALA-RX-1");
            DatePicker campoFecha = (DatePicker) ventana.lookup("#campoFecha");
            campoFecha.getEditor().setText(campoFecha.getConverter().toString(LocalDate.of(2026, 12, 3)));
            return ventana.obtenerDatos();
        });

        assertEquals(LocalDateTime.of(2026, 12, 3, 10, 0), datos.fechaHora());
    }

    @Test
    void conFechaEscritaMalDevuelveNulo() throws Exception {
        DatosAsignacionCita datos = enHiloFx(() -> {
            tabla().getSelectionModel().select(1);
            rellenar(null, "10:00", "SALA-RX-1");
            ((DatePicker) ventana.lookup("#campoFecha")).getEditor().setText("mañana");
            return ventana.obtenerDatos();
        });

        assertNull(datos);
    }

    @Test
    void sinSolicitudSeleccionadaDevuelveNulo() throws Exception {
        DatosAsignacionCita datos = enHiloFx(() -> {
            rellenar(LocalDate.of(2026, 12, 1), "09:30", "SALA-RX-1");
            return ventana.obtenerDatos();
        });

        assertNull(datos);
    }

    @Test
    void sinFechaDevuelveNulo() throws Exception {
        DatosAsignacionCita datos = enHiloFx(() -> {
            tabla().getSelectionModel().select(0);
            rellenar(null, "09:30", "SALA-RX-1");
            return ventana.obtenerDatos();
        });

        assertNull(datos);
    }

    @Test
    void conHoraMalEscritaDevuelveNulo() throws Exception {
        DatosAsignacionCita datos = enHiloFx(() -> {
            tabla().getSelectionModel().select(0);
            rellenar(LocalDate.of(2026, 12, 1), "9 y media", "SALA-RX-1");
            return ventana.obtenerDatos();
        });

        assertNull(datos);
    }

    @Test
    void losBotonesEjecutanLasAccionesDelControlador() throws Exception {
        AtomicInteger asignar = new AtomicInteger();
        AtomicInteger recargar = new AtomicInteger();

        enHiloFx(() -> {
            ventana.alPulsarAsignar(asignar::incrementAndGet);
            ventana.alPulsarRecargar(recargar::incrementAndGet);
            ((Button) ventana.lookup("#botonAsignar")).fire();
            ((Button) ventana.lookup("#botonRecargar")).fire();
            ((Button) ventana.lookup("#botonRecargar")).fire();
            return null;
        });

        assertEquals(1, asignar.get());
        assertEquals(2, recargar.get());
    }

    @SuppressWarnings("unchecked")
    private TableView<SolicitudPendiente> tabla() {
        return (TableView<SolicitudPendiente>) ventana.lookup("#tablaSolicitudes");
    }

    private void rellenar(LocalDate fecha, String hora, String sala) {
        ((DatePicker) ventana.lookup("#campoFecha")).setValue(fecha);
        ((TextField) ventana.lookup("#campoHora")).setText(hora);
        ((TextField) ventana.lookup("#campoSala")).setText(sala);
    }

    /** Ejecuta la acción en el hilo de JavaFX y espera su resultado. */
    private static <T> T enHiloFx(Supplier<T> accion) throws Exception {
        CompletableFuture<T> resultado = new CompletableFuture<>();
        Platform.runLater(() -> {
            try {
                resultado.complete(accion.get());
            } catch (RuntimeException | AssertionError e) {
                resultado.completeExceptionally(e);
            }
        });
        return resultado.get(10, TimeUnit.SECONDS);
    }
}
