
package es.uclm.esi.iso2.radiodiagnostico.cita;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CitaRepositorioMySqlTest {

    private DataSource dataSource;
    private Connection connection;
    private PreparedStatement statement;
    private ResultSet resultado;
    private CitaRepositorioMySql repositorio;

    @BeforeEach
    void setUp() throws Exception {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        statement = mock(PreparedStatement.class);
        resultado = mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultado);

        repositorio = new CitaRepositorioMySql(dataSource);
    }

    @Test
    void buscarPorDniDevuelveLasCitasDelPaciente() throws Exception {
        LocalDateTime fecha = LocalDateTime.of(2026, 12, 1, 10, 0);

        when(resultado.next()).thenReturn(true, false);
        when(resultado.getString("id")).thenReturn("CITA-1");
        when(resultado.getString("id_solicitud")).thenReturn("SOL-1");
        when(resultado.getString("dni_paciente")).thenReturn("12345678A");
        when(resultado.getString("tipo_prueba")).thenReturn("Radiografía");
        when(resultado.getTimestamp("fecha_hora")).thenReturn(Timestamp.valueOf(fecha));
        when(resultado.getString("sala")).thenReturn("SALA-RX-1");
        when(resultado.getString("estado")).thenReturn("ASIGNADA");

        var citas = repositorio.buscarPorDniPaciente("12345678A");

        assertEquals(1, citas.size());
        assertEquals("CITA-1", citas.get(0).id());
        assertEquals("12345678A", citas.get(0).dniPaciente());
        assertEquals(fecha, citas.get(0).fechaHora());

        verify(statement).setString(1, "12345678A");
    }

    @Test
    void buscarPorDniSinCitasDevuelveListaVacia() throws Exception {
        when(resultado.next()).thenReturn(false);

        var citas = repositorio.buscarPorDniPaciente("12345678A");

        assertTrue(citas.isEmpty());
    }
    @Test
    void buscarPorSolicitudDevuelveLaCita() throws Exception {
        LocalDateTime fecha = LocalDateTime.of(2026, 12, 1, 10, 0);

        when(resultado.next()).thenReturn(true);
        when(resultado.getString("id")).thenReturn("CITA-1");
        when(resultado.getString("id_solicitud")).thenReturn("SOL-1");
        when(resultado.getString("dni_paciente")).thenReturn("12345678A");
        when(resultado.getString("tipo_prueba")).thenReturn("Radiografía");
        when(resultado.getTimestamp("fecha_hora")).thenReturn(Timestamp.valueOf(fecha));
        when(resultado.getString("sala")).thenReturn("SALA-RX-1");
        when(resultado.getString("estado")).thenReturn("ASIGNADA");

        var cita = repositorio.buscarPorSolicitud("SOL-1");

        assertTrue(cita.isPresent());
        assertEquals("CITA-1", cita.get().id());
        assertEquals("SOL-1", cita.get().idSolicitud());

        verify(statement).setString(1, "SOL-1");
    }

    @Test
    void guardarInsertaLaCita() throws Exception {
        LocalDateTime fecha = LocalDateTime.of(2026, 12, 1, 10, 0);

        Cita cita = new Cita(
                "CITA-1",
                "SOL-1",
                "12345678A",
                "Radiografía",
                fecha,
                "SALA-RX-1",
                EstadoCita.ASIGNADA
        );

        when(statement.executeUpdate()).thenReturn(1);

        repositorio.guardar(cita);

        verify(statement).setString(1, "CITA-1");
        verify(statement).setString(2, "SOL-1");
        verify(statement).setString(3, "12345678A");
        verify(statement).setString(4, "Radiografía");
        verify(statement).setTimestamp(5, Timestamp.valueOf(fecha));
        verify(statement).setString(6, "SALA-RX-1");
        verify(statement).setString(7, "ASIGNADA");
        verify(statement).executeUpdate();
    }
}

