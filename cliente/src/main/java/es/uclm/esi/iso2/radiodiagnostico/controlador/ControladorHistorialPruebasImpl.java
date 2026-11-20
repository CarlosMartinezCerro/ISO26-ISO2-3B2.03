package es.uclm.esi.iso2.radiodiagnostico.controlador;

import es.uclm.esi.iso2.radiodiagnostico.informe.ClienteHistorialPruebas;
import es.uclm.esi.iso2.radiodiagnostico.informe.ErrorComunicacionException;
import es.uclm.esi.iso2.radiodiagnostico.informe.ResultadoHistorialPruebas;
import es.uclm.esi.iso2.radiodiagnostico.vista.VistaHistorialPruebas;
import java.util.List;
import java.util.Objects;

public class ControladorHistorialPruebasImpl implements ControladorHistorialPruebas {

    static final String MENSAJE_SIN_CONEXION =
            "No se ha podido conectar con el servidor. Inténtelo de nuevo más tarde.";
    static final String MENSAJE_DNI_VACIO =
            "Introduzca el DNI del paciente para consultar su historial de pruebas.";

    private final VistaHistorialPruebas vista;
    private final ClienteHistorialPruebas cliente;

    public ControladorHistorialPruebasImpl(VistaHistorialPruebas vista, ClienteHistorialPruebas cliente) {
        this.vista = Objects.requireNonNull(vista, "La vista es obligatoria");
        this.cliente = Objects.requireNonNull(cliente, "El cliente es obligatorio");
    }

    @Override
    public void consultarPruebasHistoricas() {
        String dni = vista.obtenerDniPaciente();
        if (dni == null || dni.isBlank()) {
            vista.mostrarError(MENSAJE_DNI_VACIO);
            return;
        }
        try {
            ResultadoHistorialPruebas resultado = cliente.consultarPruebasHistoricas(dni.trim());
            if (resultado.correcto()) {
                vista.mostrarPruebas(resultado.pruebas());
                if (resultado.pruebas().isEmpty()) {
                    vista.mostrarMensaje(resultado.mensaje());
                }
            } else {
                vista.mostrarPruebas(List.of());
                vista.mostrarError(resultado.mensaje());
            }
        } catch (ErrorComunicacionException e) {
            vista.mostrarError(MENSAJE_SIN_CONEXION);
        }
    }
}