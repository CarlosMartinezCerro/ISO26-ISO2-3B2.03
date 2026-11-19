package es.uclm.esi.iso2.radiodiagnostico.vista;

import java.awt.Component;
import javax.swing.JOptionPane;

/** Avisos con los diálogos de Swing ({@link JOptionPane}). */
public class AvisosDialogo implements Avisos {

    private final Component padre;

    public AvisosDialogo(Component padre) {
        this.padre = padre;
    }

    @Override
    public void informacion(String titulo, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, titulo, JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public void error(String titulo, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, titulo, JOptionPane.ERROR_MESSAGE);
    }
}
