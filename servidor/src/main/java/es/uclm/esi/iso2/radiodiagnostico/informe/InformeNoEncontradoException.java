package es.uclm.esi.iso2.radiodiagnostico.informe;

public class InformeNoEncontradoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public InformeNoEncontradoException(long id) {
        super("No existe o no es valido el informe con id " + id + ".");
    }
}
