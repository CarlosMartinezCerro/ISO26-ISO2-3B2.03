package es.uclm.esi.iso2.radiodiagnostico.prueba;

/** Unidad en la que se expresa la periodicidad del seguimiento (CdU13). */
public enum UnidadPeriodicidad {
    DIAS("días"),
    SEMANAS("semanas"),
    MESES("meses");

    private final String texto;

    UnidadPeriodicidad(String texto) {
        this.texto = texto;
    }

    /** Texto que se muestra al usuario. */
    @Override
    public String toString() {
        return texto;
    }
}
