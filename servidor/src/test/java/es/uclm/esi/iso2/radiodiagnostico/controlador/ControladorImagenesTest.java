package es.uclm.esi.iso2.radiodiagnostico.controlador;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.uclm.esi.iso2.radiodiagnostico.modelo.informe.DatosImagen;
import es.uclm.esi.iso2.radiodiagnostico.modelo.informe.ImagenRepositorioEnMemoria;
import es.uclm.esi.iso2.radiodiagnostico.modelo.informe.PacsNoDisponibleException;
import es.uclm.esi.iso2.radiodiagnostico.modelo.informe.ResultadoCreacionImagen;
import es.uclm.esi.iso2.radiodiagnostico.modelo.informe.ServicioImagenesImpl;
import es.uclm.esi.iso2.radiodiagnostico.modelo.informe.SistemaPacsSimulado;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ControladorImagenesTest {

    private static final DatosImagen DATOS = new DatosImagen("INF-001", "FICTICIO-001",
            "MED-001", "Radiografía", "torax.dcm", new byte[] {1, 2, 3});

    private ControladorImagenes controlador;

    @BeforeEach
    void crearControlador() {
        controlador = new ControladorImagenes(
                new ServicioImagenesImpl(new ImagenRepositorioEnMemoria(), new SistemaPacsSimulado()));
    }

    @Test
    void creacionCorrectaDevuelveResultadoCorrecto() {
        ResultadoCreacionImagen resultado = controlador.crearImagen(DATOS);

        assertTrue(resultado.correcto());
        assertTrue(resultado.mensaje().contains("IMG-001"));
    }

    @Test
    void datosInvalidosDevuelvenErrorConMensaje() {
        ResultadoCreacionImagen resultado = controlador.crearImagen(new DatosImagen(
                "INF-001", "FICTICIO-001", "MED-001", "", "torax.dcm", new byte[] {1}));

        assertEquals(new ResultadoCreacionImagen(false, "El campo tipo de prueba es obligatorio."),
                resultado);
    }

    @Test
    void pacsCaidoDevuelveErrorConMensaje() {
        ControladorImagenes conPacsCaido = new ControladorImagenes(new ServicioImagenesImpl(
                new ImagenRepositorioEnMemoria(), imagen -> {
                    throw new PacsNoDisponibleException("El PACS no responde.");
                }));

        ResultadoCreacionImagen resultado = conPacsCaido.crearImagen(DATOS);

        assertFalse(resultado.correcto());
        assertEquals("El PACS no responde.", resultado.mensaje());
    }
}
