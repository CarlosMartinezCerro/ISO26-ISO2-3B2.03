package es.uclm.esi.iso2.radiodiagnostico.solicitud;

import com.mysql.cj.jdbc.MysqlDataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.sql.DataSource;

public class SolicitudRepositorioMySql implements SolicitudRepositorio {

    private static final String GUARDAR = """
            INSERT INTO solicitud (id, dni_paciente, tipo_prueba, prioridad, estado)
            VALUES (?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE dni_paciente = VALUES(dni_paciente),
                tipo_prueba = VALUES(tipo_prueba), prioridad = VALUES(prioridad),
                estado = VALUES(estado)
            """;
    private static final String BUSCAR_POR_ID = """
            SELECT id, dni_paciente, tipo_prueba, prioridad, estado
            FROM solicitud WHERE id = ?
            """;
    private static final String BUSCAR_POR_ESTADO = """
            SELECT id, dni_paciente, tipo_prueba, prioridad, estado
            FROM solicitud WHERE estado = ?
            """;
    private static final String BUSCAR_POR_PACIENTE = """
            SELECT id, dni_paciente, tipo_prueba, prioridad, estado
            FROM solicitud WHERE dni_paciente = ?
            """;

    private final DataSource dataSource;

    public SolicitudRepositorioMySql() {
        this(dataSourceDesdeEntorno());
    }

    public SolicitudRepositorioMySql(DataSource dataSource) {
        this.dataSource = Objects.requireNonNull(dataSource, "El DataSource es obligatorio");
    }

    @Override
    public Optional<SolicitudPrueba> buscarPorId(String id) {
        try (var connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(BUSCAR_POR_ID)) {
            statement.setString(1, id);
            try (ResultSet resultado = statement.executeQuery()) {
                return resultado.next() ? Optional.of(convertir(resultado)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo buscar la solicitud.", e);
        }
    }

    @Override
    public List<SolicitudPrueba> buscarPorEstado(EstadoSolicitud estado) {
        return buscar(BUSCAR_POR_ESTADO, estado.name());
    }

    @Override
    public List<SolicitudPrueba> buscarPorPaciente(String dniPaciente) {
        return buscar(BUSCAR_POR_PACIENTE, dniPaciente);
    }

    private List<SolicitudPrueba> buscar(String consulta, String parametro) {
        List<SolicitudPrueba> resultado = new ArrayList<>();
        try (var connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(consulta)) {
            statement.setString(1, parametro);
            try (ResultSet filas = statement.executeQuery()) {
                while (filas.next()) {
                    resultado.add(convertir(filas));
                }
            }
            return resultado;
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudieron buscar las solicitudes.", e);
        }
    }

    @Override
    public void guardar(SolicitudPrueba solicitud) {
        Objects.requireNonNull(solicitud, "La solicitud es obligatoria");
        try (var connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(GUARDAR)) {
            statement.setString(1, solicitud.id());
            statement.setString(2, solicitud.dniPaciente());
            statement.setString(3, solicitud.tipoPrueba());
            statement.setString(4, solicitud.prioridad().name());
            statement.setString(5, solicitud.estado().name());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo guardar la solicitud.", e);
        }
    }

    private SolicitudPrueba convertir(ResultSet fila) throws SQLException {
        return new SolicitudPrueba(fila.getString("id"), fila.getString("dni_paciente"),
                fila.getString("tipo_prueba"),
                PrioridadSolicitud.valueOf(fila.getString("prioridad")),
                EstadoSolicitud.valueOf(fila.getString("estado")));
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
            throw new IllegalStateException("Debe configurarse la variable de entorno " + nombre + ".");
        }
        return valor;
    }
}
