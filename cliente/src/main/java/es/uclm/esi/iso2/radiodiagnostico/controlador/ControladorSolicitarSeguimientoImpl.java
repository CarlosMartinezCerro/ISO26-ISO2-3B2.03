package es.uclm.esi.iso2.radiodiagnostico.controlador;

import es.uclm.esi.iso2.radiodiagnostico.modelo.prueba.ClienteSeguimientos;
import es.uclm.esi.iso2.radiodiagnostico.modelo.prueba.DatosSolicitudSeguimiento;
import es.uclm.esi.iso2.radiodiagnostico.modelo.prueba.ErrorComunicacionException;
import es.uclm.esi.iso2.radiodiagnostico.modelo.prueba.ResultadoSolicitudSeguimiento;
import es.uclm.esi.iso2.radiodiagnostico.vista.VistaSolicitarSeguimiento;
import java.util.Objects;

/**
 * Controlador del cliente para el CdU13. Solo comprueba que el formulario esté completo y que los
 * números sean positivos; el resto de reglas las valida el servidor y su mensaje se muestra tal cual.
 */
public class ControladorSolicitarSeguimientoImpl implements ControladorSolicitarSeguimiento {

    static final String MENSAJE_SIN_CONEXION =
            "No se ha podido conectar con el servidor. Inténtelo de nuevo más tarde.";
    static final String MENSAJE_FORMATO_INCORRECTO = "Revise los números y las fechas: los números "
            + "deben ser enteros y las fechas tener el formato dd/mm/aaaa.";
    static final String MENSAJE_SIN_FIN =
            "Indique el número de repeticiones o la fecha límite del seguimiento.";

    private final VistaSolicitarSeguimiento vista;
    private final ClienteSeguimientos cliente;

    public ControladorSolicitarSeguimientoImpl(VistaSolicitarSeguimiento vista,
                                               ClienteSeguimientos cliente) {
        this.vista = Objects.requireNonNull(vista, "La vista es obligatoria");
        this.cliente = Objects.requireNonNull(cliente, "El cliente es obligatorio");
    }

    @Override
    public void solicitar() {
        DatosSolicitudSeguimiento datos = vista.obtenerDatos();
        if (datos == null) {
            vista.mostrarError(MENSAJE_FORMATO_INCORRECTO);
            return;
        }
        String error = validar(datos);
        if (error != null) {
            vista.mostrarError(error);
            return;
        }
        try {
            ResultadoSolicitudSeguimiento resultado = cliente.solicitar(datos);
            if (resultado == null) {
                vista.mostrarError(MENSAJE_SIN_CONEXION);
            } else if (resultado.correcto()) {
                vista.mostrarRepeticiones(resultado.mensaje(), resultado.repeticiones());
            } else {
                vista.mostrarError(resultado.mensaje());
            }
        } catch (ErrorComunicacionException e) {
            vista.mostrarError(MENSAJE_SIN_CONEXION);
        }
    }

    /** @return el primer error encontrado, o null si el formulario está completo */
    private static String validar(DatosSolicitudSeguimiento datos) {
        if (vacio(datos.dniPaciente())) {
            return "Indique el DNI del paciente.";
        }
        if (vacio(datos.dniMedico())) {
            return "Indique el DNI del médico.";
        }
        if (vacio(datos.tipoPrueba())) {
            return "Seleccione el tipo de prueba.";
        }
        if (vacio(datos.indicacionClinica())) {
            return "Indique la indicación clínica.";
        }
        if (vacio(datos.justificacion())) {
            return "Indique la justificación.";
        }
        if (datos.prioridad() == null) {
            return "Seleccione la prioridad.";
        }
        if (datos.cada() == null || datos.unidad() == null) {
            return "Indique cada cuánto se repite la prueba.";
        }
        if (datos.cada() <= 0) {
            return "La periodicidad debe ser un número mayor que 0.";
        }
        if (datos.fechaInicio() == null) {
            return "Indique la fecha de inicio.";
        }
        if (datos.numeroRepeticiones() == null && datos.fechaLimite() == null) {
            return MENSAJE_SIN_FIN;
        }
        if (datos.numeroRepeticiones() != null && datos.numeroRepeticiones() <= 0) {
            return "El número de repeticiones debe ser mayor que 0.";
        }
        return null;
    }

    private static boolean vacio(String texto) {
        return texto == null || texto.isBlank();
    }
}
