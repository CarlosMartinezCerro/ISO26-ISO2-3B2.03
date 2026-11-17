package es.uclm.esi.iso2.radiodiagnostico.vista;

import es.uclm.esi.iso2.radiodiagnostico.controlador.ControladorConsultarImagen;
import es.uclm.esi.iso2.radiodiagnostico.controlador.ControladorConsultarImagenImpl;
import es.uclm.esi.iso2.radiodiagnostico.informe.ClienteConsultaImagenes;
import es.uclm.esi.iso2.radiodiagnostico.informe.ImagenConsultada;
import es.uclm.esi.iso2.radiodiagnostico.informe.RespuestaConsultaImagen;
import es.uclm.esi.iso2.radiodiagnostico.informe.RespuestaListadoImagenes;
import es.uclm.esi.iso2.radiodiagnostico.informe.ResumenImagen;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDateTime;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;

/**
 * Demo para ver la ventana del CdU19 sin servidor.
 * Usa un cliente falso con datos ficticios (RGPD). No es código de producción.
 * Prueba con el DNI FICTICIO-001.
 */
public final class DemoVentanaConsultarImagen {

    private DemoVentanaConsultarImagen() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            VentanaConsultarImagen ventana = new VentanaConsultarImagen();
            ControladorConsultarImagen controlador =
                    new ControladorConsultarImagenImpl(ventana, new ClienteFalso(), "MED-001");
            ventana.alPulsarBuscar(controlador::buscarImagenes);
            ventana.alPulsarVer(controlador::verImagen);
            ventana.setVisible(true);
        });
    }

    private static final class ClienteFalso implements ClienteConsultaImagenes {

        private final List<ImagenConsultada> imagenes = List.of(
                new ImagenConsultada("IMG-002", "INF-002", "FICTICIO-001", "Radiografía",
                        LocalDateTime.of(2026, 11, 2, 9, 30), "torax.png", pngDePrueba(),
                        "pacs://radiodiagnostico/FICTICIO-001/IMG-002"),
                new ImagenConsultada("IMG-001", "INF-001", "FICTICIO-001", "TAC",
                        LocalDateTime.of(2026, 9, 10, 12, 0), "craneo.dcm", new byte[] {1, 2, 3},
                        "pacs://radiodiagnostico/FICTICIO-001/IMG-001"));

        @Override
        public RespuestaListadoImagenes obtenerImagenesPaciente(String idMedico, String dniPaciente) {
            List<ResumenImagen> resumen = imagenes.stream()
                    .filter(i -> i.dniPaciente().equals(dniPaciente))
                    .map(i -> new ResumenImagen(i.id(), i.idInforme(), i.tipoPrueba(),
                            i.fechaCreacion(), i.nombreFichero()))
                    .toList();
            String mensaje = resumen.isEmpty()
                    ? "El paciente " + dniPaciente + " no tiene imágenes disponibles."
                    : resumen.size() + " imagen(es) encontradas.";
            return new RespuestaListadoImagenes(true, mensaje, resumen);
        }

        @Override
        public RespuestaConsultaImagen obtenerImagen(String idMedico, String idImagen) {
            return imagenes.stream()
                    .filter(i -> i.id().equals(idImagen))
                    .findFirst()
                    .map(i -> new RespuestaConsultaImagen(true, "Imagen recuperada.", i))
                    .orElse(new RespuestaConsultaImagen(false, "No existe la imagen " + idImagen + ".", null));
        }

        /** Dibuja una "radiografía" ficticia para que se vea la vista previa. */
        private static byte[] pngDePrueba() {
            BufferedImage imagen = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = imagen.createGraphics();
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, 200, 200);
            g.setColor(Color.LIGHT_GRAY);
            g.setStroke(new BasicStroke(4));
            g.drawOval(40, 30, 120, 150);
            for (int y = 60; y < 170; y += 18) {
                g.drawArc(55, y, 90, 30, 0, 180);
            }
            g.drawLine(100, 30, 100, 180);
            g.dispose();
            try (ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
                ImageIO.write(imagen, "png", salida);
                return salida.toByteArray();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }
}
