package es.uclm.esi.iso2.radiodiagnostico.modelo.prueba;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import org.junit.jupiter.api.Test;

/** Pruebas de las implementaciones simuladas del catálogo y del registro de médicos. */
class SimuladosSeguimientoTest {

    private final CatalogoTiposPrueba catalogo = new CatalogoTiposPruebaSimulado();

    @Test
    void elCatalogoReconoceLosTiposSinDistinguirMayusculasNiEspacios() {
        assertTrue(catalogo.existe(" tac "));
        assertTrue(catalogo.existe("RESONANCIA MAGNÉTICA"));
        assertFalse(catalogo.existe("Gammagrafía"));
        assertFalse(catalogo.existe(null));
    }

    @Test
    void laMamografiaAdmiteSeguimientoYLaRadiografiaNo() {
        assertTrue(catalogo.admiteSeguimiento("Mamografía"));
        assertTrue(catalogo.admiteSeguimiento("Ecografía"));
        assertFalse(catalogo.admiteSeguimiento("Radiografía"));
        assertFalse(catalogo.admiteSeguimiento("Gammagrafía"));
    }

    @Test
    void elRegistroReconoceLosMedicosFicticios() {
        RegistroMedicos medicos = new RegistroMedicosSimulado();

        assertTrue(medicos.esMedico("00000001R"));
        assertTrue(medicos.esMedico(" 00000002w "));
        assertFalse(medicos.esMedico("99999999R"));
        assertFalse(medicos.esMedico(null));
    }

    @Test
    void elRegistroAdmiteOtrosMedicos() {
        assertTrue(new RegistroMedicosSimulado(Set.of("11111111H")).esMedico("11111111H"));
    }
}
