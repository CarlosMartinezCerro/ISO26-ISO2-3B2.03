package es.uclm.esi.iso2.radiodiagnostico.controlador;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import es.uclm.esi.iso2.radiodiagnostico.modelo.informe.Informe;
import es.uclm.esi.iso2.radiodiagnostico.modelo.informe.ServicioInformeImpl;
import es.uclm.esi.iso2.radiodiagnostico.modelo.sistemasexternos.HistorialClinicoSimulado;
import es.uclm.esi.iso2.radiodiagnostico.modelo.usuario.Medico;
import es.uclm.esi.iso2.radiodiagnostico.modelo.usuario.Paciente;

class ControladorInformeTest {

    @Test
    void delegaCrearInformeYObtenerAntecedentes() {
        ControladorInforme controlador =
                new ControladorInforme(new ServicioInformeImpl(new HistorialClinicoSimulado()));
        Medico medico = new Medico("Ana", "Ruiz", "12345678A", "", "", LocalDate.now(), "", "Radiologia");
        Paciente paciente = new Paciente("Luis", "Perez", "87654321B", "", "", LocalDate.now(), "");
        Informe informe = new Informe(medico, paciente, "Radiologia", "Dolor", "Consulta actual",
                "Diagnostico", "Pruebas", "Tratamiento", false, LocalDate.now(), "Anamnesis");

        assertSame(informe, controlador.crearInforme(informe));
        assertEquals("Antecedentes simulados del paciente 87654321B.",
                controlador.obtenerAntecedentes(informe));
    }
}
