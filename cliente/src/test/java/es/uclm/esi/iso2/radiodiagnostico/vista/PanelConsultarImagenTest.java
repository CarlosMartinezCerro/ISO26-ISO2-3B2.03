package es.uclm.esi.iso2.radiodiagnostico.vista;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.uclm.esi.iso2.radiodiagnostico.informe.ImagenConsultada;
import es.uclm.esi.iso2.radiodiagnostico.informe.ResumenImagen;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tests de la vista del CdU19. No abren ninguna ventana: funcionan también sin pantalla. */
class PanelConsultarImagenTest {

    private static final LocalDateTime FECHA = LocalDateTime.of(2026, 11, 2, 9, 30);
    private static final List<ResumenImagen> IMAGENES = List.of(
            new ResumenImagen("IMG-002", "INF-002", "Radiografía", FECHA, "torax.png"),
            new ResumenImagen("IMG-001", "INF-001", "TAC", FECHA.minusMonths(2), "craneo.dcm"));

    private AvisosFalsos avisos;
    private PanelConsultarImagen panel;

    @BeforeEach
    void crearPanel() {
        avisos = new AvisosFalsos();
        panel = new PanelConsultarImagen(avisos);
    }

    @Test
    void devuelveElDniSinEspacios() {
        panel.campoDni.setText("  FICTICIO-001 ");

        assertEquals("FICTICIO-001", panel.obtenerDniPaciente());
    }

    @Test
    void muestraLasImagenesEnLaTablaYElMensaje() {
        panel.mostrarImagenes(IMAGENES, "2 imagen(es) encontradas.");

        assertEquals(2, panel.modeloTabla.getRowCount());
        assertEquals("IMG-002", panel.modeloTabla.getValueAt(0, 0));
        assertEquals("INF-002", panel.modeloTabla.getValueAt(0, 1));
        assertEquals("Radiografía", panel.modeloTabla.getValueAt(0, 2));
        assertEquals("02/11/2026 09:30", panel.modeloTabla.getValueAt(0, 3));
        assertEquals("torax.png", panel.modeloTabla.getValueAt(0, 4));
        assertEquals("2 imagen(es) encontradas.", panel.etiquetaEstado.getText());
    }

    @Test
    void laTablaNoSePuedeEditar() {
        panel.mostrarImagenes(IMAGENES, "");

        assertFalse(panel.modeloTabla.isCellEditable(0, 0));
    }

    @Test
    void sinSeleccionNoHayImagenSeleccionada() {
        panel.mostrarImagenes(IMAGENES, "");

        assertNull(panel.obtenerImagenSeleccionada());
    }

    @Test
    void devuelveLaImagenSeleccionada() {
        panel.mostrarImagenes(IMAGENES, "");
        panel.tablaImagenes.setRowSelectionInterval(1, 1);

        assertEquals("IMG-001", panel.obtenerImagenSeleccionada());
    }

    @Test
    void unaBusquedaNuevaBorraLaImagenAnterior() {
        panel.mostrarImagen(imagen("torax.png", png(50, 50)));

        panel.mostrarImagenes(List.of(), "Sin imágenes");

        assertEquals("", panel.areaDatos.getText());
        assertNull(panel.vistaPrevia.getIcon());
        assertEquals(0, panel.modeloTabla.getRowCount());
    }

    @Test
    void muestraLosDatosYLaVistaPreviaDeUnPng() {
        panel.mostrarImagen(imagen("torax.png", png(50, 50)));

        String datos = panel.areaDatos.getText();
        assertTrue(datos.contains("Imagen: IMG-002"));
        assertTrue(datos.contains("Paciente: FICTICIO-001"));
        assertTrue(datos.contains("Fecha: 02/11/2026 09:30"));
        assertTrue(datos.contains("RIS/PACS: pacs://radiodiagnostico/FICTICIO-001/IMG-002"));
        assertNotNull(panel.vistaPrevia.getIcon());
        assertEquals("", panel.vistaPrevia.getText());
    }

    @Test
    void unDicomNoTieneVistaPrevia() {
        panel.mostrarImagen(imagen("craneo.dcm", new byte[] {1, 2, 3}));

        assertNull(panel.vistaPrevia.getIcon());
        assertEquals("Vista previa no disponible (formato DICOM u otro)", panel.vistaPrevia.getText());
    }

    @Test
    void lasImagenesGrandesSeReducen() {
        ImageIcon icono = PanelConsultarImagen.crearVistaPrevia(png(880, 440));

        assertEquals(220, icono.getIconWidth());
        assertEquals(110, icono.getIconHeight());
    }

    @Test
    void lasImagenesPequenasNoSeAgrandan() {
        ImageIcon icono = PanelConsultarImagen.crearVistaPrevia(png(40, 30));

        assertEquals(40, icono.getIconWidth());
        assertEquals(30, icono.getIconHeight());
    }

    @Test
    void sinContenidoNoHayVistaPrevia() {
        assertNull(PanelConsultarImagen.crearVistaPrevia(null));
        assertNull(PanelConsultarImagen.crearVistaPrevia(new byte[0]));
    }

    @Test
    void losErroresSeMuestranComoAviso() {
        panel.mostrarError("Introduzca el DNI del paciente.");

        assertEquals("Consultar imagen", avisos.tituloError);
        assertEquals("Introduzca el DNI del paciente.", avisos.error);
    }

    @Test
    void elBotonBuscarYLaTeclaIntroLlamanAlControlador() {
        int[] veces = {0};
        panel.alPulsarBuscar(() -> veces[0]++);

        panel.botonBuscar.doClick();
        panel.campoDni.postActionEvent();

        assertEquals(2, veces[0]);
    }

    @Test
    void elBotonVerLlamaAlControlador() {
        int[] veces = {0};
        panel.alPulsarVer(() -> veces[0]++);

        panel.botonVer.doClick();

        assertEquals(1, veces[0]);
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
}
