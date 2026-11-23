package es.uclm.esi.iso2.radiodiagnostico.modelo.prueba;

import java.time.LocalDate;

/** Cada cuánto se repite una prueba de seguimiento: "cada N días, semanas o meses". */
public record PeriodicidadSeguimiento(int cada, UnidadPeriodicidad unidad) {

    /**
     * Fecha prevista de la repetición {@code indice} (0 es la fecha de inicio).
     * Se calcula siempre desde la fecha de inicio y no desde la repetición anterior,
     * para que los meses cortos no desplacen el resto (31/01, 28/02, 31/03).
     */
    public LocalDate fechaRepeticion(LocalDate inicio, int indice) {
        long salto = (long) cada * indice;
        return switch (unidad) {
            case DIAS -> inicio.plusDays(salto);
            case SEMANAS -> inicio.plusWeeks(salto);
            case MESES -> inicio.plusMonths(salto);
        };
    }
}
