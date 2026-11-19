package es.uclm.esi.iso2.radiodiagnostico;

import static org.junit.jupiter.api.Assertions.assertEquals;

import es.uclm.esi.iso2.radiodiagnostico.api.ClienteHttp;
import es.uclm.esi.iso2.radiodiagnostico.api.ServidorFalso;
import es.uclm.esi.iso2.radiodiagnostico.vista.Avisos;
import es.uclm.esi.iso2.radiodiagnostico.vista.PanelAsignarCita;
import es.uclm.esi.iso2.radiodiagnostico.vista.PanelConsultarImagen;
import org.junit.jupiter.api.Test;

class ClienteRadiodiagnosticoTest {

    /** Guarda el último error en vez de abrir un diálogo. */
    private static final class AvisosEnMemoria implements Avisos {
        String error;

        @Override
        public void informacion(String titulo, String mensaje) {
        }

        @Override
        public void error(String titulo, String mensaje) {
            error = mensaje;
        }
    }

    @Test
    void sinVariableUsaElServidorLocal() {
        assertEquals("http://localhost:8080", ClienteRadiodiagnostico.urlServidor(null));
        assertEquals("http://localhost:8080", ClienteRadiodiagnostico.urlServidor(""));
    }

    @Test
    void usaLaUrlDeLaVariable() {
        assertEquals("http://servidor:9090", ClienteRadiodiagnostico.urlServidor(" http://servidor:9090 "));
    }

    @Test
    void asignarCitaCargaLasSolicitudesDelServidorAlConectar() throws Exception {
        try (ServidorFalso servidor = new ServidorFalso()) {
            servidor.respuesta = "[{\"id\":\"SOL-002\",\"dniPaciente\":\"FICTICIO-002\",\"tipoPrueba\":\"TAC\",\"prioridad\":\"URGENTE\"}]";
            AvisosEnMemoria avisos = new AvisosEnMemoria();
            PanelAsignarCita panel = new PanelAsignarCita(avisos);

            ClienteRadiodiagnostico.conectarAsignarCita(panel, new ClienteHttp(servidor.url())).asignarCita();

            assertEquals("/api/solicitudes/pendientes", servidor.ruta);
            assertEquals("Seleccione una solicitud e indique fecha, hora y sala.", avisos.error);
        }
    }

    @Test
    void consultarImagenQuedaConectadoAlControlador() throws Exception {
        try (ServidorFalso servidor = new ServidorFalso()) {
            AvisosEnMemoria avisos = new AvisosEnMemoria();
            PanelConsultarImagen panel = new PanelConsultarImagen(avisos);

            ClienteRadiodiagnostico.conectarConsultarImagen(panel, new ClienteHttp(servidor.url()), "MED-001")
                    .buscarImagenes();

            assertEquals("Introduzca el DNI del paciente.", avisos.error);
        }
    }
}
