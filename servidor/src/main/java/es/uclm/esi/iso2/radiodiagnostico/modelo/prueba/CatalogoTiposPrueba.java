package es.uclm.esi.iso2.radiodiagnostico.modelo.prueba;

/**
 * Catálogo de tipos de prueba del servicio de radiodiagnóstico.
 * De momento solo hay una implementación simulada; se sustituirá cuando exista el catálogo real.
 */
public interface CatalogoTiposPrueba {

    boolean existe(String tipoPrueba);

    /** Indica si el tipo de prueba puede pedirse como seguimiento periódico (CdU13). */
    boolean admiteSeguimiento(String tipoPrueba);
}
