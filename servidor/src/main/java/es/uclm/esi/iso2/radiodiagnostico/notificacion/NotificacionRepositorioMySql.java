package es.uclm.esi.iso2.radiodiagnostico.notificacion;

import com.mysql.cj.jdbc.MysqlDataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.sql.DataSource;

/** Repositorio de notificaciones en MySQL (D-010). Tabla {@code notificacion} de schema.sql. */
public class NotificacionRepositorioMySql implements NotificacionRepositorio {

    private static final String INSERTAR_NOTIFICACION = """
            INSERT INTO notificacion (id, dni_paciente, tipo, canal, destino, asunto, mensaje, fecha_envio)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;
    private static final String BUSCAR_POR_PACIENTE = """
            SELECT id, dni_paciente, tipo, canal, destino, asunto, mensaje, fecha_envio
            FROM notificacion
            WHERE dni_paciente = ?
            ORDER BY fecha_envio DESC, id DESC
            """;
    private static final String CONTAR = "SELECT COUNT(*) FROM notificacion";

    private final DataSource dataSource;

    public NotificacionRepositorioMySql() {
        this(dataSourceDesdeEntorno());
    }

    public NotificacionRepositorioMySql(DataSource dataSource) {
        this.dataSource = Objects.requireNonNull(dataSource, "El DataSource es obligatorio");
    }

    @Override
    public void guardar(Notificacion notificacion) {
        Objects.requireNonNull(notificacion, "La notificación es obligatoria");
        try (var connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERTAR_NOTIFICACION)) {
            statement.setString(1, notificacion.id());
            statement.setString(2, notificacion.dniPaciente());
            statement.setString(3, notificacion.tipo().name());
            statement.setString(4, notificacion.canal().name());
            statement.setString(5, notificacion.destino());
            statement.setString(6, notificacion.asunto());
            statement.setString(7, notificacion.mensaje());
            statement.setTimestamp(8, Timestamp.valueOf(notificacion.fechaEnvio()));
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo guardar la notificación.", e);
        }
    }

    @Override
    public List<Notificacion> buscarPorPaciente(String dniPaciente) {
        try (var connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(BUSCAR_POR_PACIENTE)) {
            statement.setString(1, dniPaciente);
            try (ResultSet resultado = statement.executeQuery()) {
                List<Notificacion> notificaciones = new ArrayList<>();
                while (resultado.next()) {
                    notificaciones.add(new Notificacion(
                            resultado.getString("id"),
                            resultado.getString("dni_paciente"),
                            TipoNotificacion.valueOf(resultado.getString("tipo")),
                            CanalNotificacion.valueOf(resultado.getString("canal")),
                            resultado.getString("destino"),
                            resultado.getString("asunto"),
                            resultado.getString("mensaje"),
                            resultado.getTimestamp("fecha_envio").toLocalDateTime()));
                }
                return notificaciones;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudieron buscar las notificaciones.", e);
        }
    }

    @Override
    public int contar() {
        try (var connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(CONTAR);
             ResultSet resultado = statement.executeQuery()) {
            return resultado.next() ? resultado.getInt(1) : 0;
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudieron contar las notificaciones.", e);
        }
    }

    private static DataSource dataSourceDesdeEntorno() {
        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setURL(valorEntornoObligatorio("MYSQL_URL"));
        dataSource.setUser(valorEntornoObligatorio("MYSQL_USER"));
        dataSource.setPassword(valorEntornoObligatorio("MYSQL_PASSWORD"));
        return dataSource;
    }

    private static String valorEntornoObligatorio(String nombre) {
        String valor = System.getenv(nombre);
        if (valor == null || valor.isBlank()) {
            throw new IllegalStateException(
                    "Debe configurarse la variable de entorno " + nombre + ".");
        }
        return valor;
    }
}
