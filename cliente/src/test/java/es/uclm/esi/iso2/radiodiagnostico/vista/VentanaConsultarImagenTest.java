package es.uclm.esi.iso2.radiodiagnostico.vista;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.uclm.esi.iso2.radiodiagnostico.modelo.informe.ImagenConsultada;
import es.uclm.esi.iso2.radiodiagnostico.modelo.informe.ResumenImagen;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Prueba la tabla, el detalle y la vista previa del CdU19. Los diálogos de error no se prueban
 * porque bloquean hasta que alguien los cierra. Sin pantalla los tests se omiten.
 */
class VentanaConsultarImagenTest {

    private static final LocalDateTime FECHA = LocalDateTime.of(2026, 11, 2, 9, 30);
    private static final List<ResumenImagen> IMAGENES = List.of(
            new ResumenImagen("IMG-002", "INF-002", "Radiografía", FECHA, "torax.png"),
            new ResumenImagen("IMG-001", "INF-001", "TAC", FECHA.minusMonths(2), "craneo.dcm"));

    private VentanaConsultarImagen ventana;

    @BeforeAll
    static void arrancarJavaFx() {
        JavaFxEnTests.arrancarOOmitir();
    }

    @BeforeEach
    void crearVentana() throws Exception {
        ventana = enHiloFx(VentanaConsultarImagen::new);
    }

    @Test
    void devuelveElDniSinEspacios() throws Exception {
        String dni = enHiloFx(() -> {
            ((TextField) ventana.lookup("#campoDni")).setText("  FICTICIO-001 ");
            return ventana.obtenerDniPaciente();
        });

        assertEquals("FICTICIO-001", dni);
    }

    @Test
    void muestraLasImagenesYElMensaje() throws Exception {
        String estado = enHiloFx(() -> {
            ventana.mostrarImagenes(IMAGENES, "2 imagen(es) encontradas.");
            assertEquals(2, tabla().getItems().size());
            return ((Label) ventana.lookup("#etiquetaEstado")).getText();
        });

        assertEquals("2 imagen(es) encontradas.", estado);
    }

    @Test
    void devuelveLaImagenSeleccionadaONulo() throws Exception {
        String[] ids = enHiloFx(() -> {
            ventana.mostrarImagenes(IMAGENES, "");
            String sinSeleccion = ventana.obtenerImagenSeleccionada();
            tabla().getSelectionModel().select(1);
            return new String[] {sinSeleccion, ventana.obtenerImagenSeleccionada()};
        });

        assertNull(ids[0]);
        assertEquals("IMG-001", ids[1]);
    }

    @Test
    void muestraLosDatosYLaVistaPreviaDeUnPng() throws Exception {
        String datos = enHiloFx(() -> {
            ventana.mostrarImagen(imagen("torax.png", png(50, 40)));
            assertNotNull(((ImageView) ventana.lookup("#vistaPrevia")).getImage());
            assertEquals("", ((Label) ventana.lookup("#etiquetaVistaPrevia")).getText());
            return ((TextArea) ventana.lookup("#areaDatos")).getText();
        });

        assertTrue(datos.contains("Imagen: IMG-002"));
        assertTrue(datos.contains("Paciente: FICTICIO-001"));
        assertTrue(datos.contains("Fecha: 02/11/2026 09:30"));
        assertTrue(datos.contains("RIS/PACS: pacs://radiodiagnostico/FICTICIO-001/IMG-002"));
    }

    @Test
    void unDicomNoTieneVistaPrevia() throws Exception {
        String aviso = enHiloFx(() -> {
            ventana.mostrarImagen(imagen("craneo.dcm", new byte[] {1, 2, 3}));
            assertNull(((ImageView) ventana.lookup("#vistaPrevia")).getImage());
            return ((Label) ventana.lookup("#etiquetaVistaPrevia")).getText();
        });

        assertEquals(VentanaConsultarImagen.SIN_VISTA_PREVIA, aviso);
    }

    @Test
    void sinContenidoNoHayVistaPrevia() throws Exception {
        boolean sinVistaPrevia = enHiloFx(() -> VentanaConsultarImagen.crearVistaPrevia(null) == null
                && VentanaConsultarImagen.crearVistaPrevia(new byte[0]) == null);

        assertTrue(sinVistaPrevia);
    }

    @Test
    void unaBusquedaNuevaBorraLaImagenAnterior() throws Exception {
        String datos = enHiloFx(() -> {
            ventana.mostrarImagen(imagen("torax.png", png(10, 10)));
            ventana.mostrarImagenes(List.of(), "Sin imágenes");
            assertNull(((ImageView) ventana.lookup("#vistaPrevia")).getImage());
            return ((TextArea) ventana.lookup("#areaDatos")).getText();
        });

        assertEquals("", datos);
    }

    @Test
    void losBotonesEjecutanLasAccionesDelControlador() throws Exception {
        AtomicInteger buscar = new AtomicInteger();
        AtomicInteger ver = new AtomicInteger();

        enHiloFx(() -> {
            ventana.alPulsarBuscar(buscar::incrementAndGet);
            ventana.alPulsarVer(ver::incrementAndGet);
            ((Button) ventana.lookup("#botonBuscar")).fire();
            ((Button) ventana.lookup("#botonVer")).fire();
            return null;
        });

        assertEquals(1, buscar.get());
        assertEquals(1, ver.get());
    }

    @SuppressWarnings("unchecked")
    private TableView<ResumenImagen> tabla() {
        return (TableView<ResumenImagen>) ventana.lookup("#tablaImagenes");
    }

    private static ImagenConsultada imagen(String fichero, byte[] contenido) {
        return new ImagenConsultada("IMG-002", "INF-002", "FICTICIO-001", "Radiografía", FECHA,
                fichero, contenido, "pacs://radiodiagnostico/FICTICIO-001/IMG-002");
    }

    private static byte[] png(int ancho, int alto) {
        try (ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            ImageIO.write(new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_RGB), "png", salida);
            return salida.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
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
