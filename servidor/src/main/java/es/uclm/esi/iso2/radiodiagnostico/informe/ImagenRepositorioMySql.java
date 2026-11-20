package es.uclm.esi.iso2.radiodiagnostico.informe;

import com.mysql.cj.jdbc.MysqlDataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.sql.DataSource;

/** Repositorio de imágenes en MySQL (D-010), tabla {@code imagen} de schema.sql. CdU17 y CdU19. */
public class ImagenRepositorioMySql implements ImagenRepositorio {

    private static final String COLUMNAS = """
            id, id_informe, dni_paciente, id_medico, tipo_prueba,
            nombre_fichero, contenido, fecha_creacion, referencia_pacs""";
    private static final String INSERTAR_IMAGEN = "INSERT INTO imagen (" + COLUMNAS + ")"
            + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String BUSCAR_POR_ID = "SELECT " + COLUMNAS + " FROM imagen WHERE id = ?";
    private static final String BUSCAR_POR_INFORME = "SELECT " + COLUMNAS
            + " FROM imagen WHERE id_informe = ? ORDER BY id";
    private static final String BUSCAR_POR_PACIENTE = "SELECT " + COLUMNAS
            + " FROM imagen WHERE dni_paciente = ? ORDER BY id";
    private static final String CONTAR = "SELECT COUNT(*) FROM imagen";

    private final DataSource dataSource;

    public ImagenRepositorioMySql() {
        this(dataSourceDesdeEntorno());
    }

    public ImagenRepositorioMySql(DataSource dataSource) {
        this.dataSource = Objects.requireNonNull(dataSource, "El DataSource es obligatorio");
    }

    @Override
    public void guardar(Imagen imagen) {
        Objects.requireNonNull(imagen, "La imagen es obligatoria");
        try (var connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERTAR_IMAGEN)) {
            statement.setString(1, imagen.id());
            statement.setString(2, imagen.idInforme());
            statement.setString(3, imagen.dniPaciente());
            statement.setString(4, imagen.idMedico());
            statement.setString(5, imagen.tipoPrueba());
            statement.setString(6, imagen.nombreFichero());
            statement.setBytes(7, imagen.contenido());
            statement.setTimestamp(8, Timestamp.valueOf(imagen.fechaCreacion()));
            statement.setString(9, imagen.referenciaPacs());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo guardar la imagen.", e);
        }
    }

    @Override
    public Optional<Imagen> buscarPorId(String id) {
        return buscar(BUSCAR_POR_ID, id).stream().findFirst();
    }

    @Override
    public List<Imagen> buscarPorInforme(String idInforme) {
        return buscar(BUSCAR_POR_INFORME, idInforme);
    }

    @Override
    public List<Imagen> buscarPorPaciente(String dniPaciente) {
        return buscar(BUSCAR_POR_PACIENTE, dniPaciente);
    }

    @Override
    public int contar() {
        try (var connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(CONTAR);
             ResultSet resultado = statement.executeQuery()) {
            return resultado.next() ? resultado.getInt(1) : 0;
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudieron contar las imágenes.", e);
        }
    }

    private List<Imagen> buscar(String sql, String valor) {
        try (var connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, valor);
            try (ResultSet resultado = statement.executeQuery()) {
                List<Imagen> imagenes = new ArrayList<>();
                while (resultado.next()) {
                    imagenes.add(leer(resultado));
                }
                return imagenes;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudieron buscar las imágenes.", e);
        }
    }

    private static Imagen leer(ResultSet fila) throws SQLException {
        return new Imagen(
                fila.getString("id"),
                fila.getString("id_informe"),
                fila.getString("dni_paciente"),
                fila.getString("id_medico"),
                fila.getString("tipo_prueba"),
                fila.getString("nombre_fichero"),
                fila.getBytes("contenido"),
                fila.getTimestamp("fecha_creacion").toLocalDateTime(),
                fila.getString("referencia_pacs"));
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
