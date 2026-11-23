package es.uclm.esi.iso2.radiodiagnostico.controlador;

import es.uclm.esi.iso2.radiodiagnostico.modelo.paciente.ClientePacientes;
import es.uclm.esi.iso2.radiodiagnostico.modelo.paciente.DatosAltaPaciente;
import es.uclm.esi.iso2.radiodiagnostico.modelo.paciente.ErrorComunicacionException;
import es.uclm.esi.iso2.radiodiagnostico.modelo.paciente.ResultadoAlta;
import es.uclm.esi.iso2.radiodiagnostico.vista.VistaAltaPaciente;
import java.util.Objects;

public class ControladorAltaPacienteImpl implements ControladorAltaPaciente {

    static final String MENSAJE_SIN_CONEXION =
            "No se ha podido conectar con el servidor. Inténtelo de nuevo más tarde.";
    static final String MENSAJE_FECHA_INCORRECTA =
            "La fecha de nacimiento no es válida. Use el formato dd/mm/aaaa.";

    private final VistaAltaPaciente vista;
    private final ClientePacientes cliente;

    public ControladorAltaPacienteImpl(VistaAltaPaciente vista, ClientePacientes cliente) {
        this.vista = Objects.requireNonNull(vista, "La vista es obligatoria");
        this.cliente = Objects.requireNonNull(cliente, "El cliente es obligatorio");
    }

    @Override
    public void darDeAlta() {
        DatosAltaPaciente datos = vista.obtenerDatos();
        if (datos == null) {
            vista.mostrarError(MENSAJE_FECHA_INCORRECTA);
            return;
        }
        try {
            ResultadoAlta resultado = cliente.darDeAlta(datos);
            if (resultado.correcto()) {
                vista.mostrarExito(resultado.mensaje());
                vista.limpiar();
            } else {
                vista.mostrarError(resultado.mensaje());
            }
        } catch (ErrorComunicacionException e) {
            vista.mostrarError(MENSAJE_SIN_CONEXION);
        }
    }
}
