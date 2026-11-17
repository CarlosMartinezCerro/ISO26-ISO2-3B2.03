package es.uclm.esi.iso2.radiodiagnostico.informe;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tests del CdU19 · Consultar imagen sobre el servicio del CdU17. */
class ServicioImagenesConsultaTest {

    private static final ZoneId ZONA = ZoneId.of("Europe/Madrid");
    private static final String MEDICO = "MED-001";
    private static final String OTRO_MEDICO = "MED-002";
    private static final String PACIENTE = "FICTICIO-001";
    private static final byte[] CONTENIDO = {1, 2, 3};

    private ImagenRepositorioEnMemoria repositorio;
    private ServicioImagenes servicio;

    @BeforeEach
    void crearImagenes() {
        repositorio = new ImagenRepositorioEnMemoria();
        // IMG-001 y IMG-002 del médico, IMG-003 de otro médico, IMG-004 de otro paciente
        crearEl(LocalDateTime.of(2026, 9, 10, 9, 0), PACIENTE, MEDICO);
        crearEl(LocalDateTime.of(2026, 11, 2, 9, 0), PACIENTE, MEDICO);
        crearEl(LocalDateTime.of(2026, 10, 5, 9, 0), PACIENTE, OTRO_MEDICO);
        crearEl(LocalDateTime.of(2026, 10, 1, 9, 0), "FICTICIO-002", MEDICO);
        servicio = new ServicioImagenesImpl(repositorio, new SistemaPacsSimulado());
    }

    @Test
    void listaSoloLasImagenesDelPacienteCreadasPorElMedico() {
        assertEquals(List.of("IMG-002", "IMG-001"), ids(servicio.listarImagenesPaciente(MEDICO, PACIENTE)));
    }

    @Test
    void listaDeLaMasRecienteALaMasAntigua() {
        List<Imagen> imagenes = servicio.listarImagenesPaciente(MEDICO, PACIENTE);

        assertTrue(imagenes.get(0).fechaCreacion().isAfter(imagenes.get(1).fechaCreacion()));
    }

    @Test
    void pacienteSinImagenesDevuelveListaVacia() {
        assertEquals(List.of(), servicio.listarImagenesPaciente(MEDICO, "FICTICIO-999"));
    }

    @Test
    void otroMedicoSoloVeLasSuyas() {
        assertEquals(List.of("IMG-003"), ids(servicio.listarImagenesPaciente(OTRO_MEDICO, PACIENTE)));
    }

    @Test
    void listarRechazaPacienteVacio() {
        assertDatoInvalido(() -> servicio.listarImagenesPaciente(MEDICO, " "),
                "El campo paciente es obligatorio.");
    }

    @Test
    void listarRechazaMedicoNulo() {
        assertDatoInvalido(() -> servicio.listarImagenesPaciente(null, PACIENTE),
                "El campo médico es obligatorio.");
    }

    @Test
    void consultaUnaImagenDelMedico() {
        Imagen imagen = servicio.consultarImagen(MEDICO, "IMG-002");

        assertEquals("IMG-002", imagen.id());
        assertEquals("pacs://radiodiagnostico/FICTICIO-001/IMG-002", imagen.referenciaPacs());
    }

    @Test
    void consultarRechazaImagenInexistente() {
        ImagenNoEncontradaException error = assertThrows(ImagenNoEncontradaException.class,
                () -> servicio.consultarImagen(MEDICO, "IMG-999"));

        assertEquals("No existe la imagen IMG-999.", error.getMessage());
    }

    @Test
    void consultarRechazaImagenDeOtroMedico() {
        assertThrows(AccesoDenegadoException.class, () -> servicio.consultarImagen(MEDICO, "IMG-003"));
    }

    @Test
    void consultarRechazaImagenVacia() {
        assertDatoInvalido(() -> servicio.consultarImagen(MEDICO, ""), "El campo imagen es obligatorio.");
    }

    @Test
    void consultarRechazaMedicoVacio() {
        assertDatoInvalido(() -> servicio.consultarImagen(" ", "IMG-001"), "El campo médico es obligatorio.");
    }

    @Test
    void elRepositorioBuscaPorPaciente() {
        assertEquals(3, repositorio.buscarPorPaciente(PACIENTE).size());
    }

    /** Usa el CdU17 para crear la imagen con la fecha indicada. */
    private void crearEl(LocalDateTime fecha, String dni, String medico) {
        Clock reloj = Clock.fixed(fecha.atZone(ZONA).toInstant(), ZONA);
        new ServicioImagenesImpl(repositorio, new SistemaPacsSimulado(), reloj)
                .crearImagen(new DatosImagen("INF-001", dni, medico, "Radiografía", "torax.dcm", CONTENIDO));
    }

    private void assertDatoInvalido(Runnable accion, String mensaje) {
        DatosImagenInvalidosException error = assertThrows(DatosImagenInvalidosException.class, accion::run);
        assertEquals(mensaje, error.getMessage());
    }

    private static List<String> ids(List<Imagen> imagenes) {
        return imagenes.stream().map(Imagen::id).toList();
    }
}
