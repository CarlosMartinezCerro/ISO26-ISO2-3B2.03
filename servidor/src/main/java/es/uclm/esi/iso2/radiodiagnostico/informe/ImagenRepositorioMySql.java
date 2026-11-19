package es.uclm.esi.iso2.radiodiagnostico.informe;

import es.uclm.esi.iso2.radiodiagnostico.persistencia.ConexionMySql;
import es.uclm.esi.iso2.radiodiagnostico.persistencia.ErrorBaseDatosException;
import es.uclm.esi.iso2.radiodiagnostico.persistencia.FuenteConexiones;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** Repositorio de imágenes en MySQL (tabla `imagen`, ver schema-imagenes.sql). */
public class ImagenRepositorioMySql implements ImagenRepositorio {

    private static final String COLUMNAS = """
            id, id_informe, dni_paciente, id_medico, tipo_prueba,
            nombre_fichero, contenido, fecha_creacion, referencia_pacs""";
    private static final String INSERTAR = "INSERT INTO imagen (" + COLUMNAS + ")"
            + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String POR_ID = "SELECT " + COLUMNAS + " FROM imagen WHERE id = ?";
    private static final String POR_INFORME = "SELECT " + COLUMNAS
            + " FROM imagen WHERE id_informe = ? ORDER BY id";
    private static final String POR_PACIENTE = "SELECT " + COLUMNAS
            + " FROM imagen WHERE dni_paciente = ? ORDER BY id";
    private static final String CONTAR = "SELECT COUNT(*) FROM imagen";

    private final FuenteConexiones conexiones;

    /** Usa la configuración de las variables de entorno MYSQL_URL, MYSQL_USER y MYSQL_PASSWORD. */
    public ImagenRepositorioMySql() {
        this(ConexionMySql.desdeEntorno());
    }

    public ImagenRepositorioMySql(FuenteConexiones conexiones) {
        this.conexiones = Objects.requireNonNull(conexiones, "La fuente de conexiones es obligatoria");
    }

    @Override
    public void guardar(Imagen imagen) {
        Objects.requireNonNull(imagen, "La imagen es obligatoria");
        try (Connection conexion = conexiones.abrir();
             PreparedStatement sentencia = conexion.prepareStatement(INSERTAR)) {
            sentencia.setString(1, imagen.id());
            sentencia.setString(2, imagen.idInforme());
            sentencia.setString(3, imagen.dniPaciente());
            sentencia.setString(4, imagen.idMedico());
            sentencia.setString(5, imagen.tipoPrueba());
            sentencia.setString(6, imagen.nombreFichero());
            sentencia.setBytes(7, imagen.contenido());
            sentencia.setTimestamp(8, Timestamp.valueOf(imagen.fechaCreacion()));
            sentencia.setString(9, imagen.referenciaPacs());
            sentencia.executeUpdate();
        } catch (SQLException e) {
            throw new ErrorBaseDatosException("No se pudo guardar la imagen " + imagen.id() + ".", e);
        }
    }

    @Override
    public Optional<Imagen> buscarPorId(String id) {
        List<Imagen> encontradas = consultar(POR_ID, id, "No se pudo buscar la imagen " + id + ".");
        return encontradas.stream().findFirst();
    }

    @Override
    public List<Imagen> buscarPorInforme(String idInforme) {
        return consultar(POR_INFORME, idInforme, "No se pudieron buscar las imágenes del informe.");
    }

    @Override
    public List<Imagen> buscarPorPaciente(String dniPaciente) {
        return consultar(POR_PACIENTE, dniPaciente, "No se pudieron buscar las imágenes del paciente.");
    }

    @Override
    public int contar() {
        try (Connection conexion = conexiones.abrir();
             PreparedStatement sentencia = conexion.prepareStatement(CONTAR);
             ResultSet resultado = sentencia.executeQuery()) {
            resultado.next();
            return resultado.getInt(1);
        } catch (SQLException e) {
            throw new ErrorBaseDatosException("No se pudieron contar las imágenes.", e);
        }
    }

    private List<Imagen> consultar(String sql, String parametro, String mensajeError) {
        try (Connection conexion = conexiones.abrir();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, parametro);
            try (ResultSet resultado = sentencia.executeQuery()) {
                List<Imagen> imagenes = new ArrayList<>();
                while (resultado.next()) {
                    imagenes.add(leer(resultado));
                }
                return imagenes;
            }
        } catch (SQLException e) {
            throw new ErrorBaseDatosException(mensajeError, e);
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
}
