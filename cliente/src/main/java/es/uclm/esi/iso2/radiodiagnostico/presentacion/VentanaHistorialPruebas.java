package es.uclm.esi.iso2.radiodiagnostico.presentacion;

import es.uclm.esi.iso2.radiodiagnostico.controlador.ControladorHistorialPruebas;
import es.uclm.esi.iso2.radiodiagnostico.dominio.Prueba;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class VentanaHistorialPruebas extends JFrame {
    private final JTextField txtIdPaciente;
    private final DefaultTableModel modeloTabla;
    private final ControladorHistorialPruebas controlador;

    public VentanaHistorialPruebas() {
        super("Radiodiagnóstico - CdU 10: Consultar Pruebas Históricas");
        this.controlador = new ControladorHistorialPruebas();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 420);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Panel superior de búsqueda
        JPanel panelBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panelBusqueda.add(new JLabel("ID Paciente (ej. PAC-001):"));
        txtIdPaciente = new JTextField(15);
        txtIdPaciente.setText("PAC-001");
        JButton btnBuscar = new JButton("Consultar historial");
        panelBusqueda.add(txtIdPaciente);
        panelBusqueda.add(btnBuscar);

        // Tabla de resultados
        String[] columnas = {"ID Prueba", "Fecha", "Tipo de Prueba", "Estado", "Informe", "Imagen"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable tabla = new JTable(modeloTabla);
        JScrollPane scrollTabla = new JScrollPane(tabla);

        add(panelBusqueda, BorderLayout.NORTH);
        add(scrollTabla, BorderLayout.CENTER);

        btnBuscar.addActionListener(e -> buscarPruebas());
    }

    private void buscarPruebas() {
        modeloTabla.setRowCount(0);
        String idPaciente = txtIdPaciente.getText();
        try {
            List<Prueba> pruebas = controlador.obtenerHistorial(idPaciente);
            if (pruebas.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "El paciente " + idPaciente + " no tiene pruebas históricas realizadas.",
                        "Sin resultados", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            for (Prueba p : pruebas) {
                modeloTabla.addRow(new Object[]{
                        p.getIdPrueba(),
                        p.getFechaRealizacion(),
                        p.getTipoPrueba(),
                        p.getEstado(),
                        p.getInforme(),
                        p.getRutaImagen()
                });
            }
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Aviso", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo conectar con el servidor: " + ex.getMessage(),
                    "Error de conexión", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new VentanaHistorialPruebas().setVisible(true));
    }
}