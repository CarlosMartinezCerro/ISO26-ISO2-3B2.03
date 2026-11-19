
package es.uclm.esi.iso2.radiodiagnostico;

import es.uclm.esi.iso2.radiodiagnostico.cita.ClienteCitasHttp;
import es.uclm.esi.iso2.radiodiagnostico.controlador.ControladorAltaPaciente;
import es.uclm.esi.iso2.radiodiagnostico.controlador.ControladorAltaPacienteImpl;
import es.uclm.esi.iso2.radiodiagnostico.controlador.ControladorConsultarCitas;
import es.uclm.esi.iso2.radiodiagnostico.controlador.ControladorConsultarCitasImpl;
import es.uclm.esi.iso2.radiodiagnostico.paciente.ClientePacientesHttp;
import es.uclm.esi.iso2.radiodiagnostico.vista.VentanaAltaPaciente;
import es.uclm.esi.iso2.radiodiagnostico.vista.VentanaConsultarCitas;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class AplicacionCliente extends Application {

    public static final String URL_POR_DEFECTO =
            "http://localhost:7070";

    @Override
    public void start(Stage ventana) {
        VentanaAltaPaciente vistaAlta = new VentanaAltaPaciente();

        ControladorAltaPaciente controladorAlta =
                new ControladorAltaPacienteImpl(
                        vistaAlta,
                        new ClientePacientesHttp(urlServidor()));

        vistaAlta.alPulsarAlta(controladorAlta::darDeAlta);

        Button botonConsultar = new Button("Consultar citas");

        botonConsultar.setOnAction(evento -> {
            VentanaConsultarCitas vistaCitas =
                    new VentanaConsultarCitas();

            ControladorConsultarCitas controladorCitas =
                    new ControladorConsultarCitasImpl(
                            vistaCitas,
                            new ClienteCitasHttp(urlServidor()));

            vistaCitas.alPulsarConsultar(
                    controladorCitas::consultarCitas);

            Stage ventanaCitas = new Stage();
            ventanaCitas.setTitle(
                    "Radiodiagnóstico - Consultar citas");
            ventanaCitas.setScene(new Scene(vistaCitas, 750, 400));
            ventanaCitas.show();
        });

        VBox contenido = new VBox(15, vistaAlta, botonConsultar);

        ventana.setTitle(VentanaAltaPaciente.TITULO);
        ventana.setScene(new Scene(contenido));
        ventana.show();
    }

    private static String urlServidor() {
        String url = System.getenv("SERVIDOR_URL");
        return url == null || url.isBlank()
                ? URL_POR_DEFECTO : url;
    }

    public static void main(String[] args) {
        launch(args);
    }
}

