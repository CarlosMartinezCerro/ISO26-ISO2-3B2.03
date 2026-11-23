package es.uclm.esi.iso2.radiodiagnostico.modelo.prueba;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class PeriodicidadSeguimientoTest {

    private static final LocalDate INICIO = LocalDate.of(2027, 1, 31);

    @Test
    void laPrimeraRepeticionEsLaFechaDeInicio() {
        assertEquals(INICIO, new PeriodicidadSeguimiento(5, UnidadPeriodicidad.DIAS)
                .fechaRepeticion(INICIO, 0));
    }

    @Test
    void sumaDias() {
        assertEquals(LocalDate.of(2027, 2, 10), new PeriodicidadSeguimiento(5, UnidadPeriodicidad.DIAS)
                .fechaRepeticion(INICIO, 2));
    }

    @Test
    void sumaSemanas() {
        assertEquals(LocalDate.of(2027, 2, 28), new PeriodicidadSeguimiento(2, UnidadPeriodicidad.SEMANAS)
                .fechaRepeticion(INICIO, 2));
    }

    @Test
    void losMesesSeCalculanDesdeElInicioSinArrastrarElFinDeMes() {
        PeriodicidadSeguimiento mensual = new PeriodicidadSeguimiento(1, UnidadPeriodicidad.MESES);

        assertEquals(LocalDate.of(2027, 2, 28), mensual.fechaRepeticion(INICIO, 1));
        assertEquals(LocalDate.of(2027, 3, 31), mensual.fechaRepeticion(INICIO, 2));
    }
}
