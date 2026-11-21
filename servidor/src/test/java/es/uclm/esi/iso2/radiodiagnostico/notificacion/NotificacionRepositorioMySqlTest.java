package es.uclm.esi.iso2.radiodiagnostico.notificacion;

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
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Pruebas unitarias del repositorio MySQL de notificaciones sin base de datos (como las del CdU22 y
 * CdU19): un JDBC falso (hecho con proxies) registra las sentencias y devuelve las filas indicadas.
 */
class NotificacionRepositorioMySqlTest {

    private static final LocalDateTime FECHA = LocalDateTime.of(2026, 11, 21, 10, 0);
    private static final Notificacion NOTIFICACION = new Notificacion("NOT-001", "12345678Z",
            TipoNotificacion.GENERAL, CanalNotificacion.SMS, "612345678", "Recordatorio", "Traiga la tarjeta.", FECHA);

    private JdbcFalso jdbc;
    private NotificacionRepositorioMySql repositorio;

    @BeforeEach
    void crearRepositorio() {
        jdbc = new JdbcFalso();
        repositorio = new NotificacionRepositorioMySql(jdbc.dataSource());
    }

    @Test
    void guardarInsertaTodosLosCampos() {
        repositorio.guardar(NOTIFICACION);

        assertTrue(jdbc.sql.startsWith("INSERT INTO notificacion"));
        assertEquals(Map.of(1, "NOT-001", 2, "12345678Z", 3, "GENERAL", 4, "SMS", 5, "612345678",
                6, "Recordatorio", 7, "Traiga la tarjeta.", 8, Timestamp.valueOf(FECHA)), jdbc.parametros);
        assertTrue(jdbc.ejecutado);
        assertTrue(jdbc.conexionCerrada, "La conexión debe cerrarse");
    }

    @Test
    void errorAlGuardarLanzaIllegalState() {
        SQLException causa = new SQLException("Conexión perdida", "08S01", 0);
        jdbc.error = causa;

        IllegalStateException excepcion = assertThrows(IllegalStateException.class,
                () -> repositorio.guardar(NOTIFICACION));

        assertSame(causa, excepcion.getCause());
    }

    @Test
    void guardarNuloLanzaExcepcion() {
        assertThrows(NullPointerException.class, () -> repositorio.guardar(null));
    }

    @Test
    void buscarPorPacienteDevuelveLasFilasOrdenadasPorFecha() {
        jdbc.filas.add(fila("NOT-002", "APLICACION", "12345678Z"));
        jdbc.filas.add(fila("NOT-001", "SMS", "612345678"));

        List<Notificacion> notificaciones = repositorio.buscarPorPaciente("12345678Z");

        assertTrue(jdbc.sql.contains("WHERE dni_paciente = ?"));
        assertTrue(jdbc.sql.contains("ORDER BY fecha_envio DESC"));
        assertEquals(Map.of(1, "12345678Z"), jdbc.parametros);
        assertEquals(List.of("NOT-002", "NOT-001"), notificaciones.stream().map(Notificacion::id).toList());
        assertEquals(new Notificacion("NOT-001", "12345678Z", TipoNotificacion.GENERAL, CanalNotificacion.SMS,
                "612345678", "Recordatorio", "Traiga la tarjeta.", FECHA), notificaciones.get(1));
    }

    @Test
    void buscarPorPacienteSinFilasDevuelveListaVacia() {
        assertTrue(repositorio.buscarPorPaciente("12345678Z").isEmpty());
    }

    @Test
    void errorAlBuscarLanzaIllegalState() {
        jdbc.error = new SQLException("Tabla inexistente", "42S02", 1146);

        assertThrows(IllegalStateException.class, () -> repositorio.buscarPorPaciente("12345678Z"));
    }

    @Test
    void contarDevuelveElNumeroDeNotificaciones() {
        jdbc.filas.add(Map.of(1, 4));

        assertEquals(4, repositorio.contar());
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
    void sinVariablesDeEntornoNoSePuedeCrear() {
        Assumptions.assumeTrue(System.getenv("MYSQL_URL") == null, "MYSQL_URL está definida en este equipo");

        IllegalStateException e = assertThrows(IllegalStateException.class, NotificacionRepositorioMySql::new);

        assertEquals("Debe configurarse la variable de entorno MYSQL_URL.", e.getMessage());
    }

    @Test
    void dataSourceNuloLanzaExcepcion() {
        assertThrows(NullPointerException.class, () -> new NotificacionRepositorioMySql((DataSource) null));
    }

    private static Map<Object, Object> fila(String id, String canal, String destino) {
        Map<Object, Object> fila = new HashMap<>();
        fila.put("id", id);
        fila.put("dni_paciente", "12345678Z");
        fila.put("tipo", "GENERAL");
        fila.put("canal", canal);
        fila.put("destino", destino);
        fila.put("asunto", "Recordatorio");
        fila.put("mensaje", "Traiga la tarjeta.");
        fila.put("fecha_envio", Timestamp.valueOf(FECHA));
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
                case "setString", "setTimestamp" -> {
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
                case "getString", "getTimestamp", "getInt" -> filas.get(actual[0]).get(args[0]);
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
