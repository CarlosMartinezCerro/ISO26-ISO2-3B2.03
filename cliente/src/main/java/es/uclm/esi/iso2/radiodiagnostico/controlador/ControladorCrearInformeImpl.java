package es.uclm.esi.iso2.radiodiagnostico.controlador;

import java.util.Objects;

import es.uclm.esi.iso2.radiodiagnostico.modelo.informe.ClienteInformes;
import es.uclm.esi.iso2.radiodiagnostico.modelo.informe.DatosCreacionInforme;
import es.uclm.esi.iso2.radiodiagnostico.modelo.informe.ErrorComunicacionException;
import es.uclm.esi.iso2.radiodiagnostico.modelo.informe.ResultadoAntecedentes;
import es.uclm.esi.iso2.radiodiagnostico.modelo.informe.ResultadoCreacionInforme;
import es.uclm.esi.iso2.radiodiagnostico.modelo.informe.Informe;
import es.uclm.esi.iso2.radiodiagnostico.vista.VistaCrearInforme;

/** Controlador cliente MVC de informes. */
public class ControladorCrearInformeImpl implements ControladorCrearInforme {

    private final VistaCrearInforme vista;
    private final ClienteInformes cliente;

    public ControladorCrearInformeImpl(VistaCrearInforme vista, ClienteInformes cliente) {
        this.vista = Objects.requireNonNull(vista, "La vista es obligatoria");
        this.cliente = Objects.requireNonNull(cliente, "El cliente es obligatorio");
    }

    @Override
    public void crearInforme() {
        Informe informe = vista.obtenerInforme();
        if (informe == null) {
            vista.mostrarError("Complete los datos obligatorios del informe.");
            return;
        }
        DatosCreacionInforme datos = new DatosCreacionInforme(
                informe.getMedico() == null ? null : informe.getMedico().getDni(),
                informe.getPaciente() == null ? null : informe.getPaciente().getDni(),
                informe.getEspecialidad(),
                informe.getMotivoConsulta(),
                informe.getConsultaActual(),
                informe.getDiagnostico(),
                informe.getPruebas(),
                informe.gettratamiento(),
                informe.isAlta(),
                informe.getFecha(),
                informe.getAnamnesis(),
                informe.obtenerAntecedentes(),
                informe.obtenerHistorial());
        try {
            ResultadoCreacionInforme resultado = cliente.crearInforme(datos);
            if (resultado.correcto()) {
                vista.mostrarExito(resultado.mensaje());
                vista.limpiar();
            } else {
                vista.mostrarError(resultado.mensaje());
            }
        } catch (ErrorComunicacionException excepcion) {
            vista.mostrarError("No se pudo conectar con el servidor.");
        }
    }

    @Override
    public void consultarAntecedentes(String dniPaciente, String especialidad) {
        if (dniPaciente == null || dniPaciente.isBlank()
                || especialidad == null || especialidad.isBlank()) {
            vista.habilitarCamposInforme(false);
            vista.mostrarError("DNI del paciente y especialidad son obligatorios.");
            return;
        }
        try {
            ResultadoAntecedentes resultado =
                    cliente.obtenerAntecedentes(dniPaciente, especialidad);
            if (resultado.correcto()) {
                vista.cargarAntecedentes(
                        resultado.antecedentes(),
                        resultado.historialEspecialidad());
                vista.habilitarCamposInforme(true);
            } else {
                vista.habilitarCamposInforme(false);
                vista.mostrarError(resultado.mensaje());
            }
        } catch (ErrorComunicacionException | IllegalArgumentException excepcion) {
            vista.habilitarCamposInforme(false);
            vista.mostrarError(excepcion.getMessage());
        }
    }
}
