package es.uclm.esi.iso2.radiodiagnostico.paciente;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Optional;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * CdU22, unidades RG y RB, con un JDBC falso. Diseño: docs/testing/CdU22-diseno-pruebas.md.
 * Con dos variables, pairwise coincide con el producto cartesiano y contiene a each use (6.4).
 */
@DisplayName("CdU22 · PacienteRepositorioMySql")
class PacienteRepositorioMySqlCdU22Test {

    private static final String DNI = "12345678Z";
    private static final LocalDate NACIMIENTO = LocalDate.of(1990, 5, 12);
    private static final Paciente P1 = new Paciente(DNI, "Alex", "Ejemplo", NACIMIENTO, "600000001");
    private static final Paciente P2 = new Paciente(DNI, "Alex", "Ejemplo", NACIMIENTO, null);
    private static final Paciente P4 = new Paciente(DNI, "Alex", "Ejemplo", null, "600000001");

    private final DataSource dataSource = mock(DataSource.class);
    private final Connection conexion = mock(Connection.class);
    private final PreparedStatement sentencia = mock(PreparedStatement.class);
    private final ResultSet filas = mock(ResultSet.class);
    private PacienteRepositorioMySql repositorio;

    @BeforeEach
    void crearRepositorio() throws SQLException {
        when(dataSource.getConnection()).thenReturn(conexion);
        when(conexion.prepareStatement(contains("paciente"))).thenReturn(sentencia);
        when(sentencia.executeUpdate()).thenReturn(1);
        when(sentencia.executeQuery()).thenReturn(filas);
        repositorio = new PacienteRepositorioMySql(dataSource);
    }

    @Nested
    @DisplayName("RG y RB · Each use")
    class EachUse {

        @ParameterizedTest(name = "{0}")
        @DisplayName("RG: guardar")
        @CsvSource(delimiter = '|', value = {
                "RG-PW-02 = RG-DC-01 | P1 | B2 | DUP",
                "RG-PW-03 | P1 | B3 | ISE",
                "RG-PW-04 | P1 | B4 | ISE",
                "RG-PW-05 = RG-DC-02 | P1 | B5 | ISE",
                "RG-PW-06 | P1 | B6 | ISE",
                "RG-PW-07 | P2 | B1 | OK",
                "RG-PW-13 | P3 | B1 | NPE",
                "RG-PW-19 | P4 | B1 | NPE"})
        void guardarEachUse(String id, String paciente, String baseDatos, String esperado) throws SQLException {
            comprobarGuardar(paciente, baseDatos, esperado);
        }

        @ParameterizedTest(name = "{0}")
        @DisplayName("RB: buscarPorDni")
        @CsvSource(delimiter = '|', value = {
                "RB-PW-01 = RB-DC-02 | Q1 | G1 | P1",
                "RB-PW-02 | Q1 | G2 | P2",
                "RB-PW-03 = RB-DC-01 | Q1 | G3 | VACIO",
                "RB-PW-04 | Q1 | G4 | ISE",
                "RB-PW-05 | Q2 | G3 | VACIO",
                "RB-PW-07 | Q3 | G3 | VACIO"})
        void buscarEachUse(String id, String dni, String baseDatos, String esperado) throws SQLException {
            comprobarBuscar(dni, baseDatos, esperado);
        }
    }

    @Nested
    @DisplayName("RG y RB · Pairwise")
    class Pairwise {

        @ParameterizedTest(name = "{0}")
        @DisplayName("RG: guardar")
        @CsvSource(delimiter = '|', value = {
                "RG-PW-08 | P2 | B2 | DUP",
                "RG-PW-09 | P2 | B3 | ISE",
                "RG-PW-10 | P2 | B4 | ISE",
                "RG-PW-11 | P2 | B5 | ISE",
                "RG-PW-12 | P2 | B6 | ISE",
                "RG-PW-14 | P3 | B2 | NPE",
                "RG-PW-15 | P3 | B3 | NPE",
                "RG-PW-16 | P3 | B4 | NPE",
                "RG-PW-17 | P3 | B5 | NPE",
                "RG-PW-18 | P3 | B6 | NPE",
                "RG-PW-20 | P4 | B2 | NPE",
                "RG-PW-21 | P4 | B3 | NPE",
                "RG-PW-22 | P4 | B4 | NPE",
                "RG-PW-23 | P4 | B5 | NPE",
                "RG-PW-24 | P4 | B6 | ISE"})
        void guardarPairwise(String id, String paciente, String baseDatos, String esperado) throws SQLException {
            comprobarGuardar(paciente, baseDatos, esperado);
        }

        @ParameterizedTest(name = "{0}")
        @DisplayName("RB: buscarPorDni")
        @CsvSource(delimiter = '|', value = {
                "RB-PW-06 | Q2 | G4 | ISE",
                "RB-PW-08 | Q3 | G4 | ISE"})
        void buscarPairwise(String id, String dni, String baseDatos, String esperado) throws SQLException {
            comprobarBuscar(dni, baseDatos, esperado);
        }
    }

