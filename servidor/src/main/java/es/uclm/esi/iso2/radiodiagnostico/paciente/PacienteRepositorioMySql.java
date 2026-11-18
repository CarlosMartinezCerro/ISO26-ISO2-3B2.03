package es.uclm.esi.iso2.radiodiagnostico.paciente;

import com.mysql.cj.jdbc.MysqlDataSource;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;
import javax.sql.DataSource;

public class PacienteRepositorioMySql implements PacienteRepositorio {

    private static final String INSERTAR_PACIENTE = """
            INSERT INTO paciente (dni, nombre, apellidos, fecha_nacimiento, telefono)
            VALUES (?, ?, ?, ?, ?)
            """;
    private static final String BUSCAR_PACIENTE_POR_DNI = """
            SELECT dni, nombre, apellidos, fecha_nacimiento, telefono
            FROM paciente
            WHERE dni = ?
            """;

    private final DataSource dataSource;

    public PacienteRepositorioMySql() {
        this(dataSourceDesdeEntorno());
    }

    public PacienteRepositorioMySql(DataSource dataSource) {
        this.dataSource = Objects.requireNonNull(dataSource, "El DataSource es obligatorio");
    }

    @Override
    public void guardar(Paciente paciente) {
        Objects.requireNonNull(paciente, "El paciente es obligatorio");
        try (var connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERTAR_PACIENTE)) {
            statement.setString(1, paciente.dni());
            statement.setString(2, paciente.nombre());
            statement.setString(3, paciente.apellidos());
            statement.setDate(4, Date.valueOf(paciente.fechaNacimiento()));
            statement.setString(5, paciente.telefono());
            statement.executeUpdate();
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                throw new PacienteDuplicadoException(
                        "Ya existe un paciente con el DNI " + paciente.dni() + ".");
            }
            throw new IllegalStateException("No se pudo guardar el paciente.", e);
        }
    }

    @Override
    public Optional<Paciente> buscarPorDni(String dni) {
        try (var connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(BUSCAR_PACIENTE_POR_DNI)) {
            statement.setString(1, dni);
            try (ResultSet resultado = statement.executeQuery()) {
                if (!resultado.next()) {
                    return Optional.empty();
                }
                return Optional.of(new Paciente(
                        resultado.getString("dni"),
                        resultado.getString("nombre"),
                        resultado.getString("apellidos"),
                        resultado.getDate("fecha_nacimiento").toLocalDate(),
                        resultado.getString("telefono")));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo buscar el paciente.", e);
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
