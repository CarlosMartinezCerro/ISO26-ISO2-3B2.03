package es.uclm.esi.iso2.radiodiagnostico.comunicaciones;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServidorRadiodiagnostico {
    public static final int PUERTO = 5000;

    public static void main(String[] args) {
        System.out.println("Iniciando Servidor de Radiodiagnóstico en el puerto " + PUERTO + "...");
        try (ServerSocket serverSocket = new ServerSocket(PUERTO)) {
            while (true) {
                Socket clienteSocket = serverSocket.accept();
                new Thread(new ManejadorCliente(clienteSocket)).start();
            }
        } catch (IOException e) {
            System.err.println("Error en el servidor: " + e.getMessage());
        }
    }
}