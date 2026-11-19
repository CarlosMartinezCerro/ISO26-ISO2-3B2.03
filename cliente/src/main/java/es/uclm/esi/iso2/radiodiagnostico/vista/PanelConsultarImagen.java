package es.uclm.esi.iso2.radiodiagnostico.vista;

import es.uclm.esi.iso2.radiodiagnostico.informe.ImagenConsultada;
import es.uclm.esi.iso2.radiodiagnostico.informe.ResumenImagen;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

/**
 * Contenido de la pantalla del CdU19 (Swing). No contiene lógica: todo lo decide el controlador.
 * Es un panel, no una ventana, para poder probarlo sin pantalla; lo muestra {@link VentanaConsultarImagen}.
 */
public class PanelConsultarImagen extends JPanel implements VistaConsultarImagen {

    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final int TAMANO_VISTA_PREVIA = 220;

    final JTextField campoDni = new JTextField(14);
    final JButton botonBuscar = new JButton("Buscar imágenes");
    final JButton botonVer = new JButton("Ver imagen");
    final JLabel etiquetaEstado = new JLabel(" ");
    final DefaultTableModel modeloTabla;
    final JTable tablaImagenes;
    final JTextArea areaDatos = new JTextArea(7, 28);
    final JLabel vistaPrevia = new JLabel("", SwingConstants.CENTER);

    private final transient Avisos avisos;

    public PanelConsultarImagen(Avisos avisos) {
        super(new BorderLayout(10, 10));
        this.avisos = avisos;

        JPanel panelBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        panelBusqueda.add(new JLabel("DNI del paciente:"));
        panelBusqueda.add(campoDni);
        panelBusqueda.add(botonBuscar);
        panelBusqueda.add(etiquetaEstado);

        modeloTabla = new DefaultTableModel(
                new String[] {"Imagen", "Informe", "Tipo de prueba", "Fecha", "Fichero"}, 0) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
        tablaImagenes = new JTable(modeloTabla);
        tablaImagenes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        areaDatos.setEditable(false);
        JPanel panelDetalle = new JPanel(new BorderLayout(5, 5));
        panelDetalle.setBorder(BorderFactory.createTitledBorder("Imagen seleccionada"));
        panelDetalle.add(new JScrollPane(areaDatos), BorderLayout.NORTH);
        panelDetalle.add(vistaPrevia, BorderLayout.CENTER);

        JSplitPane centro = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(tablaImagenes), panelDetalle);
        centro.setResizeWeight(0.55);

        JPanel panelAcciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        panelAcciones.add(botonVer);

        add(panelBusqueda, BorderLayout.NORTH);
        add(centro, BorderLayout.CENTER);
        add(panelAcciones, BorderLayout.SOUTH);
    }

    /** El controlador se engancha aquí a los botones (la vista no lo conoce). */
    public void alPulsarBuscar(Runnable accion) {
        botonBuscar.addActionListener(evento -> accion.run());
        campoDni.addActionListener(evento -> accion.run());
    }

    public void alPulsarVer(Runnable accion) {
        botonVer.addActionListener(evento -> accion.run());
    }

    @Override
    public String obtenerDniPaciente() {
        return campoDni.getText().trim();
    }

    @Override
    public String obtenerImagenSeleccionada() {
        int fila = tablaImagenes.getSelectedRow();
        return fila < 0 ? null : (String) modeloTabla.getValueAt(fila, 0);
    }

    @Override
    public void mostrarImagenes(List<ResumenImagen> imagenes, String mensaje) {
        modeloTabla.setRowCount(0);
        for (ResumenImagen imagen : imagenes) {
            modeloTabla.addRow(new Object[] {imagen.id(), imagen.idInforme(), imagen.tipoPrueba(),
                imagen.fechaCreacion().format(FORMATO), imagen.nombreFichero()});
        }
        areaDatos.setText("");
        vistaPrevia.setIcon(null);
        vistaPrevia.setText("");
        etiquetaEstado.setText(mensaje);
    }

    @Override
    public void mostrarImagen(ImagenConsultada imagen) {
        areaDatos.setText("Imagen: " + imagen.id()
                + "\nInforme: " + imagen.idInforme()
                + "\nPaciente: " + imagen.dniPaciente()
                + "\nTipo de prueba: " + imagen.tipoPrueba()
                + "\nFecha: " + imagen.fechaCreacion().format(FORMATO)
                + "\nFichero: " + imagen.nombreFichero()
                + "\nRIS/PACS: " + imagen.referenciaPacs());
        ImageIcon icono = crearVistaPrevia(imagen.contenido());
        vistaPrevia.setIcon(icono);
        vistaPrevia.setText(icono == null ? "Vista previa no disponible (formato DICOM u otro)" : "");
    }

    /** PNG/JPG se pueden pintar; DICOM necesita un visor específico. */
    static ImageIcon crearVistaPrevia(byte[] contenido) {
        if (contenido == null || contenido.length == 0) {
            return null;
        }
        try {
            BufferedImage leida = ImageIO.read(new ByteArrayInputStream(contenido));
            if (leida == null) {
                return null;
            }
            double escala = Math.min(1.0, (double) TAMANO_VISTA_PREVIA
                    / Math.max(leida.getWidth(), leida.getHeight()));
            return new ImageIcon(leida.getScaledInstance((int) (leida.getWidth() * escala),
                    (int) (leida.getHeight() * escala), Image.SCALE_SMOOTH));
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public void mostrarError(String mensaje) {
        avisos.error("Consultar imagen", mensaje);
    }
}