    private void comprobarGuardar(String codigoPaciente, String baseDatos, String esperado)
            throws SQLException {
        Paciente paciente = switch (codigoPaciente) {
            case "P1" -> P1;
            case "P2" -> P2;
            case "P3" -> null;
            case "P4" -> P4;
            default -> throw new IllegalArgumentException(codigoPaciente);
        };
        SQLException error = prepararError(baseDatos);

        switch (esperado) {
            case "OK" -> {
                assertDoesNotThrow(() -> repositorio.guardar(paciente));
                verify(conexion).prepareStatement(contains("INSERT INTO paciente"));
                verify(sentencia).setString(1, paciente.dni());
                verify(sentencia).setString(2, paciente.nombre());
                verify(sentencia).setString(3, paciente.apellidos());
                verify(sentencia).setDate(4, Date.valueOf(paciente.fechaNacimiento()));
                verify(sentencia).setString(5, paciente.telefono());
                verify(sentencia).executeUpdate();
            }
            case "DUP" -> {
                PacienteDuplicadoException excepcion = assertThrows(PacienteDuplicadoException.class,
                        () -> repositorio.guardar(paciente));
                assertTrue(excepcion.getMessage().contains(DNI), "El mensaje debe indicar el DNI");
            }
            case "ISE" -> {
                IllegalStateException excepcion = assertThrows(IllegalStateException.class,
                        () -> repositorio.guardar(paciente));
                assertSame(error, excepcion.getCause());
            }
            case "NPE" -> {
                assertThrows(NullPointerException.class, () -> repositorio.guardar(paciente));
                verify(sentencia, never()).executeUpdate();
            }
            default -> throw new IllegalArgumentException(esperado);
        }
        comprobarCierre(codigoPaciente.equals("P3"), baseDatos.equals("B6"));
    }

    /** PC3 y PC4: la conexión se cierra si se llegó a abrir. */
    private void comprobarCierre(boolean sinUsarDataSource, boolean sinConexion) throws SQLException {
        if (sinUsarDataSource) {
            verifyNoInteractions(dataSource);
        }
        if (sinUsarDataSource || sinConexion) {
            verify(sentencia, never()).executeUpdate();
            verifyNoInteractions(conexion);
        } else {
            verify(sentencia).close();
            verify(conexion).close();
        }
    }

    /** @return el error que dará la base de datos, o null si no falla */
    private SQLException prepararError(String baseDatos) throws SQLException {
        SQLException error = switch (baseDatos) {
            case "B1" -> null;
            case "B2" -> new SQLException("Duplicate entry", "23000", 1062);
            case "B3" -> new SQLException("Duplicate key name", "42000", 1061);
            case "B4" -> new SQLException("Incorrect column specifier", "42000", 1063);
            case "B5" -> new SQLException("Conexión perdida", "08S01", 0);
            case "B6" -> new SQLException("Sin conexión", "08001", 0);
            default -> throw new IllegalArgumentException(baseDatos);
        };
        if (baseDatos.equals("B6")) {
            when(dataSource.getConnection()).thenThrow(error);
            org.mockito.Mockito.clearInvocations(dataSource);
        } else if (error != null) {
            when(sentencia.executeUpdate()).thenThrow(error);
            org.mockito.Mockito.clearInvocations(sentencia);
        }
        return error;
    }

    private void comprobarBuscar(String codigoDni, String baseDatos, String esperado)
            throws SQLException {
        String dni = switch (codigoDni) {
            case "Q1" -> DNI;
            case "Q2" -> "";
            case "Q3" -> null;
            default -> throw new IllegalArgumentException(codigoDni);
        };
        SQLException error = new SQLException("Tabla inexistente", "42S02", 1146);
        switch (baseDatos) {
            case "G1" -> prepararFila("600000001");
            case "G2" -> prepararFila(null);
            case "G3" -> when(filas.next()).thenReturn(false);
            case "G4" -> when(sentencia.executeQuery()).thenThrow(error);
            default -> throw new IllegalArgumentException(baseDatos);
        }

        switch (esperado) {
            case "P1" -> assertEquals(Optional.of(P1), repositorio.buscarPorDni(dni));
            case "P2" -> assertEquals(Optional.of(P2), repositorio.buscarPorDni(dni));
            case "VACIO" -> assertEquals(Optional.empty(), repositorio.buscarPorDni(dni));
            case "ISE" -> assertSame(error, assertThrows(IllegalStateException.class,
                    () -> repositorio.buscarPorDni(dni)).getCause());
            default -> throw new IllegalArgumentException(esperado);
        }
        verify(conexion).prepareStatement(contains("WHERE dni = ?"));
        verify(sentencia).setString(1, dni);
        verify(sentencia).close();
        verify(conexion).close();
    }

    private void prepararFila(String telefono) throws SQLException {
        when(filas.next()).thenReturn(true, false);
        when(filas.getString("dni")).thenReturn(DNI);
        when(filas.getString("nombre")).thenReturn("Alex");
        when(filas.getString("apellidos")).thenReturn("Ejemplo");
        when(filas.getDate("fecha_nacimiento")).thenReturn(Date.valueOf(NACIMIENTO));
        when(filas.getString("telefono")).thenReturn(telefono);
    }
}
