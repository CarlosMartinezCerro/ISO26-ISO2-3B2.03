package es.uclm.esi.iso2.radiodiagnostico.controlador;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.uclm.esi.iso2.radiodiagnostico.informe.DatosImagen;
import es.uclm.esi.iso2.radiodiagnostico.informe.ImagenRepositorioEnMemoria;
import es.uclm.esi.iso2.radiodiagnostico.informe.Imagen;
import es.uclm.esi.iso2.radiodiagnostico.informe.ImagenRepositorio;
import es.uclm.esi.iso2.radiodiagnostico.informe.ResultadoConsultaImagen;
import es.uclm.esi.iso2.radiodiagnostico.informe.ResultadoCreacionImagen;
import es.uclm.esi.iso2.radiodiagnostico.informe.ResultadoListadoImagenes;
import es.uclm.esi.iso2.radiodiagnostico.informe.ServicioImagenesImpl;
import es.uclm.esi.iso2.radiodiagnostico.informe.SistemaPacsSimulado;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tests del CdU19 · Consultar imagen sobre el controlador del CdU17. */
class ControladorImagenesConsultaTest {

    private ControladorImagenes controlador;

    @BeforeEach
    void crearControlador() {
        controlador = new ControladorImagenes(
                new ServicioImagenesImpl(new ImagenRepositorioEnMemoria(), new SistemaPacsSimulado()));
        controlador.crearImagen(new DatosImagen("INF-001", "FICTICIO-001", "MED-001",
                "TAC", "craneo.dcm", new byte[] {1}));
        controlador.crearImagen(new DatosImagen("INF-002", "FICTICIO-001", "MED-002",
                "TAC", "abdomen.dcm", new byte[] {1}));
    }

    @Test
    void listadoCorrectoIndicaCuantasHay() {
        ResultadoListadoImagenes resultado = controlador.imagenesPaciente("MED-001", "FICTICIO-001");

        assertTrue(resultado.correcto());
        assertEquals(1, resultado.imagenes().size());
        assertEquals("1 imagen(es) encontradas.", resultado.mensaje());
    }

    @Test
    void listadoVacioAvisaDeQueNoHayImagenes() {
        ResultadoListadoImagenes resultado = controlador.imagenesPaciente("MED-001", "FICTICIO-999");

        assertTrue(resultado.correcto());
        assertEquals("El paciente FICTICIO-999 no tiene imágenes disponibles.", resultado.mensaje());
    }

    @Test
    void listadoSinPacienteDevuelveError() {
        ResultadoListadoImagenes resultado = controlador.imagenesPaciente("MED-001", "");

        assertFalse(resultado.correcto());
        assertEquals("El campo paciente es obligatorio.", resultado.mensaje());
        assertTrue(resultado.imagenes().isEmpty());
    }

    @Test
    void consultaCorrectaDevuelveLaImagen() {
        ResultadoConsultaImagen resultado = controlador.consultarImagen("MED-001", "IMG-001");

        assertTrue(resultado.correcto());
        assertEquals("IMG-001", resultado.imagen().id());
    }

    @Test
    void consultaDeImagenAjenaDevuelveError() {
        ResultadoConsultaImagen resultado = controlador.consultarImagen("MED-001", "IMG-002");

        assertFalse(resultado.correcto());
        assertNull(resultado.imagen());
    }

    @Test
    void consultaDeImagenInexistenteDevuelveError() {
        assertFalse(controlador.consultarImagen("MED-001", "IMG-999").correcto());
    }

    @Test
    void consultaSinImagenDevuelveError() {
        assertFalse(controlador.consultarImagen("MED-001", " ").correcto());
    }

    @Test
    void siFallaLaBaseDeDatosElListadoDevuelveUnMensaje() {
        ResultadoListadoImagenes resultado = controladorSinBaseDeDatos().imagenesPaciente("MED-001", "FICTICIO-001");

        assertFalse(resultado.correcto());
        assertEquals(ControladorImagenes.ERROR_INTERNO, resultado.mensaje());
    }

    @Test
    void siFallaLaBaseDeDatosLaConsultaDevuelveUnMensaje() {
        ResultadoConsultaImagen resultado = controladorSinBaseDeDatos().consultarImagen("MED-001", "IMG-001");

        assertFalse(resultado.correcto());
        assertEquals(ControladorImagenes.ERROR_INTERNO, resultado.mensaje());
    }

    @Test
    void siFallaLaBaseDeDatosCrearImagenDevuelveUnMensaje() {
        ResultadoCreacionImagen resultado = controladorSinBaseDeDatos().crearImagen(new DatosImagen(
                "INF-001", "FICTICIO-001", "MED-001", "TAC", "craneo.dcm", new byte[] {1}));

        assertFalse(resultado.correcto());
        assertEquals(ControladorImagenes.ERROR_INTERNO, resultado.mensaje());
    }

    /** Repositorio que falla como lo haría ImagenRepositorioMySql sin base de datos. */
    private static ControladorImagenes controladorSinBaseDeDatos() {
        ImagenRepositorio caido = new ImagenRepositorioEnMemoria() {
            @Override
            public List<Imagen> buscarPorPaciente(String dniPaciente) {
                throw new IllegalStateException("Sin base de datos");
            }

            @Override
            public Optional<Imagen> buscarPorId(String id) {
                throw new IllegalStateException("Sin base de datos");
            }

            @Override
            public void guardar(Imagen imagen) {
                throw new IllegalStateException("Sin base de datos");
            }
        };
        return new ControladorImagenes(new ServicioImagenesImpl(caido, new SistemaPacsSimulado()));
    }
}
