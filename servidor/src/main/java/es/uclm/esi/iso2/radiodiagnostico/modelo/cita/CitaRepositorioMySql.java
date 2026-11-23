
package es.uclm.esi.iso2.radiodiagnostico.modelo.cita;

import com.mysql.cj.jdbc.MysqlDataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.sql.DataSource;

public class CitaRepositorioMySql implements CitaRepositorio {

    private static final String INSERTAR_CITA = """
            INSERT INTO cita (id, id_solicitud, dni_paciente, tipo_prueba,
                              fecha_hora, sala, estado)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

    private static final String BUSCAR_POR_SOLICITUD = """
            SELECT id, id_solicitud, dni_paciente, tipo_prueba,
                   fecha_hora, sala, estado
            FROM cita
            WHERE id_solicitud = ?
            """;

    private static final String BUSCAR_POR_DNI = """
            SELECT id, id_solicitud, dni_paciente, tipo_prueba,
                   fecha_hora, sala, estado
            FROM cita
            WHERE dni_paciente = ?
            ORDER BY fecha_hora
            """;

    private static final String BUSCAR_POR_ID = """
            SELECT id, id_solicitud, dni_paciente, tipo_prueba,
                   fecha_hora, sala, estado
            FROM cita
            WHERE id = ?
            """;

    private static final String ACTUALIZAR_CITA = """
            UPDATE cita
            SET id_solicitud = ?, dni_paciente = ?, tipo_prueba = ?,
                fecha_hora = ?, sala = ?, estado = ?
            WHERE id = ?
            """;

    private static final String EXISTE_CITA = """
            SELECT COUNT(*)
            FROM cita
            WHERE sala = ? AND fecha_hora = ?
            """;

    private final DataSource dataSource;

    public CitaRepositorioMySql() {
        this(dataSourceDesdeEntorno());
    }

    public CitaRepositorioMySql(DataSource dataSource) {
        this.dataSource = Objects.requireNonNull(dataSource, "El DataSource es obligatorio");
    }

    @Override
    public void guardar(Cita cita) {
        Objects.requireNonNull(cita, "La cita es obligatoria");

        try (var connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERTAR_CITA)) {

            statement.setString(1, cita.id());
            statement.setString(2, cita.idSolicitud());
            statement.setString(3, cita.dniPaciente());
            statement.setString(4, cita.tipoPrueba());
            statement.setTimestamp(5, Timestamp.valueOf(cita.fechaHora()));
            statement.setString(6, cita.sala());
            statement.setString(7, cita.estado().name());

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo guardar la cita.", e);
        }
    }

    @Override
    public Optional<Cita> buscarPorSolicitud(String idSolicitud) {
        try (var connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(BUSCAR_POR_SOLICITUD)) {

            statement.setString(1, idSolicitud);

            try (ResultSet resultado = statement.executeQuery()) {
                if (!resultado.next()) {
                    return Optional.empty();
                }

                return Optional.of(convertirCita(resultado));
            }

        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo buscar la cita.", e);
        }
    }

    @Override
    public List<Cita> buscarPorDniPaciente(String dniPaciente) {
        List<Cita> citas = new ArrayList<>();

        try (var connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(BUSCAR_POR_DNI)) {

            statement.setString(1, dniPaciente);

            try (ResultSet resultado = statement.executeQuery()) {
                while (resultado.next()) {
                    citas.add(convertirCita(resultado));
                }
            }

            return citas;

        } catch (SQLException e) {
            throw new IllegalStateException("No se pudieron buscar las citas del paciente.", e);
        }
    }

    @Override
    public Optional<Cita> buscarPorId(String id) {
        try (var connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(BUSCAR_POR_ID)) {

            statement.setString(1, id);

            try (ResultSet resultado = statement.executeQuery()) {
                if (!resultado.next()) {
                    return Optional.empty();
                }

                return Optional.of(convertirCita(resultado));
            }

        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo buscar la cita.", e);
        }
    }

    @Override
    public void actualizar(Cita cita) {
        Objects.requireNonNull(cita, "La cita es obligatoria");

        try (var connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(ACTUALIZAR_CITA)) {

            statement.setString(1, cita.idSolicitud());
            statement.setString(2, cita.dniPaciente());
            statement.setString(3, cita.tipoPrueba());
            statement.setTimestamp(4, Timestamp.valueOf(cita.fechaHora()));
            statement.setString(5, cita.sala());
            statement.setString(6, cita.estado().name());
            statement.setString(7, cita.id());

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo actualizar la cita.", e);
        }
    }

    @Override
    public boolean existeCitaEn(String sala, LocalDateTime fechaHora) {
        try (var connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(EXISTE_CITA)) {

            statement.setString(1, sala);
            statement.setTimestamp(2, Timestamp.valueOf(fechaHora));

            try (ResultSet resultado = statement.executeQuery()) {
                resultado.next();
                return resultado.getInt(1) > 0;
            }

        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo comprobar la disponibilidad de la sala.", e);
        }
    }

    private Cita convertirCita(ResultSet resultado) throws SQLException {
        return new Cita(
                resultado.getString("id"),
                resultado.getString("id_solicitud"),
                resultado.getString("dni_paciente"),
                resultado.getString("tipo_prueba"),
                resultado.getTimestamp("fecha_hora").toLocalDateTime(),
                resultado.getString("sala"),
                EstadoCita.valueOf(resultado.getString("estado")));
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
