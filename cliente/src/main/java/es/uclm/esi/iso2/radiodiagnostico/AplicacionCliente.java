package es.uclm.esi.iso2.radiodiagnostico;

import es.uclm.esi.iso2.radiodiagnostico.controlador.ControladorAltaPaciente;
import es.uclm.esi.iso2.radiodiagnostico.controlador.ControladorAltaPacienteImpl;
import es.uclm.esi.iso2.radiodiagnostico.controlador.ControladorCrearInformeImpl;
import es.uclm.esi.iso2.radiodiagnostico.informe.ClienteInformesHttp;
import es.uclm.esi.iso2.radiodiagnostico.paciente.ClientePacientesHttp;
import es.uclm.esi.iso2.radiodiagnostico.usuario.Medico;
import es.uclm.esi.iso2.radiodiagnostico.vista.VentanaAltaPaciente;
import es.uclm.esi.iso2.radiodiagnostico.vista.VentanaCrearInforme;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Aplicación JavaFX del cliente (D-008): crea vista, modelo y controlador y los conecta.
 *
 * <p>La dirección del servidor se lee de la variable de entorno {@code SERVIDOR_URL}
 * (por defecto {@code http://localhost:7070}).
 *
 * <p>Arranque: {@code mvn -pl cliente javafx:run}.
 */
public class AplicacionCliente extends Application {

    public static final String URL_POR_DEFECTO = "http://localhost:7070";

    @Override
    public void start(Stage ventana) {
       /* VentanaAltaPaciente vista = new VentanaAltaPaciente();
        ControladorAltaPaciente controlador =
                new ControladorAltaPacienteImpl(vista, new ClientePacientesHttp(urlServidor()));
        vista.alPulsarAlta(controlador::darDeAlta);

        ventana.setTitle(VentanaAltaPaciente.TITULO);
        ventana.setScene(new Scene(vista));
        ventana.show();*/
        VentanaCrearInforme vista = new VentanaCrearInforme();
        ControladorCrearInformeImpl controlador =
                new ControladorCrearInformeImpl(vista, new ClienteInformesHttp(urlServidor()));
        vista.alPulsarCargarAntecedentes(
                () -> controlador.consultarAntecedentes(
                        vista.obtenerDniPaciente(),
                        vista.obtenerEspecialidad()));
        vista.alPulsarCrear(controlador::crearInforme);
        ventana.setScene(new Scene(vista));
        ventana.show();
    }

    private static String urlServidor() {
        String url = System.getenv("SERVIDOR_URL");
        return url == null || url.isBlank() ? URL_POR_DEFECTO : url;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
