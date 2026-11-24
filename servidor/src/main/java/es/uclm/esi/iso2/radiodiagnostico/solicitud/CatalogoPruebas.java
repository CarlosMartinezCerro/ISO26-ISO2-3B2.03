package es.uclm.esi.iso2.radiodiagnostico.solicitud;

import java.util.List;

/** Tipos ofrecidos por la aplicación, alineados con el selector de pruebas existente. */
public final class CatalogoPruebas {

    private static final List<String> DISPONIBLES = List.of(
            "Radiografía", "Ecografía", "Mamografía", "TAC", "Resonancia magnética");

    private CatalogoPruebas() {
    }

    public static List<String> disponibles() {
        return DISPONIBLES;
    }

    public static boolean estaDisponible(String tipoPrueba) {
        return tipoPrueba != null && DISPONIBLES.contains(tipoPrueba.trim());
    }
}
