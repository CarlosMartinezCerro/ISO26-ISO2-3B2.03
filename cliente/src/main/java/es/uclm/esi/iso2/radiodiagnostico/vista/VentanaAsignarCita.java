package es.uclm.esi.iso2.radiodiagnostico.vista;

import javax.swing.JFrame;

/** Ventana del CdU25: solo es el marco que muestra el {@link PanelAsignarCita}. */
public class VentanaAsignarCita extends JFrame {

    private static final long serialVersionUID = 1L;

    private final PanelAsignarCita panel;

    public VentanaAsignarCita() {
        super("Radiodiagnóstico - CdU25: Asignar cita solicitada");
        panel = new PanelAsignarCita(new AvisosDialogo(this));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setContentPane(panel);
        setSize(900, 400);
        setLocationRelativeTo(null);
    }

    /** La vista que se le pasa al controlador. */
    public PanelAsignarCita panel() {
        return panel;
    }
}
