package es.uclm.esi.iso2.radiodiagnostico.modelo.prueba;

import java.util.Locale;
import java.util.Map;

/**
 * Catálogo fijo con los tipos de prueba que ya ofrece el cliente.
 * La radiografía no admite seguimiento periódico; la mamografía sí (programas de cribado).
 */
public class CatalogoTiposPruebaSimulado implements CatalogoTiposPrueba {

    private static final Map<String, Boolean> ADMITE_SEGUIMIENTO = Map.of(
            normalizar("Radiografía"), false,
            normalizar("Ecografía"), true,
            normalizar("Mamografía"), true,
            normalizar("TAC"), true,
            normalizar("Resonancia magnética"), true);

    @Override
    public boolean existe(String tipoPrueba) {
        return tipoPrueba != null && ADMITE_SEGUIMIENTO.containsKey(normalizar(tipoPrueba));
    }

    @Override
    public boolean admiteSeguimiento(String tipoPrueba) {
        return existe(tipoPrueba) && ADMITE_SEGUIMIENTO.get(normalizar(tipoPrueba));
    }

    private static String normalizar(String tipoPrueba) {
        return tipoPrueba.trim().toLowerCase(Locale.ROOT);
    }
}
