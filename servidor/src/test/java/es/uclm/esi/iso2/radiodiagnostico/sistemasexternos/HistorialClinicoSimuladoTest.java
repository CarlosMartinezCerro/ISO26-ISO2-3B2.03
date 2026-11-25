package es.uclm.esi.iso2.radiodiagnostico.sistemasexternos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.uclm.esi.iso2.radiodiagnostico.informe.DatosInformeInvalidosException;
import es.uclm.esi.iso2.radiodiagnostico.informe.DniPacienteInvalidoException;
import es.uclm.esi.iso2.radiodiagnostico.informe.Informe;
import es.uclm.esi.iso2.radiodiagnostico.informe.InformeNoEncontradoException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class HistorialClinicoSimuladoTest {

    private static Informe informe(String dniPaciente, String especialidad) {
        return new Informe("99999999Z", dniPaciente, especialidad, "Dolor",
                "Consulta", "Diagnostico", "Pruebas", "Tratamiento",
                false, LocalDate.of(2026, 11, 24), "Anamnesis");
    }

    @Test
    void obtieneAntecedentesSimuladosYListaPorPaciente() {
        HistorialClinico historial = new HistorialClinicoSimulado();
        Informe informe = informe("87654321B", "Radiologia");

        Informe actualizado = historial.getAntecedentes(informe);

        assertEquals("Antecedentes simulados del paciente 87654321B.",
                actualizado.antecedentes());
        assertEquals("Historial simulado de Radiologia para el paciente 87654321B.",
                actualizado.historialEspecialidad());
        assertFalse(historial.getInformes("87654321B").isEmpty());
        assertTrue(historial.getInformes("00000000T").isEmpty());
    }

    @Test
    void validaDatosDeConsultaYArchivado() {
        HistorialClinicoSimulado historial = new HistorialClinicoSimulado();

        assertThrows(DatosInformeInvalidosException.class,
                () -> historial.getAntecedentes(null));
        assertThrows(DniPacienteInvalidoException.class,
                () -> historial.getAntecedentes(informe(" ", "Radiologia")));
        assertThrows(DatosInformeInvalidosException.class,
                () -> historial.getAntecedentes(informe("87654321B", " ")));
        assertThrows(DniPacienteInvalidoException.class,
                () -> historial.getInformes(" "));
        assertThrows(DatosInformeInvalidosException.class,
                () -> historial.archivar(null));
    }

    @Test
    void rechazaInformeSinDniMedicoAlArchivar() {
        HistorialClinicoSimulado historial = new HistorialClinicoSimulado();
        Informe sinMedico = new Informe("", "87654321B", "Radiologia", "Dolor",
                "Consulta", "Diagnostico", "", "", false,
                LocalDate.of(2026, 11, 24), "");

        assertThrows(DatosInformeInvalidosException.class,
                () -> historial.archivar(sinMedico));
    }

    @Test
    void obtieneInformePorIdYRechazaIdsNoDisponibles() {
        HistorialClinicoSimulado historial = new HistorialClinicoSimulado();

        assertEquals(1, historial.getInforme(1).id());
        assertThrows(InformeNoEncontradoException.class,
                () -> historial.getInforme(0));
        assertThrows(InformeNoEncontradoException.class,
                () -> historial.getInforme(999999));
    }

    @Test
    void archivaInformeNuevoYReemplazaElMismoId() throws Exception {
        Path fichero = fichero();
        byte[] original = Files.readAllBytes(fichero);
        try {
            HistorialClinicoSimulado historial = new HistorialClinicoSimulado();
            Informe nuevo = historial.archivar(informe("00000000T", "Cardiologia"));
            Informe reemplazo = historial.archivar(nuevo.conAntecedentes("A", "B"));

            assertTrue(nuevo.id() > 0);
            assertEquals(nuevo.id(), reemplazo.id());
            assertEquals("A", historial.getInforme(nuevo.id()).antecedentes());
            assertEquals(1, historial.getInformes("00000000T").size());
        } finally {
            Files.write(fichero, original);
        }
    }

    private static Path fichero() {
        Path desdeModulo = Paths.get("src", "main", "resources", "HistorialClinicoBBDD.csv");
        return Files.exists(desdeModulo)
                ? desdeModulo
                : Paths.get("servidor", "src", "main", "resources",
                        "HistorialClinicoBBDD.csv");
    }
}
