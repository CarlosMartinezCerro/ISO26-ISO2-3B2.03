package es.uclm.esi.iso2.radiodiagnostico.modelo.paciente;

public class DatosInvalidosException extends RuntimeException {

    public DatosInvalidosException(String mensaje) {
        super(mensaje);
    }
}