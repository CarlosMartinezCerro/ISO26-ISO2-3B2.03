package es.uclm.esi.iso2.radiodiagnostico.modelo.paciente;

public class PacienteDuplicadoException extends RuntimeException {

    public PacienteDuplicadoException(String mensaje) {
        super(mensaje);
    }
}
