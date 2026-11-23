package es.uclm.esi.iso2.radiodiagnostico.modelo.informe;

public class DniPacienteInvalidoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DniPacienteInvalidoException(String mensaje) {
        super(mensaje);
    }
}