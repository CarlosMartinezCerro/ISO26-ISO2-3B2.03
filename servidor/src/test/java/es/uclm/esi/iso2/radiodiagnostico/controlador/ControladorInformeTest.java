package es.uclm.esi.iso2.radiodiagnostico.controlador;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import es.uclm.esi.iso2.radiodiagnostico.informe.Informe;
import es.uclm.esi.iso2.radiodiagnostico.informe.ServicioInformeImpl;
import es.uclm.esi.iso2.radiodiagnostico.sistemasexternos.HistorialClinicoSimulado;
class ControladorInformeTest {

    @Test
    void delegaCrearInformeYObtenerAntecedentes() {
        ControladorInforme controlador =
                new ControladorInforme(new ServicioInformeImpl(new HistorialClinicoSimulado()));
        Informe informe = new Informe("12345678A", "87654321B", "Radiologia", "Dolor", "Consulta actual",
                "Diagnostico", "Pruebas", "Tratamiento", false, LocalDate.now(), "Anamnesis");

        Informe creado = controlador.crearInforme(informe);
        assertTrue(creado.id() > 0);
        assertEquals("Antecedentes simulados del paciente 87654321B.",
                controlador.obtenerAntecedentes(informe).antecedentes());
    }
}
