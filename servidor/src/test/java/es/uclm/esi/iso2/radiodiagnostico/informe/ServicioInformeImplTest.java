package es.uclm.esi.iso2.radiodiagnostico.informe;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

    @Test
    void lanzaExcepcionPorDniPacienteInvalido() {
        HistorialClinicoSimulado historial = new HistorialClinicoSimulado();
        ServicioInforme servicio = new ServicioInformeImpl(historial);
        Informe informe = new Informe(
                "12345678A", " ", "Radiologia", "Dolor", "Consulta",
                "Diagnostico", "", "", false, LocalDate.now(), "");

        assertThrows(DniPacienteInvalidoException.class,
                () -> servicio.crearInforme(informe));
        assertThrows(DniPacienteInvalidoException.class,
                () -> servicio.listarInformes(" "));
    }

    @Test
    void lanzaExcepcionPorDatosOInformeNoEncontrado() {
        HistorialClinicoSimulado historial = new HistorialClinicoSimulado();
        ServicioInforme servicio = new ServicioInformeImpl(historial);

        assertThrows(DatosInformeInvalidosException.class,
                () -> servicio.crearInforme(null));
        assertThrows(InformeNoEncontradoException.class,
                () -> servicio.obtenerInforme(-1));
        assertThrows(InformeNoEncontradoException.class,
                () -> servicio.obtenerInforme(999999));
    }

    @Test
    void rechazaDatosObligatoriosAusentesAlCrearInforme() {
        ServicioInforme servicio = new ServicioInformeImpl(new HistorialClinicoSimulado());

        assertThrows(DatosInformeInvalidosException.class,
                () -> servicio.crearInforme(new Informe(
                        "12345678A", "87654321B", "", "Dolor", "Consulta",
                        "Diagnostico", "", "", false, LocalDate.now(), "")));
        assertThrows(DatosInformeInvalidosException.class,
                () -> servicio.crearInforme(new Informe(
                        "12345678A", "87654321B", "Radiologia", "Dolor", "",
                        "Diagnostico", "", "", false, LocalDate.now(), "")));
        assertThrows(DatosInformeInvalidosException.class,
                () -> servicio.crearInforme(new Informe(
                        "12345678A", "87654321B", "Radiologia", "Dolor", "Consulta",
                        "", "", "", false, LocalDate.now(), "")));
        assertThrows(DatosInformeInvalidosException.class,
                () -> servicio.crearInforme(new Informe(
                        "12345678A", "87654321B", "Radiologia", "Dolor", "Consulta",
                        "Diagnostico", "", "", false, null, "")));
    }

    @Test
    void rechazaInformeSinEspecialidadAlObtenerAntecedentes() {
        ServicioInforme servicio = new ServicioInformeImpl(new HistorialClinicoSimulado());
        Informe informe = new Informe(
                "12345678A", "87654321B", "", "Dolor", "Consulta",
                "Diagnostico", "", "", false, LocalDate.now(), "");

        assertThrows(DatosInformeInvalidosException.class,
                () -> servicio.obtenerAntecedentes(informe));
        assertThrows(DatosInformeInvalidosException.class,
                () -> servicio.obtenerAntecedentes(null));
    }

    @Test
    void rechazaDependenciaNula() {
        assertThrows(NullPointerException.class, () -> new ServicioInformeImpl(null));
    }
}
