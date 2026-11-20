package es.uclm.esi.iso2.radiodiagnostico.informe;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Pruebas unitarias del repositorio MySQL de imágenes sin base de datos (igual que las del CdU22):
 * un JDBC falso (hecho con proxies) registra las sentencias y devuelve las filas que se le indican.
 */
class ImagenRepositorioMySqlTest {

    private static final LocalDateTime FECHA = LocalDateTime.of(2026, 11, 2, 9, 30);
    private static final byte[] CONTENIDO = {1, 2, 3};
    private static final Imagen IMAGEN = new Imagen("IMG-001", "INF-001", "FICTICIO-001", "MED-001",
            "TAC", "craneo.dcm", CONTENIDO, FECHA, "pacs://radiodiagnostico/FICTICIO-001/IMG-001");

    private JdbcFalso jdbc;
    private ImagenRepositorioMySql repositorio;

    @BeforeEach
    void crearRepositorio() {
        jdbc = new JdbcFalso();
        repositorio = new ImagenRepositorioMySql(jdbc.dataSource());
    }

    @Test
    void guardarInsertaTodosLosCampos() {
        repositorio.guardar(IMAGEN);

        assertTrue(jdbc.sql.startsWith("INSERT INTO imagen"));
        assertEquals("IMG-001", jdbc.parametros.get(1));
        assertEquals("MED-001", jdbc.parametros.get(4));
        assertArrayEquals(CONTENIDO, (byte[]) jdbc.parametros.get(7));
        assertEquals(Timestamp.valueOf(FECHA), jdbc.parametros.get(8));
        assertEquals("pacs://radiodiagnostico/FICTICIO-001/IMG-001", jdbc.parametros.get(9));
        assertTrue(jdbc.ejecutado);
        assertTrue(jdbc.conexionCerrada, "La conexión debe cerrarse");
    }

    @Test
    void errorAlGuardarLanzaIllegalState() {
        SQLException causa = new SQLException("Conexión perdida", "08S01", 0);
        jdbc.error = causa;

        IllegalStateException excepcion = assertThrows(IllegalStateException.class,
                () -> repositorio.guardar(IMAGEN));

        assertSame(causa, excepcion.getCause());
    }

    @Test
    void guardarNuloLanzaExcepcion() {
        assertThrows(NullPointerException.class, () -> repositorio.guardar(null));
    }

    @Test
    void buscarPorIdDevuelveLaImagenDeLaFila() {
        jdbc.filas.add(fila("IMG-001", "FICTICIO-001"));

        Imagen leida = repositorio.buscarPorId("IMG-001").orElseThrow();

        assertTrue(jdbc.sql.contains("WHERE id = ?"));
        assertEquals(Map.of(1, "IMG-001"), jdbc.parametros);
        assertEquals("FICTICIO-001", leida.dniPaciente());
        assertEquals("MED-001", leida.idMedico());
        assertArrayEquals(CONTENIDO, leida.contenido());
        assertEquals(FECHA, leida.fechaCreacion());
        assertEquals("pacs://radiodiagnostico/FICTICIO-001/IMG-001", leida.referenciaPacs());
    }

    @Test
    void buscarPorIdSinFilasDevuelveVacio() {
        assertTrue(repositorio.buscarPorId("IMG-999").isEmpty());
    }

    @Test
    void buscarPorPacienteDevuelveTodasLasFilas() {
        jdbc.filas.add(fila("IMG-001", "FICTICIO-001"));
        jdbc.filas.add(fila("IMG-002", "FICTICIO-001"));

        List<Imagen> imagenes = repositorio.buscarPorPaciente("FICTICIO-001");

        assertTrue(jdbc.sql.contains("WHERE dni_paciente = ?"));
        assertEquals(List.of("IMG-001", "IMG-002"), imagenes.stream().map(Imagen::id).toList());
    }

    @Test
    void buscarPorInformeFiltraPorInforme() {
        jdbc.filas.add(fila("IMG-001", "FICTICIO-001"));

        assertEquals(1, repositorio.buscarPorInforme("INF-001").size());
        assertTrue(jdbc.sql.contains("WHERE id_informe = ?"));
    }

