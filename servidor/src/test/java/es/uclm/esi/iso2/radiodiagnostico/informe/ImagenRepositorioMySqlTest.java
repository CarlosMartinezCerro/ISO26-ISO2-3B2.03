package es.uclm.esi.iso2.radiodiagnostico.informe;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import es.uclm.esi.iso2.radiodiagnostico.persistencia.ConexionMySql;
import es.uclm.esi.iso2.radiodiagnostico.persistencia.ErrorBaseDatosException;
import es.uclm.esi.iso2.radiodiagnostico.persistencia.FuenteConexiones;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Pruebas de integración con MySQL (CdU19).
 * Solo se ejecutan si están definidas MYSQL_URL, MYSQL_USER y MYSQL_PASSWORD;
 * si no, JUnit las marca como omitidas y mvn verify sigue en verde.
 * ¡Cuidado! Vacían la tabla imagen: usad una base de datos de pruebas.
 */
class ImagenRepositorioMySqlTest {

    private static final ZoneId ZONA = ZoneId.of("Europe/Madrid");
    private static final LocalDateTime FECHA = LocalDateTime.of(2026, 11, 2, 9, 30, 15);
    private static final byte[] CONTENIDO = {(byte) 0x89, 'P', 'N', 'G', 0, 1, 2, 3};

    private FuenteConexiones conexiones;
    private ImagenRepositorioMySql repositorio;

    @BeforeEach
    void prepararBaseDeDatos() throws SQLException, IOException {
        assumeTrue(System.getenv("MYSQL_URL") != null,
                "Sin MYSQL_URL no se ejecutan las pruebas de MySQL");
        conexiones = ConexionMySql.desdeEntorno();
        try (Connection conexion = conexiones.abrir(); Statement sentencia = conexion.createStatement()) {
            sentencia.execute(leerEsquema());
            sentencia.execute("DELETE FROM imagen");
        }
        repositorio = new ImagenRepositorioMySql(conexiones);
    }

    @Test
    void guardaYRecuperaTodosLosCamposDeLaImagen() {
        repositorio.guardar(imagen("IMG-001", "INF-001", "FICTICIO-001", "MED-001", FECHA));

        Imagen leida = repositorio.buscarPorId("IMG-001").orElseThrow();

        assertEquals("INF-001", leida.idInforme());
        assertEquals("FICTICIO-001", leida.dniPaciente());
        assertEquals("MED-001", leida.idMedico());
        assertEquals("Radiografía", leida.tipoPrueba());
        assertEquals("torax.png", leida.nombreFichero());
        assertArrayEquals(CONTENIDO, leida.contenido());
        assertEquals(FECHA, leida.fechaCreacion());
        assertEquals("pacs://radiodiagnostico/FICTICIO-001/IMG-001", leida.referenciaPacs());
    }

    @Test
    void buscarPorIdInexistenteDevuelveVacio() {
        assertTrue(repositorio.buscarPorId("IMG-999").isEmpty());
    }

    @Test
    void buscaPorPacienteYPorInforme() {
        repositorio.guardar(imagen("IMG-001", "INF-001", "FICTICIO-001", "MED-001", FECHA));
        repositorio.guardar(imagen("IMG-002", "INF-002", "FICTICIO-001", "MED-001", FECHA));
        repositorio.guardar(imagen("IMG-003", "INF-002", "FICTICIO-002", "MED-001", FECHA));

        assertEquals(List.of("IMG-001", "IMG-002"), ids(repositorio.buscarPorPaciente("FICTICIO-001")));
        assertEquals(List.of("IMG-002", "IMG-003"), ids(repositorio.buscarPorInforme("INF-002")));
        assertEquals(3, repositorio.contar());
    }

    @Test
    void guardarDosVecesElMismoIdLanzaErrorDeBaseDeDatos() {
        repositorio.guardar(imagen("IMG-001", "INF-001", "FICTICIO-001", "MED-001", FECHA));

        assertThrows(ErrorBaseDatosException.class,
                () -> repositorio.guardar(imagen("IMG-001", "INF-001", "FICTICIO-001", "MED-001", FECHA)));
    }

    @Test
    void elCdU17GuardaEnMySqlYElCdU19LoConsulta() {
        Clock reloj = Clock.fixed(FECHA.atZone(ZONA).toInstant(), ZONA);
        ServicioImagenes servicio = new ServicioImagenesImpl(repositorio, new SistemaPacsSimulado(), reloj);
        servicio.crearImagen(new DatosImagen("INF-001", "FICTICIO-001", "MED-001",
                "TAC", "craneo.dcm", CONTENIDO));

        List<Imagen> imagenes = servicio.listarImagenesPaciente("MED-001", "FICTICIO-001");

        assertEquals(List.of("IMG-001"), ids(imagenes));
        assertEquals("IMG-001", servicio.consultarImagen("MED-001", "IMG-001").id());
    }

    private static String leerEsquema() throws IOException {
        try (InputStream esquema = ImagenRepositorioMySqlTest.class.getResourceAsStream("/schema-imagenes.sql")) {
            String sql = new String(esquema.readAllBytes(), StandardCharsets.UTF_8);
            return sql.replaceAll("(?m)^--.*$", "").trim().replaceAll(";$", "");
        }
    }

    private static Imagen imagen(String id, String informe, String dni, String medico, LocalDateTime fecha) {
        return new Imagen(id, informe, dni, medico, "Radiografía", "torax.png", CONTENIDO, fecha,
                "pacs://radiodiagnostico/" + dni + "/" + id);
    }

    private static List<String> ids(List<Imagen> imagenes) {
        return imagenes.stream().map(Imagen::id).toList();
    }
}
