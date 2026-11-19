package es.uclm.esi.iso2.radiodiagnostico.vista;

import javax.swing.JFrame;

/** Ventana del CdU19: solo es el marco que muestra el {@link PanelConsultarImagen}. */
public class VentanaConsultarImagen extends JFrame {

    private static final long serialVersionUID = 1L;

    private final PanelConsultarImagen panel;

    public VentanaConsultarImagen() {
        super("Radiodiagnóstico - CdU19: Consultar imagen");
        panel = new PanelConsultarImagen(new AvisosDialogo(this));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setContentPane(panel);
        setSize(950, 480);
        setLocationRelativeTo(null);
    }

    /** La vista que se le pasa al controlador. */
    public PanelConsultarImagen panel() {
        return panel;
    }
}
