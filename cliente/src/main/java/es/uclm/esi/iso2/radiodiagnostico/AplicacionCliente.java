package es.uclm.esi.iso2.radiodiagnostico;

import es.uclm.esi.iso2.radiodiagnostico.cita.ClienteConfirmacionCitasHttp;
import es.uclm.esi.iso2.radiodiagnostico.controlador.ControladorAltaPaciente;
import es.uclm.esi.iso2.radiodiagnostico.controlador.ControladorAltaPacienteImpl;
import es.uclm.esi.iso2.radiodiagnostico.controlador.ControladorConfirmarCita;
import es.uclm.esi.iso2.radiodiagnostico.controlador.ControladorConfirmarCitaImpl;
import es.uclm.esi.iso2.radiodiagnostico.controlador.ControladorCrearImagen;
import es.uclm.esi.iso2.radiodiagnostico.controlador.ControladorCrearImagenImpl;
import es.uclm.esi.iso2.radiodiagnostico.informe.ClienteImagenesHttp;
import es.uclm.esi.iso2.radiodiagnostico.paciente.ClientePacientesHttp;
import es.uclm.esi.iso2.radiodiagnostico.vista.VentanaAltaPaciente;
import es.uclm.esi.iso2.radiodiagnostico.vista.VentanaConfirmarCita;
import es.uclm.esi.iso2.radiodiagnostico.vista.VentanaCrearImagen;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Aplicación JavaFX del cliente (D-008): muestra un menú con los casos de uso y,
 * al elegir uno, crea su vista, modelo y controlador y los conecta.
 *
 * <p>La dirección del servidor se lee de la variable de entorno {@code SERVIDOR_URL}
 * (por defecto {@code http://localhost:7070}).
 *
 * <p>Arranque: {@code mvn -pl cliente javafx:run}.
 */
public class AplicacionCliente extends Application {

    public static final String URL_POR_DEFECTO = "http://localhost:7070";
    public static final String TITULO = "Radiodiagnóstico";

    @Override
    public void start(Stage ventana) {
        String url = urlServidor();

        VBox menu = new VBox(10,
                botonMenu("CdU22 · Dar de alta paciente", () -> abrirAltaPaciente(url)),
                botonMenu("CdU17 · Crear imagen", () -> abrirCrearImagen(url)),
                botonMenu("CdU2 · Confirmar cita", () -> abrirConfirmarCita(url)));
        menu.setPadding(new Insets(16));

        ventana.setTitle(TITULO);
        ventana.setScene(new Scene(menu));
        ventana.show();
    }

    private static void abrirAltaPaciente(String url) {
        VentanaAltaPaciente vista = new VentanaAltaPaciente();
        ControladorAltaPaciente controlador =
                new ControladorAltaPacienteImpl(vista, new ClientePacientesHttp(url));
        vista.alPulsarAlta(controlador::darDeAlta);
        abrir(VentanaAltaPaciente.TITULO, vista);
    }

    private static void abrirCrearImagen(String url) {
        VentanaCrearImagen vista = new VentanaCrearImagen();
        ControladorCrearImagen controlador =
                new ControladorCrearImagenImpl(vista, new ClienteImagenesHttp(url));
        vista.alPulsarCrear(controlador::crearImagen);
        abrir(VentanaCrearImagen.TITULO, vista);
    }

    private static void abrirConfirmarCita(String url) {
        VentanaConfirmarCita vista = new VentanaConfirmarCita();
        ControladorConfirmarCita controlador =
                new ControladorConfirmarCitaImpl(vista, new ClienteConfirmacionCitasHttp(url));
        vista.alPulsarConfirmar(controlador::confirmarCita);
        abrir(VentanaConfirmarCita.TITULO, vista);
    }

    private static Button botonMenu(String texto, Runnable accion) {
        Button boton = new Button(texto);
        boton.setMaxWidth(Double.MAX_VALUE);
        boton.setOnAction(evento -> accion.run());
        return boton;
    }

    private static void abrir(String titulo, Parent vista) {
        Stage ventana = new Stage();
        ventana.setTitle(titulo);
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
