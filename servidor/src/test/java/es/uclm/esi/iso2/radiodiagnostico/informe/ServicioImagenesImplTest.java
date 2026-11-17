package es.uclm.esi.iso2.radiodiagnostico.informe;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ServicioImagenesImplTest {

    private static final ZoneId ZONA = ZoneId.of("Europe/Madrid");
    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 11, 17, 10, 0);
    private static final byte[] CONTENIDO = {1, 2, 3};

    private ImagenRepositorioEnMemoria imagenes;
    private ServicioImagenesImpl servicio;

    @BeforeEach
    void crearServicio() {
        imagenes = new ImagenRepositorioEnMemoria();
        Clock reloj = Clock.fixed(AHORA.atZone(ZONA).toInstant(), ZONA);
        SistemaPacs pacsFalso = imagen -> "pacs-test/" + imagen.id();
        servicio = new ServicioImagenesImpl(imagenes, pacsFalso, reloj);
    }

    private static DatosImagen datos(String idInforme, String fichero, byte[] contenido) {
        return new DatosImagen(idInforme, "FICTICIO-001", "MED-001", "Radiografía", fichero, contenido);
    }

    @Test
    void creaLaImagenYLaGuardaArchivadaEnPacs() {
        Imagen imagen = servicio.crearImagen(datos("INF-001", "torax.dcm", CONTENIDO));

        assertEquals("IMG-001", imagen.id());
        assertEquals(AHORA, imagen.fechaCreacion());
        assertEquals("pacs-test/IMG-001", imagen.referenciaPacs());
        assertEquals(imagen, imagenes.buscarPorId("IMG-001").orElseThrow());
    }

    @Test
    void asignaIdentificadoresConsecutivos() {
        servicio.crearImagen(datos("INF-001", "torax.dcm", CONTENIDO));

        Imagen segunda = servicio.crearImagen(datos("INF-001", "torax2.png", CONTENIDO));

        assertEquals("IMG-002", segunda.id());
        assertEquals(2, servicio.listarImagenesDeInforme("INF-001").size());
    }

    @Test
    void sinInformeLanzaDatosInvalidos() {
        DatosImagenInvalidosException error = assertThrows(DatosImagenInvalidosException.class,
                () -> servicio.crearImagen(datos(" ", "torax.dcm", CONTENIDO)));

        assertEquals("El campo informe es obligatorio.", error.getMessage());
    }

    @Test
    void ficheroVacioLanzaDatosInvalidos() {
        assertThrows(DatosImagenInvalidosException.class,
                () -> servicio.crearImagen(datos("INF-001", "torax.dcm", new byte[0])));
    }

    @Test
    void formatoNoPermitidoLanzaDatosInvalidos() {
        assertThrows(DatosImagenInvalidosException.class,
                () -> servicio.crearImagen(datos("INF-001", "informe.pdf", CONTENIDO)));
    }

    @Test
    void sinDatosLanzaDatosInvalidos() {
        assertThrows(DatosImagenInvalidosException.class, () -> servicio.crearImagen(null));
    }

    @Test
    void siElPacsFallaNoSeGuardaNada() {
        ServicioImagenesImpl conPacsCaido = new ServicioImagenesImpl(imagenes, imagen -> {
            throw new PacsNoDisponibleException("El PACS no responde.");
        });

        assertThrows(PacsNoDisponibleException.class,
                () -> conPacsCaido.crearImagen(datos("INF-001", "torax.dcm", CONTENIDO)));
        assertTrue(imagenes.buscarPorInforme("INF-001").isEmpty());
    }
}
