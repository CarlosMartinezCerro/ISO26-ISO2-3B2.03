package es.uclm.esi.iso2.radiodiagnostico.controlador;

import es.uclm.esi.iso2.radiodiagnostico.prueba.ClienteConsultaSeguimientos;
import es.uclm.esi.iso2.radiodiagnostico.prueba.ErrorComunicacionException;
import es.uclm.esi.iso2.radiodiagnostico.prueba.ResultadoConsultaSeguimientos;
import es.uclm.esi.iso2.radiodiagnostico.vista.VistaConsultarSeguimientos;
import java.util.List;
import java.util.Objects;

/**
 * Implementación del controlador del CdU11. Sigue el mismo flujo que
 * {@link ControladorHistorialPruebasImpl} (CdU10): lee el DNI de la vista, consulta al servidor y
 * muestra el resultado, un mensaje si no hay datos o un error.
 */
public class ControladorConsultarSeguimientosImpl implements ControladorConsultarSeguimientos {

    static final String MENSAJE_DNI_VACIO =
            "Introduzca el DNI del paciente para consultar sus pruebas de seguimiento.";

    private final VistaConsultarSeguimientos vista;
    private final ClienteConsultaSeguimientos cliente;

    public ControladorConsultarSeguimientosImpl(VistaConsultarSeguimientos vista,
                                                ClienteConsultaSeguimientos cliente) {
        this.vista = Objects.requireNonNull(vista, "La vista es obligatoria");
        this.cliente = Objects.requireNonNull(cliente, "El cliente es obligatorio");
    }

    @Override
    public void consultarSeguimientos() {
        String dni = vista.obtenerDniPaciente();
        if (dni == null || dni.isBlank()) {
            vista.mostrarError(MENSAJE_DNI_VACIO);
            return;
        }
        try {
            ResultadoConsultaSeguimientos resultado = cliente.consultarSeguimientos(dni.trim());
            if (resultado.correcto()) {
                vista.mostrarSeguimientos(resultado.seguimientos());
                if (resultado.seguimientos().isEmpty()) {
                    vista.mostrarMensaje(resultado.mensaje());
                }
            } else {
                vista.mostrarSeguimientos(List.of());
                vista.mostrarError(resultado.mensaje());
            }
        } catch (ErrorComunicacionException e) {
            // Mismo mensaje que en el CdU10 para que el usuario vea siempre el mismo aviso.
            vista.mostrarError(ControladorHistorialPruebasImpl.MENSAJE_SIN_CONEXION);
        }
    }
}
