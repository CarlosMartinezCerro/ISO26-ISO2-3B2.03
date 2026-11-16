package es.uclm.esi.iso2.radiodiagnostico.controlador;

import es.uclm.esi.iso2.radiodiagnostico.comunicaciones.ClienteRadiodiagnostico;
import es.uclm.esi.iso2.radiodiagnostico.dominio.Prueba;

import java.util.List;

public class ControladorHistorialPruebas {
    private final ClienteRadiodiagnostico cliente;

    public ControladorHistorialPruebas() {
        this.cliente = new ClienteRadiodiagnostico();
    }

    public List<Prueba> obtenerHistorial(String idPaciente) throws Exception {
        return cliente.consultarPruebasHistoricas(idPaciente);
    }
}