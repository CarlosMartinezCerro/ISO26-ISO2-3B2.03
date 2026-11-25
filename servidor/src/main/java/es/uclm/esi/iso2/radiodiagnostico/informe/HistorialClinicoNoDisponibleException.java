package es.uclm.esi.iso2.radiodiagnostico.informe;

public class HistorialClinicoNoDisponibleException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public HistorialClinicoNoDisponibleException(String mensaje) {
        super(mensaje);
    }

    public HistorialClinicoNoDisponibleException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
