package es.uclm.esi.iso2.radiodiagnostico.vista;

import es.uclm.esi.iso2.radiodiagnostico.cita.DatosConfirmacionCita;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

/** Vista de escritorio (Swing) del CdU2. No contiene lógica: todo lo decide el controlador. */
public class VentanaConfirmarCita extends JFrame implements VistaConfirmarCita {

    private static final long serialVersionUID = 1L;

    private final JTextField campoCita = new JTextField(14);
    private final JTextField campoDni = new JTextField(14);
    private final JButton botonConfirmar = new JButton("Confirmar cita");

    public VentanaConfirmarCita() {
        super("Radiodiagnóstico - CdU2: Confirmar cita");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(420, 180);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JPanel formulario = new JPanel(new GridLayout(0, 2, 8, 8));
        formulario.setBorder(BorderFactory.createEmptyBorder(15, 15, 0, 15));
        formulario.add(new JLabel("Código de la cita:"));
        formulario.add(campoCita);
        formulario.add(new JLabel("DNI:"));
        formulario.add(campoDni);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        botones.add(botonConfirmar);

        add(formulario, BorderLayout.CENTER);
        add(botones, BorderLayout.SOUTH);
    }

    /** El controlador se engancha aquí al botón (la vista no lo conoce). */
    public void alPulsarConfirmar(Runnable accion) {
        botonConfirmar.addActionListener(evento -> accion.run());
    }

    @Override
    public DatosConfirmacionCita obtenerDatos() {
        String idCita = campoCita.getText().trim();
        String dni = campoDni.getText().trim();
        if (idCita.isEmpty() || dni.isEmpty()) {
            return null;
        }
        return new DatosConfirmacionCita(idCita, dni);
    }

    @Override
    public void mostrarExito(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Cita confirmada", JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "No se pudo confirmar", JOptionPane.ERROR_MESSAGE);
    }
}
