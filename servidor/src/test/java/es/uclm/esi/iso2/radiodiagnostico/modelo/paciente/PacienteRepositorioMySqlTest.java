package es.uclm.esi.iso2.radiodiagnostico.modelo.paciente;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Pruebas unitarias del repositorio MySQL sin base de datos: un JDBC falso
 * (hecho con proxies) registra las sentencias y devuelve las filas que se le indican.
 */
class PacienteRepositorioMySqlTest {

    private static final Paciente PACIENTE = new Paciente("12345678Z", "Alex", "Ejemplo",
            LocalDate.of(1990, 5, 12), "600000001");

    private JdbcFalso jdbc;
    private PacienteRepositorioMySql repositorio;

    @BeforeEach
    void crearRepositorio() {
        jdbc = new JdbcFalso();
        repositorio = new PacienteRepositorioMySql(jdbc.dataSource());
    }

    @Test
    void guardarInsertaTodosLosCampos() {
        repositorio.guardar(PACIENTE);

        assertTrue(jdbc.sql.contains("INSERT INTO paciente"));
        assertEquals(Map.of(1, "12345678Z", 2, "Alex", 3, "Ejemplo",
                4, Date.valueOf(LocalDate.of(1990, 5, 12)), 5, "600000001"), jdbc.parametros);
        assertTrue(jdbc.ejecutado);
        assertTrue(jdbc.conexionCerrada, "La conexión debe cerrarse");
    }

    @Test
    void guardarConDniDuplicadoEnLaBaseDeDatosLanzaPacienteDuplicado() {
        jdbc.error = new SQLException("Duplicate entry", "23000", 1062);

        PacienteDuplicadoException excepcion = assertThrows(PacienteDuplicadoException.class,
                () -> repositorio.guardar(PACIENTE));

        assertEquals("Ya existe un paciente con el DNI 12345678Z.", excepcion.getMessage());
    }

    @Test
    void otroErrorAlGuardarLanzaIllegalState() {
        SQLException causa = new SQLException("Conexión perdida", "08S01", 0);
        jdbc.error = causa;

        IllegalStateException excepcion = assertThrows(IllegalStateException.class,
                () -> repositorio.guardar(PACIENTE));

        assertSame(causa, excepcion.getCause());
    }

    @Test
    void guardarNuloLanzaExcepcion() {
        assertThrows(NullPointerException.class, () -> repositorio.guardar(null));
    }

    @Test
    void buscarPorDniDevuelveElPacienteDeLaFila() {
        jdbc.fila = Map.of("dni", "12345678Z", "nombre", "Alex", "apellidos", "Ejemplo",
                "fecha_nacimiento", Date.valueOf(LocalDate.of(1990, 5, 12)),
                "telefono", "600000001");

        Optional<Paciente> encontrado = repositorio.buscarPorDni("12345678Z");

        assertEquals(Optional.of(PACIENTE), encontrado);
        assertTrue(jdbc.sql.contains("WHERE dni = ?"));
        assertEquals(Map.of(1, "12345678Z"), jdbc.parametros);
    }

    @Test
    void buscarPorDniSinFilasDevuelveVacio() {
        jdbc.fila = null;

        assertEquals(Optional.empty(), repositorio.buscarPorDni("12345678Z"));
    }

    @Test
    void errorAlBuscarLanzaIllegalState() {
        jdbc.error = new SQLException("Tabla inexistente", "42S02", 1146);

        assertThrows(IllegalStateException.class, () -> repositorio.buscarPorDni("12345678Z"));
    }

    @Test
    void dataSourceNuloLanzaExcepcion() {
        assertThrows(NullPointerException.class, () -> new PacienteRepositorioMySql(null));
    }

    /** JDBC mínimo: solo implementa los métodos que usa el repositorio. */
    private static final class JdbcFalso {
        final Map<Integer, Object> parametros = new HashMap<>();
        String sql;
        boolean ejecutado;
        boolean conexionCerrada;
        SQLException error;
        Map<String, Object> fila;

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
                case "setString", "setDate" -> {
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
            boolean[] leida = {false};
            return proxy(ResultSet.class, (metodo, args) -> switch (metodo) {
                case "next" -> {
                    boolean hayFila = fila != null && !leida[0];
                    leida[0] = true;
                    yield hayFila;
                }
                case "getString", "getDate" -> fila.get((String) args[0]);
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
