package es.uclm.esi.iso2.radiodiagnostico.controlador;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import es.uclm.esi.iso2.radiodiagnostico.informe.Informe;
import es.uclm.esi.iso2.radiodiagnostico.informe.InformeResumen;
import es.uclm.esi.iso2.radiodiagnostico.informe.ServicioInforme;
import es.uclm.esi.iso2.radiodiagnostico.informe.ServicioInformeImpl;
import es.uclm.esi.iso2.radiodiagnostico.sistemasexternos.HistorialClinicoSimulado;
import org.mockito.Mockito;
class ControladorInformeTest {

    @Test
    void delegaCrearInformeYObtenerAntecedentes() {
        ControladorInforme controlador =
                new ControladorInforme(new ServicioInformeImpl(new HistorialClinicoSimulado()));
        Informe informe = new Informe("12345678A", "87654321B", "Radiologia", "Dolor", "Consulta actual",
                "Diagnostico", "Pruebas", "Tratamiento", false, LocalDate.now(), "Anamnesis");

        Informe creado = controlador.crearInforme(informe);
        assertEquals("Antecedentes simulados del paciente 87654321B.",
                controlador.obtenerAntecedentes(informe).antecedentes());
        assertTrue(creado.id() > 0);
    }

    @Test
    void delegaTodasLasOperacionesEnElServicio() {
        ServicioInforme servicio = Mockito.mock(ServicioInforme.class);
        ControladorInforme controlador = new ControladorInforme(servicio);
        Informe informe = new Informe("12345678A", "87654321B", "Radiologia", "Dolor", "Consulta actual",
                "Diagnostico", "Pruebas", "Tratamiento", false, LocalDate.now(), "Anamnesis");
        Informe creado = informe.conId(7);
        Informe antecedentes = informe.conAntecedentes("Antecedentes", "Historial");
        List<InformeResumen> resumenes = List.of(
                new InformeResumen(7, "Radiologia", informe.fecha()));
        when(servicio.crearInforme(informe)).thenReturn(creado);
        when(servicio.obtenerAntecedentes(informe)).thenReturn(antecedentes);
        when(servicio.listarInformes("87654321B")).thenReturn(resumenes);
        when(servicio.obtenerInforme(7)).thenReturn(creado);

        assertEquals(creado, controlador.crearInforme(informe));
        assertEquals(antecedentes, controlador.obtenerAntecedentes(informe));
        assertEquals(resumenes, controlador.listarInformes("87654321B"));
        assertEquals(creado, controlador.obtenerInforme(7));
        verify(servicio).crearInforme(informe);
        verify(servicio).obtenerAntecedentes(informe);
        verify(servicio).listarInformes("87654321B");
        verify(servicio).obtenerInforme(7);
    }

    @Test
    void rechazaServicioNulo() {
        assertThrows(NullPointerException.class, () -> new ControladorInforme(null));
    }

    @Test
    void propagaValoresNulosDevueltosPorElServicio() {
        ServicioInforme servicio = Mockito.mock(ServicioInforme.class);
        ControladorInforme controlador = new ControladorInforme(servicio);
        when(servicio.crearInforme(null)).thenReturn(null);

        assertNull(controlador.crearInforme(null));
    }
}
