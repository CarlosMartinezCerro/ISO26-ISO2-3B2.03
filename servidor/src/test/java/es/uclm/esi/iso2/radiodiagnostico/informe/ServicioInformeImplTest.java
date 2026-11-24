package es.uclm.esi.iso2.radiodiagnostico.informe;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

import es.uclm.esi.iso2.radiodiagnostico.sistemasexternos.HistorialClinicoSimulado;
class ServicioInformeImplTest {

    private static Informe crearInforme() {
        return new Informe("12345678A", "87654321B", "Radiologia", "Dolor", "Consulta actual",
                "Diagnostico", "Pruebas", "Tratamiento", false, LocalDate.now(), "Anamnesis");
    }

    @Test
    void creaInformeArchivandoloEnElHistorial() {
        HistorialClinicoSimulado historial = new HistorialClinicoSimulado();
        ServicioInforme servicio = new ServicioInformeImpl(historial);
        Informe informe = crearInforme();
        int numInformes = historial.getInformes(informe.dniPaciente()).size();
        Informe creado = servicio.crearInforme(informe);
        assertEquals(numInformes + 1,
                historial.getInformes(informe.dniPaciente()).size());
        assertTrue(creado.id() > 0);
    }

    @Test
    void obtieneYAnadeLosAntecedentesAlInforme() {
        HistorialClinicoSimulado historial = new HistorialClinicoSimulado();
        ServicioInforme servicio = new ServicioInformeImpl(historial);
        Informe informe = crearInforme();

        Informe actualizado = servicio.obtenerAntecedentes(informe);

        assertEquals("Antecedentes simulados del paciente 87654321B.",
                actualizado.antecedentes());
        assertEquals("Historial simulado de Radiologia para el paciente 87654321B.",
                actualizado.historialEspecialidad());
    }

    @Test
    void devuelveResumenConFechaYRecuperaInformePorId() {
        HistorialClinicoSimulado historial = new HistorialClinicoSimulado();
        ServicioInforme servicio = new ServicioInformeImpl(historial);
        Informe creado = servicio.crearInforme(crearInforme());

        var resumenes = servicio.listarInformes("87654321B");
        Informe recuperado = servicio.obtenerInforme(creado.id());

        assertTrue(resumenes.stream().anyMatch(resumen ->
                resumen.id() == creado.id()
                        && "Radiologia".equals(resumen.especialidad())
                        && creado.fecha().equals(resumen.fecha())));
        assertNotNull(recuperado);
        assertEquals("87654321B", recuperado.dniPaciente());
        assertEquals("12345678A", recuperado.dniMedico());
    }

    @Test
    void informeEsInmutable() {
        for (Method metodo : Informe.class.getMethods()) {
            assertTrue(!metodo.getName().startsWith("set"),
                    "Informe no debe tener setters");
        }
    }
}
