package es.uclm.esi.iso2.radiodiagnostico.informe;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import es.uclm.esi.iso2.radiodiagnostico.persistencia.ErrorBaseDatosException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

/** Sin base de datos: comprueba qué pasa si MySQL no responde (se ejecuta siempre). */
class ImagenRepositorioMySqlErroresTest {

    private static final SQLException CAIDA = new SQLException("Connection refused");

    private final ImagenRepositorioMySql repositorio = new ImagenRepositorioMySql(() -> {
        throw CAIDA;
    });

    @Test
    void guardarSinConexionLanzaErrorBaseDatos() {
        Imagen imagen = new Imagen("IMG-001", "INF-001", "FICTICIO-001", "MED-001", "TAC",
                "craneo.dcm", new byte[] {1}, LocalDateTime.of(2026, 11, 2, 9, 0), null);

        ErrorBaseDatosException error = assertThrows(ErrorBaseDatosException.class,
                () -> repositorio.guardar(imagen));

        assertEquals("No se pudo guardar la imagen IMG-001.", error.getMessage());
        assertSame(CAIDA, error.getCause());
    }

    @Test
    void buscarSinConexionLanzaErrorBaseDatos() {
        assertThrows(ErrorBaseDatosException.class, () -> repositorio.buscarPorId("IMG-001"));
        assertThrows(ErrorBaseDatosException.class, () -> repositorio.buscarPorPaciente("FICTICIO-001"));
        assertThrows(ErrorBaseDatosException.class, () -> repositorio.buscarPorInforme("INF-001"));
    }

    @Test
    void contarSinConexionLanzaErrorBaseDatos() {
        assertThrows(ErrorBaseDatosException.class, repositorio::contar);
    }
}
