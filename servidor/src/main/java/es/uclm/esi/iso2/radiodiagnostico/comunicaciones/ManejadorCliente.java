package es.uclm.esi.iso2.radiodiagnostico.comunicaciones;

import es.uclm.esi.iso2.radiodiagnostico.dominio.Prueba;
import es.uclm.esi.iso2.radiodiagnostico.negocio.GestorPruebas;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

public class ManejadorCliente implements Runnable {
    private final Socket socket;
    private final GestorPruebas gestorPruebas;

    public ManejadorCliente(Socket socket) {
        this.socket = socket;
        this.gestorPruebas = new GestorPruebas();
    }

    @Override
    public void run() {
        try (ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            String operacion = (String) in.readObject();

            if ("CONSULTAR_PRUEBAS_HISTORICAS".equals(operacion)) {
                String idPaciente = (String) in.readObject();
                try {
                    List<Prueba> historial = gestorPruebas.consultarPruebasHistoricas(idPaciente);
                    out.writeObject("OK");
                    out.writeObject(historial);
                } catch (IllegalArgumentException e) {
                    out.writeObject("ERROR");
                    out.writeObject(e.getMessage());
                }
            } else {
                out.writeObject("ERROR");
                out.writeObject("Operación no soportada: " + operacion);
            }
            out.flush();
        } catch (Exception e) {
            System.err.println("Error atendiendo al cliente: " + e.getMessage());
        }
    }
}