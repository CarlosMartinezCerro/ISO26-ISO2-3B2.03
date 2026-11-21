package es.uclm.esi.iso2.radiodiagnostico.notificacion;

/** No existe ningún paciente dado de alta (CdU22) con el DNI indicado. */
public class PacienteNoEncontradoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public PacienteNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