    @Test
    void errorAlBuscarLanzaIllegalState() {
        jdbc.error = new SQLException("Tabla inexistente", "42S02", 1146);

        assertThrows(IllegalStateException.class, () -> repositorio.buscarPorPaciente("FICTICIO-001"));
    }

    @Test
    void contarDevuelveElNumeroDeImagenes() {
        jdbc.filas.add(Map.of(1, 7));

        assertEquals(7, repositorio.contar());
        assertTrue(jdbc.sql.contains("COUNT(*)"));
    }

    @Test
    void contarSinFilasDevuelveCero() {
        assertEquals(0, repositorio.contar());
    }

    @Test
    void errorAlContarLanzaIllegalState() {
        jdbc.error = new SQLException("Conexión perdida");

        assertThrows(IllegalStateException.class, repositorio::contar);
    }

    @Test
    void dataSourceNuloLanzaExcepcion() {
        assertThrows(NullPointerException.class, () -> new ImagenRepositorioMySql(null));
    }

    private static Map<Object, Object> fila(String id, String dni) {
        Map<Object, Object> fila = new HashMap<>();
        fila.put("id", id);
        fila.put("id_informe", "INF-001");
        fila.put("dni_paciente", dni);
        fila.put("id_medico", "MED-001");
        fila.put("tipo_prueba", "TAC");
        fila.put("nombre_fichero", "craneo.dcm");
        fila.put("contenido", CONTENIDO);
        fila.put("fecha_creacion", Timestamp.valueOf(FECHA));
        fila.put("referencia_pacs", "pacs://radiodiagnostico/" + dni + "/" + id);
        return fila;
    }

    /** JDBC mínimo: solo implementa los métodos que usa el repositorio. */
    private static final class JdbcFalso {
        final Map<Integer, Object> parametros = new HashMap<>();
        final List<Map<Object, Object>> filas = new ArrayList<>();
        String sql;
        boolean ejecutado;
        boolean conexionCerrada;
        SQLException error;

        DataSource dataSource() {
            return proxy(DataSource.class, (metodo, args) -> switch (metodo) {
                case "getConnection" -> conexion();
                default -> throw new UnsupportedOperationException(metodo);
            });
        }

        private Connection conexion() {
            return proxy(Connection.class, (metodo, args) -> switch (metodo) {
                case "prepareStatement" -> {
                    sql = (String) args[0];
                    yield sentencia();
                }
                case "close" -> {
                    conexionCerrada = true;
                    yield null;
                }
                default -> throw new UnsupportedOperationException(metodo);
            });
        }

        private PreparedStatement sentencia() {
            return proxy(PreparedStatement.class, (metodo, args) -> switch (metodo) {
                case "setString", "setBytes", "setTimestamp" -> {
                    parametros.put((Integer) args[0], args[1]);
                    yield null;
                }
                case "executeUpdate" -> {
                    lanzarErrorSiHay();
                    ejecutado = true;
                    yield 1;
                }
                case "executeQuery" -> {
                    lanzarErrorSiHay();
                    yield resultado();
                }
                case "close" -> null;
                default -> throw new UnsupportedOperationException(metodo);
            });
        }

        private ResultSet resultado() {
            int[] actual = {-1};
            return proxy(ResultSet.class, (metodo, args) -> switch (metodo) {
                case "next" -> ++actual[0] < filas.size();
                case "getString", "getBytes", "getTimestamp", "getInt" -> filas.get(actual[0]).get(args[0]);
                case "close" -> null;
                default -> throw new UnsupportedOperationException(metodo);
            });
        }

        private void lanzarErrorSiHay() throws SQLException {
            if (error != null) {
                throw error;
            }
        }

        @FunctionalInterface
        private interface Respuesta {
            Object responder(String metodo, Object[] args) throws Exception;
        }

        private static <T> T proxy(Class<T> tipo, Respuesta respuesta) {
            return tipo.cast(Proxy.newProxyInstance(tipo.getClassLoader(), new Class<?>[] {tipo},
                    (objeto, metodo, args) -> respuesta.responder(metodo.getName(), args)));
        }
    }
}
