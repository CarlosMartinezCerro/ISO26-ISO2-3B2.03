package es.uclm.esi.iso2.radiodiagnostico.modelo.informe;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import es.uclm.esi.iso2.radiodiagnostico.modelo.sistemasexternos.HistorialClinicoSimulado;
import es.uclm.esi.iso2.radiodiagnostico.modelo.usuario.Medico;
import es.uclm.esi.iso2.radiodiagnostico.modelo.usuario.Paciente;

class ServicioInformeImplTest {

    private static Informe crearInforme() {
        Medico medico = new Medico("Ana", "Ruiz", "12345678A", "", "", LocalDate.now(), "", "Radiologia");
        Paciente paciente = new Paciente("Luis", "Perez", "87654321B", "", "", LocalDate.now(), "");
        return new Informe(medico, paciente, "Radiologia", "Dolor", "Consulta actual",
                "Diagnostico", "Pruebas", "Tratamiento", false, LocalDate.now(), "Anamnesis");
    }

    @Test
    void creaInformeArchivandoloEnElHistorial() {
        HistorialClinicoSimulado historial = new HistorialClinicoSimulado();
        ServicioInforme servicio = new ServicioInformeImpl(historial);
        Informe informe = crearInforme();

        assertSame(informe, servicio.crearInforme(informe));
        assertEquals(1, historial.obtenerInformesArchivados().size());
        assertTrue(historial.obtenerInformesArchivados().contains(informe));
    }

    @Test
    void obtieneYAnadeLosAntecedentesAlInforme() {
        HistorialClinicoSimulado historial = new HistorialClinicoSimulado();
        ServicioInforme servicio = new ServicioInformeImpl(historial);
        Informe informe = crearInforme();

        String antecedentes = servicio.obtenerAntecedentes(informe);

        assertEquals("Antecedentes simulados del paciente 87654321B.", antecedentes);
        assertEquals(antecedentes, informe.obtenerAntecedentes());
        assertEquals("Historial simulado de Radiologia para el paciente 87654321B.",
                informe.obtenerHistorial());
    }
}
