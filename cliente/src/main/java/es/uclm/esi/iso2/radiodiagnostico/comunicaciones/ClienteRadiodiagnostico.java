package es.uclm.esi.iso2.radiodiagnostico.comunicaciones;

import es.uclm.esi.iso2.radiodiagnostico.dominio.Prueba;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

public class ClienteRadiodiagnostico {
    private final String host;
    private final int puerto;

    public ClienteRadiodiagnostico() {
        this("localhost", 5000);
    }

    public ClienteRadiodiagnostico(String host, int puerto) {
        this.host = host;
        this.puerto = puerto;
    }

    @SuppressWarnings("unchecked")
    public List<Prueba> consultarPruebasHistoricas(String idPaciente) throws Exception {
        try (Socket socket = new Socket(host, puerto);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            out.writeObject("CONSULTAR_PRUEBAS_HISTORICAS");
            out.writeObject(idPaciente);
            out.flush();

            String estado = (String) in.readObject();
            if ("OK".equals(estado)) {
                return (List<Prueba>) in.readObject();
            } else {
                String mensajeError = (String) in.readObject();
                throw new IllegalArgumentException(mensajeError);
            }
        }
    }
}