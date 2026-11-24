package es.uclm.esi.iso2.radiodiagnostico.solicitud;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SolicitudRepositorioMySqlTest {

    private DataSource dataSource;
    private PreparedStatement statement;
    private ResultSet filas;
    private SolicitudRepositorioMySql repositorio;

    @BeforeEach
    void preparar() throws Exception {
        dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        statement = mock(PreparedStatement.class);
        filas = mock(ResultSet.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(filas);
        repositorio = new SolicitudRepositorioMySql(dataSource);
    }

    @Test
    void guardarPersisteLaSolicitudYEstado() throws Exception {
        SolicitudPrueba solicitud = new SolicitudPrueba("SOL-1", "12345678A", "TAC",
                PrioridadSolicitud.URGENTE, EstadoSolicitud.PENDIENTE);

        repositorio.guardar(solicitud);

        verify(statement).setString(1, "SOL-1");
        verify(statement).setString(2, "12345678A");
        verify(statement).setString(3, "TAC");
        verify(statement).setString(4, "URGENTE");
        verify(statement).setString(5, "PENDIENTE");
        verify(statement).executeUpdate();
    }

    @Test
    void buscarPorIdDevuelveLaSolicitud() throws Exception {
        when(filas.next()).thenReturn(true);
        prepararFila();

        var solicitud = repositorio.buscarPorId("SOL-1");

        assertTrue(solicitud.isPresent());
        assertEquals("TAC", solicitud.orElseThrow().tipoPrueba());
        assertEquals(EstadoSolicitud.PENDIENTE, solicitud.orElseThrow().estado());
        verify(statement).setString(1, "SOL-1");
    }

    @Test
    void buscarPorEstadoYPorPacienteRecuperanResultados() throws Exception {
        when(filas.next()).thenReturn(true, false);
        prepararFila();

        var solicitudes = repositorio.buscarPorEstado(EstadoSolicitud.PENDIENTE);

        assertEquals(1, solicitudes.size());
        assertEquals("12345678A", solicitudes.get(0).dniPaciente());
        verify(statement).setString(1, "PENDIENTE");
    }

    private void prepararFila() throws Exception {
        when(filas.getString("id")).thenReturn("SOL-1");
        when(filas.getString("dni_paciente")).thenReturn("12345678A");
        when(filas.getString("tipo_prueba")).thenReturn("TAC");
        when(filas.getString("prioridad")).thenReturn("URGENTE");
        when(filas.getString("estado")).thenReturn("PENDIENTE");
    }
}
