package es.uclm.esi.iso2.radiodiagnostico.vista;

import es.uclm.esi.iso2.radiodiagnostico.informe.DatosImagen;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

/** Vista de escritorio (Swing) del CdU17. No contiene lógica: todo lo decide el controlador. */
public class VentanaCrearImagen extends JFrame implements VistaCrearImagen {

    private static final long serialVersionUID = 1L;
    private static final String SIN_FICHERO = "Ningún fichero seleccionado";

    private final JTextField campoInforme = new JTextField(12);
    private final JTextField campoPaciente = new JTextField(12);
    private final JTextField campoMedico = new JTextField(12);
    private final JComboBox<String> comboTipoPrueba = new JComboBox<>(new String[] {
        "Radiografía", "Ecografía", "Mamografía", "TAC", "Resonancia magnética"});
    private final JLabel etiquetaFichero = new JLabel(SIN_FICHERO);
    private final JButton botonSeleccionar = new JButton("Seleccionar imagen...");
    private final JButton botonCrear = new JButton("Crear imagen");
    private Path ficheroSeleccionado;

    public VentanaCrearImagen() {
        super("Radiodiagnóstico - CdU17: Crear imagen");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(520, 300);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JPanel formulario = new JPanel(new GridLayout(0, 2, 8, 8));
        formulario.setBorder(BorderFactory.createEmptyBorder(15, 15, 0, 15));
        formulario.add(new JLabel("Informe:"));
        formulario.add(campoInforme);
        formulario.add(new JLabel("DNI paciente:"));
        formulario.add(campoPaciente);
        formulario.add(new JLabel("Médico:"));
        formulario.add(campoMedico);
        formulario.add(new JLabel("Tipo de prueba:"));
        formulario.add(comboTipoPrueba);
        formulario.add(botonSeleccionar);
        formulario.add(etiquetaFichero);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        botones.add(botonCrear);

        add(formulario, BorderLayout.CENTER);
        add(botones, BorderLayout.SOUTH);

        botonSeleccionar.addActionListener(evento -> seleccionarFichero());
    }

    /** El controlador se engancha aquí al botón (la vista no lo conoce). */
    public void alPulsarCrear(Runnable accion) {
        botonCrear.addActionListener(evento -> accion.run());
    }

    private void seleccionarFichero() {
        JFileChooser selector = new JFileChooser();
        if (selector.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            ficheroSeleccionado = selector.getSelectedFile().toPath();
            etiquetaFichero.setText(ficheroSeleccionado.getFileName().toString());
        }
    }

    @Override
    public DatosImagen obtenerDatos() {
        if (ficheroSeleccionado == null) {
            return null;
        }
        try {
            return new DatosImagen(
                    campoInforme.getText().trim(),
                    campoPaciente.getText().trim(),
                    campoMedico.getText().trim(),
                    (String) comboTipoPrueba.getSelectedItem(),
                    ficheroSeleccionado.getFileName().toString(),
                    Files.readAllBytes(ficheroSeleccionado));
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public void mostrarExito(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Imagen creada", JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "No se pudo crear la imagen", JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void limpiar() {
        campoInforme.setText("");
        campoPaciente.setText("");
        campoMedico.setText("");
        comboTipoPrueba.setSelectedIndex(0);
        ficheroSeleccionado = null;
        etiquetaFichero.setText(SIN_FICHERO);
    }
}
