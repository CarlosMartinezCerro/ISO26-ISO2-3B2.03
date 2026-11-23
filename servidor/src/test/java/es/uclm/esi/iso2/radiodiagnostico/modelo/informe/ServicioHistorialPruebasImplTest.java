package es.uclm.esi.iso2.radiodiagnostico.modelo.informe;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.uclm.esi.iso2.radiodiagnostico.modelo.solicitud.EstadoSolicitud;
import es.uclm.esi.iso2.radiodiagnostico.modelo.solicitud.PrioridadSolicitud;
import es.uclm.esi.iso2.radiodiagnostico.modelo.solicitud.SolicitudPrueba;
import es.uclm.esi.iso2.radiodiagnostico.modelo.solicitud.SolicitudRepositorio;
import es.uclm.esi.iso2.radiodiagnostico.modelo.solicitud.SolicitudRepositorioEnMemoria;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ServicioHistorialPruebasImplTest {

    private ImagenRepositorio imagenes;
    private SolicitudRepositorio solicitudes;
    private ServicioHistorialPruebasImpl servicio;

    @BeforeEach
    void preparar() {
        imagenes = new ImagenRepositorioEnMemoria();
        solicitudes = new SolicitudRepositorioEnMemoria();
        servicio = new ServicioHistorialPruebasImpl(imagenes, solicitudes);
    }

    @Test
    void devuelveImagenesYSolicitudesDelPacienteOrdenadas() {
        imagenes.guardar(new Imagen("IMG-001", "INF-10", "12345678A", "MED-1",
                "TAC", "torax.dcm", new byte[] {1, 2}, LocalDateTime.of(2026, 5, 10, 10, 0), "PACS-1"));
        imagenes.guardar(new Imagen("IMG-002", "INF-11", "99999999Z", "MED-1",
                "Ecografía", "eco.png", new byte[] {1, 2}, LocalDateTime.of(2026, 5, 11, 10, 0), "PACS-2"));
        solicitudes.guardar(new SolicitudPrueba("SOL-001", "12345678A", "Resonancia magnética",
                PrioridadSolicitud.values()[0], EstadoSolicitud.PENDIENTE));

        List<PruebaHistorica> historial = servicio.consultarPruebasHistoricas("12345678A");

        assertEquals(2, historial.size());
        assertEquals("IMG-001", historial.get(0).idPrueba());
        assertEquals("REALIZADA", historial.get(0).estado());
        assertEquals("SOL-001", historial.get(1).idPrueba());
        assertEquals("PENDIENTE", historial.get(1).estado());
    }

    @Test
    void lanzaExcepcionSiDniEsVacio() {
        assertThrows(DniPacienteInvalidoException.class, () -> servicio.consultarPruebasHistoricas("   "));
    }

    @Test
    void devuelveListaVaciaSiPacienteNoTienePruebas() {
        assertTrue(servicio.consultarPruebasHistoricas("00000000T").isEmpty());
    }
}