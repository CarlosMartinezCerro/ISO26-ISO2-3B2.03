package es.uclm.esi.iso2.radiodiagnostico;

import es.uclm.esi.iso2.radiodiagnostico.api.ClienteHttp;
import es.uclm.esi.iso2.radiodiagnostico.api.ClienteRestCitas;
import es.uclm.esi.iso2.radiodiagnostico.api.ClienteRestImagenes;
import es.uclm.esi.iso2.radiodiagnostico.controlador.ControladorAsignarCita;
import es.uclm.esi.iso2.radiodiagnostico.controlador.ControladorAsignarCitaImpl;
import es.uclm.esi.iso2.radiodiagnostico.controlador.ControladorConsultarImagen;
import es.uclm.esi.iso2.radiodiagnostico.controlador.ControladorConsultarImagenImpl;
import es.uclm.esi.iso2.radiodiagnostico.vista.PanelAsignarCita;
import es.uclm.esi.iso2.radiodiagnostico.vista.PanelConsultarImagen;
import es.uclm.esi.iso2.radiodiagnostico.vista.VentanaAsignarCita;
import es.uclm.esi.iso2.radiodiagnostico.vista.VentanaConsultarImagen;
import javax.swing.SwingUtilities;

/**
 * Arranca el cliente conectado al servidor REST (D-009).
 * Uso:  (sin argumentos)    → abre las dos ventanas, con el médico MED-001
 *       citas               → solo CdU25 Asignar cita solicitada
 *       imagenes [MÉDICO]   → solo CdU19 Consultar imagen (por defecto MED-001)
 * URL del servidor: variable de entorno SERVIDOR_URL (por defecto http://localhost:8080).
 * Nota: las ventanas son Swing; según la D-008 se pasarán a JavaFX sin tocar controladores ni clientes REST.
 */
public final class ClienteRadiodiagnostico {

    static final String URL_POR_DEFECTO = "http://localhost:8080";

    private ClienteRadiodiagnostico() {
    }

    public static void main(String[] args) {
        ClienteHttp http = new ClienteHttp(urlServidor(System.getenv("SERVIDOR_URL")));
        String opcion = args.length > 0 ? args[0] : "todas";
        String idMedico = args.length > 1 ? args[1] : "MED-001";
        SwingUtilities.invokeLater(() -> {
            if (!"imagenes".equals(opcion)) {
                abrirAsignarCita(http);
            }
            if (!"citas".equals(opcion)) {
                abrirConsultarImagen(http, idMedico);
            }
        });
    }

    private static void abrirAsignarCita(ClienteHttp http) {
        VentanaAsignarCita ventana = new VentanaAsignarCita();
        conectarAsignarCita(ventana.panel(), http);
        ventana.setVisible(true);
    }

    private static void abrirConsultarImagen(ClienteHttp http, String idMedico) {
        VentanaConsultarImagen ventana = new VentanaConsultarImagen();
        conectarConsultarImagen(ventana.panel(), http, idMedico);
        ventana.setVisible(true);
    }

    /** CdU25: une la vista, el controlador y el cliente REST, y carga las solicitudes. */
    static ControladorAsignarCita conectarAsignarCita(PanelAsignarCita panel, ClienteHttp http) {
        ControladorAsignarCita controlador = new ControladorAsignarCitaImpl(panel, new ClienteRestCitas(http));
        panel.alPulsarAsignar(controlador::asignarCita);
        panel.alPulsarRecargar(controlador::cargarSolicitudes);
        controlador.cargarSolicitudes();
        return controlador;
    }

    /** CdU19: une la vista, el controlador y el cliente REST. */
    static ControladorConsultarImagen conectarConsultarImagen(PanelConsultarImagen panel, ClienteHttp http,
                                                              String idMedico) {
        ControladorConsultarImagen controlador =
                new ControladorConsultarImagenImpl(panel, new ClienteRestImagenes(http), idMedico);
        panel.alPulsarBuscar(controlador::buscarImagenes);
        panel.alPulsarVer(controlador::verImagen);
        return controlador;
    }

    static String urlServidor(String valor) {
        return valor == null || valor.isBlank() ? URL_POR_DEFECTO : valor.trim();
    }
}
