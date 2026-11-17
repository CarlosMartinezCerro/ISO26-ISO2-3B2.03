package es.uclm.esi.iso2.radiodiagnostico.vista;

import es.uclm.esi.iso2.radiodiagnostico.cita.DatosAsignacionCita;
import es.uclm.esi.iso2.radiodiagnostico.cita.SolicitudPendiente;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

/** Vista de escritorio (Swing) del CdU25. No contiene lógica: todo lo decide el controlador. */
public class VentanaAsignarCita extends JFrame implements VistaAsignarCita {

    private static final long serialVersionUID = 1L;

    private final DefaultTableModel modeloTabla;
    private final JTable tablaSolicitudes;
    private final JTextField campoFecha = new JTextField(10);
    private final JTextField campoHora = new JTextField(5);
    private final JTextField campoSala = new JTextField(10);
    private final JButton botonAsignar = new JButton("Asignar cita");
    private final JButton botonRecargar = new JButton("Recargar");

    public VentanaAsignarCita() {
        super("Radiodiagnóstico - CdU25: Asignar cita solicitada");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(750, 400);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        modeloTabla = new DefaultTableModel(
                new String[] {"Solicitud", "Paciente", "Tipo de prueba", "Prioridad"}, 0) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
        tablaSolicitudes = new JTable(modeloTabla);
        tablaSolicitudes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JPanel panelDatos = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        panelDatos.add(new JLabel("Fecha (aaaa-mm-dd):"));
        panelDatos.add(campoFecha);
        panelDatos.add(new JLabel("Hora (hh:mm):"));
        panelDatos.add(campoHora);
        panelDatos.add(new JLabel("Sala:"));
        panelDatos.add(campoSala);
        panelDatos.add(botonAsignar);
        panelDatos.add(botonRecargar);

        add(new JScrollPane(tablaSolicitudes), BorderLayout.CENTER);
        add(panelDatos, BorderLayout.SOUTH);
    }

    /** El controlador se engancha aquí a los botones (la vista no lo conoce). */
    public void alPulsarAsignar(Runnable accion) {
        botonAsignar.addActionListener(evento -> accion.run());
    }

    public void alPulsarRecargar(Runnable accion) {
        botonRecargar.addActionListener(evento -> accion.run());
    }

    @Override
    public void mostrarSolicitudes(List<SolicitudPendiente> solicitudes) {
        modeloTabla.setRowCount(0);
        for (SolicitudPendiente s : solicitudes) {
            modeloTabla.addRow(new Object[] {s.id(), s.dniPaciente(), s.tipoPrueba(), s.prioridad()});
        }
    }

    @Override
    public DatosAsignacionCita obtenerDatos() {
        int fila = tablaSolicitudes.getSelectedRow();
        if (fila < 0) {
            return null;
        }
        try {
            LocalDateTime fechaHora = LocalDateTime.of(
                    LocalDate.parse(campoFecha.getText().trim()),
                    LocalTime.parse(campoHora.getText().trim()));
            String idSolicitud = (String) modeloTabla.getValueAt(fila, 0);
            return new DatosAsignacionCita(idSolicitud, fechaHora, campoSala.getText().trim());
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    @Override
    public void mostrarExito(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Cita asignada", JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "No se pudo asignar", JOptionPane.ERROR_MESSAGE);
    }
}
