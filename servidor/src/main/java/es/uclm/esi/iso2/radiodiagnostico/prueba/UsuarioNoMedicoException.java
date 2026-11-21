package es.uclm.esi.iso2.radiodiagnostico.prueba;

public class UsuarioNoMedicoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public UsuarioNoMedicoException(String mensaje) {
        super(mensaje);
    }
}
