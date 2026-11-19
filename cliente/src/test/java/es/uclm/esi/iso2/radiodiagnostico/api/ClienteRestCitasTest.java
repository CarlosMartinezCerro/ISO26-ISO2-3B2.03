package es.uclm.esi.iso2.radiodiagnostico.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.uclm.esi.iso2.radiodiagnostico.cita.DatosAsignacionCita;
import es.uclm.esi.iso2.radiodiagnostico.cita.ErrorComunicacionException;
import es.uclm.esi.iso2.radiodiagnostico.cita.ResultadoAsignacion;
import es.uclm.esi.iso2.radiodiagnostico.cita.SolicitudPendiente;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class ClienteRestCitasTest {

    @Test
    void pideLasSolicitudesPendientes() throws Exception {
        try (ServidorFalso servidor = new ServidorFalso()) {
            servidor.respuesta = "[{\"id\":\"SOL-002\",\"dniPaciente\":\"FICTICIO-002\",\"tipoPrueba\":\"TAC\","
                    + "\"prioridad\":\"URGENTE\",\"estado\":\"PENDIENTE\"}]";

            List<SolicitudPendiente> solicitudes = cliente(servidor).obtenerSolicitudesPendientes();

            assertEquals("GET", servidor.metodo);
            assertEquals("/api/solicitudes/pendientes", servidor.ruta);
            assertEquals(List.of(new SolicitudPendiente("SOL-002", "FICTICIO-002", "TAC", "URGENTE")), solicitudes);
        }
    }

    @Test
    void enviaLaCitaComoJson() throws Exception {
        try (ServidorFalso servidor = new ServidorFalso()) {
            servidor.respuesta = "{\"correcto\":true,\"mensaje\":\"Cita asignada.\"}";

            ResultadoAsignacion resultado = cliente(servidor).asignarCita(
                    new DatosAsignacionCita("SOL-001", LocalDateTime.of(2026, 12, 1, 9, 30), "SALA-RX-1"));

            assertEquals("POST", servidor.metodo);
            assertEquals("/api/citas", servidor.ruta);
            assertTrue(servidor.tipoContenido.startsWith("application/json"));
            assertEquals("{\"idSolicitud\":\"SOL-001\",\"fechaHora\":\"2026-12-01T09:30\",\"sala\":\"SALA-RX-1\"}",
                    servidor.cuerpo);
            assertEquals(new ResultadoAsignacion(true, "Cita asignada."), resultado);
        }
    }

    @Test
    void unErrorHttpEsUnErrorDeComunicacion() throws Exception {
        try (ServidorFalso servidor = new ServidorFalso()) {
            servidor.codigo = 500;

            assertThrows(ErrorComunicacionException.class, () -> cliente(servidor).obtenerSolicitudesPendientes());
        }
    }

    @Test
    void unaRespuestaQueNoEsJsonEsUnErrorDeComunicacion() throws Exception {
        try (ServidorFalso servidor = new ServidorFalso()) {
            servidor.respuesta = "<html>Not found</html>";

            assertThrows(ErrorComunicacionException.class, () -> cliente(servidor).asignarCita(
                    new DatosAsignacionCita("SOL-001", LocalDateTime.of(2026, 12, 1, 9, 30), "SALA-RX-1")));
        }
    }

    @Test
    void sinServidorEsUnErrorDeComunicacion() throws Exception {
        String url;
        try (ServidorFalso servidor = new ServidorFalso()) {
            url = servidor.url();
        }
        ClienteRestCitas cliente = new ClienteRestCitas(new ClienteHttp(url));

        assertThrows(ErrorComunicacionException.class, cliente::obtenerSolicitudesPendientes);
    }

    private static ClienteRestCitas cliente(ServidorFalso servidor) {
        return new ClienteRestCitas(new ClienteHttp(servidor.url() + "/"));
    }
}
