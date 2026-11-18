package es.uclm.esi.iso2.radiodiagnostico.vista;

import es.uclm.esi.iso2.radiodiagnostico.informe.PruebaHistorica;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/** Vista de escritorio (Swing) del CdU10. No contiene lógica: todo lo decide el controlador. */
public class VentanaHistorialPruebas extends JFrame implements VistaHistorialPruebas {

    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final JTextField campoDni = new JTextField(12);
    private final JButton botonBuscar = new JButton("Consultar historial");
    private final JButton botonLimpiar = new JButton("Limpiar");
    private final DefaultTableModel modeloTabla = new DefaultTableModel(
            new String[] {"ID Prueba", "Tipo", "Estado", "Informe", "Ref. PACS", "Fecha"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable tablaPruebas = new JTable(modeloTabla);

    public VentanaHistorialPruebas() {
        super("Radiodiagnóstico - CdU10: Consultar pruebas históricas");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(680, 360);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JPanel panelBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panelBusqueda.setBorder(BorderFactory.createEmptyBorder(5, 10, 0, 10));
        panelBusqueda.add(new JLabel("DNI paciente:"));
        panelBusqueda.add(campoDni);
        panelBusqueda.add(botonBuscar);
        panelBusqueda.add(botonLimpiar);

        add(panelBusqueda, BorderLayout.NORTH);
        add(new JScrollPane(tablaPruebas), BorderLayout.CENTER);

        botonLimpiar.addActionListener(evento -> limpiar());
    }

    public void alPulsarBuscar(Runnable accion) {
        botonBuscar.addActionListener(evento -> accion.run());
    }

    @Override
    public String obtenerDniPaciente() {
        return campoDni.getText().trim();
    }

    @Override
    public void mostrarPruebas(List<PruebaHistorica> pruebas) {
        modeloTabla.setRowCount(0);
        for (PruebaHistorica p : pruebas) {
            String fechaTexto = p.fecha() != null ? p.fecha().format(FORMATO_FECHA) : "Pendiente";
            modeloTabla.addRow(new Object[] {
                p.idPrueba(), p.tipoPrueba(), p.estado(), p.idInforme(), p.referenciaPacs(), fechaTexto
            });
        }
    }

    @Override
    public void mostrarMensaje(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Consulta de historial", JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error en la consulta", JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void limpiar() {
        campoDni.setText("");
        modeloTabla.setRowCount(0);
    }
}