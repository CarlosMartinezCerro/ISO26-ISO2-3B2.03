
package es.uclm.esi.iso2.radiodiagnostico;

import es.uclm.esi.iso2.radiodiagnostico.cita.ClienteCitasHttp;
import es.uclm.esi.iso2.radiodiagnostico.cita.ClienteConfirmacionCitasHttp;
import es.uclm.esi.iso2.radiodiagnostico.controlador.ControladorSolicitarPruebaImpl;
import es.uclm.esi.iso2.radiodiagnostico.controlador.ControladorAltaPaciente;
import es.uclm.esi.iso2.radiodiagnostico.controlador.ControladorAltaPacienteImpl;
import es.uclm.esi.iso2.radiodiagnostico.controlador.ControladorAsignarCita;
import es.uclm.esi.iso2.radiodiagnostico.controlador.ControladorAsignarCitaImpl;
import es.uclm.esi.iso2.radiodiagnostico.controlador.ControladorConfirmarCita;
import es.uclm.esi.iso2.radiodiagnostico.controlador.ControladorConfirmarCitaImpl;
import es.uclm.esi.iso2.radiodiagnostico.controlador.ControladorConsultarCitas;
import es.uclm.esi.iso2.radiodiagnostico.controlador.ControladorConsultarCitasImpl;
import es.uclm.esi.iso2.radiodiagnostico.controlador.ControladorConsultarInforme;
import es.uclm.esi.iso2.radiodiagnostico.controlador.ControladorConsultarInformeImpl;
import es.uclm.esi.iso2.radiodiagnostico.controlador.ControladorConsultarImagen;
import es.uclm.esi.iso2.radiodiagnostico.controlador.ControladorConsultarImagenImpl;
import es.uclm.esi.iso2.radiodiagnostico.controlador.ControladorCrearImagen;
import es.uclm.esi.iso2.radiodiagnostico.controlador.ControladorCrearImagenImpl;
import es.uclm.esi.iso2.radiodiagnostico.controlador.ControladorCrearInformeImpl;
import es.uclm.esi.iso2.radiodiagnostico.controlador.ControladorSolicitarSeguimiento;
import es.uclm.esi.iso2.radiodiagnostico.controlador.ControladorSolicitarSeguimientoImpl;
import es.uclm.esi.iso2.radiodiagnostico.informe.ClienteConsultaImagenesHttp;
import es.uclm.esi.iso2.radiodiagnostico.informe.ClienteImagenesHttp;
import es.uclm.esi.iso2.radiodiagnostico.informe.ClienteInformesHttp;
import es.uclm.esi.iso2.radiodiagnostico.paciente.ClientePacientesHttp;
import es.uclm.esi.iso2.radiodiagnostico.prueba.ClienteSeguimientosHttp;
import es.uclm.esi.iso2.radiodiagnostico.solicitud.ClienteSolicitudesHttp;
import es.uclm.esi.iso2.radiodiagnostico.vista.VentanaAltaPaciente;
import es.uclm.esi.iso2.radiodiagnostico.vista.VentanaAsignarCita;
import es.uclm.esi.iso2.radiodiagnostico.vista.VentanaConfirmarCita;
import es.uclm.esi.iso2.radiodiagnostico.vista.VentanaConsultarCitas;
import es.uclm.esi.iso2.radiodiagnostico.vista.VentanaConsultarInforme;
import es.uclm.esi.iso2.radiodiagnostico.vista.VentanaConsultarImagen;
import es.uclm.esi.iso2.radiodiagnostico.vista.VentanaCrearImagen;
import es.uclm.esi.iso2.radiodiagnostico.vista.VentanaCrearInforme;
import es.uclm.esi.iso2.radiodiagnostico.vista.VentanaSolicitarSeguimiento;
import es.uclm.esi.iso2.radiodiagnostico.vista.VentanaSolicitarPrueba;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Aplicación JavaFX del cliente: muestra un menú con los casos de uso y,
 * al elegir uno, crea su vista, modelo y controlador y los conecta.
 *
 * <p>La dirección del servidor se lee de la variable de entorno {@code SERVIDOR_URL}
 * (por defecto {@code http://localhost:7070}). Hasta que exista el inicio de sesión, el médico
 * que consulta imágenes (CdU19) se lee de {@code MEDICO} (por defecto {@code MED-001}).
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
                botonMenu("CdU22 · Dar de alta paciente",
                        () -> abrirAltaPaciente(url)),
                botonMenu("CdU17 · Crear imagen",
                        () -> abrirCrearImagen(url)),
                botonMenu("CdU19 · Consultar imagen",
                        () -> abrirConsultarImagen(url, medico())),
                botonMenu("CdU · Crear informe",
                        () -> abrirCrearInforme(url)),
                botonMenu("CU18 · Consultar informe",
                        () -> abrirConsultarInforme(url)),
                botonMenu("CU1 · Consultar citas",
                        () -> abrirConsultarCitas(url)),
                botonMenu("CdU13 · Solicitar prueba de seguimiento",
                        () -> abrirSolicitarSeguimiento(url)),
                botonMenu("CdU2 · Confirmar cita",
                        () -> abrirConfirmarCita(url)),
                botonMenu("CdU25 · Asignar cita solicitada",
                        () -> abrirAsignarCita(url)),
                botonMenu("CU12 · Solicitar prueba diagnóstica",
                        () -> abrirSolicitarPrueba(url)));

        menu.setPadding(new Insets(16));

        ventana.setTitle(TITULO);
        ventana.setScene(new Scene(menu));
        ventana.show();
    }

    private static void abrirAltaPaciente(String url) {
        VentanaAltaPaciente vista = new VentanaAltaPaciente();

        ControladorAltaPaciente controlador =
                new ControladorAltaPacienteImpl(
                        vista,
                        new ClientePacientesHttp(url));

        vista.alPulsarAlta(controlador::darDeAlta);

        abrir(VentanaAltaPaciente.TITULO, vista);
    }

    private static void abrirCrearImagen(String url) {
        VentanaCrearImagen vista = new VentanaCrearImagen();

        ControladorCrearImagen controlador =
                new ControladorCrearImagenImpl(
                        vista,
                        new ClienteImagenesHttp(url));

        vista.alPulsarCrear(controlador::crearImagen);

        abrir(VentanaCrearImagen.TITULO, vista);
    }

    private static void abrirConsultarImagen(String url, String idMedico) {
        VentanaConsultarImagen vista = new VentanaConsultarImagen();
        ControladorConsultarImagen controlador =
                new ControladorConsultarImagenImpl(vista, new ClienteConsultaImagenesHttp(url), idMedico);
        vista.alPulsarBuscar(controlador::buscarImagenes);
        vista.alPulsarVer(controlador::verImagen);
        abrir(VentanaConsultarImagen.TITULO, vista);
    }

    private static void abrirAsignarCita(String url) {
        VentanaAsignarCita vista = new VentanaAsignarCita();
        ControladorAsignarCita controlador =
                new ControladorAsignarCitaImpl(vista, new ClienteCitasHttp(url));
        vista.alPulsarAsignar(controlador::asignarCita);
        vista.alPulsarRecargar(controlador::cargarSolicitudes);
        abrir(VentanaAsignarCita.TITULO, vista);
        controlador.cargarSolicitudes();
    }

    private static void abrirCrearInforme(String url) {
        VentanaCrearInforme vista = new VentanaCrearInforme();

        ControladorCrearInformeImpl controlador =
                new ControladorCrearInformeImpl(
                        vista,
                        new ClienteInformesHttp(url));

        vista.alPulsarCargarAntecedentes(
                () -> controlador.consultarAntecedentes(
                        vista.obtenerDniPaciente(),
                        vista.obtenerEspecialidad()));

        vista.alPulsarCrear(controlador::crearInforme);

        abrir(VentanaCrearInforme.TITULO, vista);
    }

    private static void abrirConsultarCitas(String url) {
        VentanaConsultarCitas vista = new VentanaConsultarCitas();

        ControladorConsultarCitas controlador =
                new ControladorConsultarCitasImpl(
                        vista,
                        new ClienteCitasHttp(url));

        vista.alPulsarConsultar(controlador::consultarCitas);

        abrir("Radiodiagnóstico - Consultar citas", vista);
    }

    private static void abrirConsultarInforme(String url) {
        VentanaConsultarInforme vista = new VentanaConsultarInforme();

        ControladorConsultarInforme controlador =
                new ControladorConsultarInformeImpl(
                        vista,
                        new ClienteInformesHttp(url));

        vista.alPulsarListar(controlador::listarInformes);
        vista.alSeleccionarInforme(controlador::obtenerInforme);

        abrir(VentanaConsultarInforme.TITULO, vista);
    }

    private static void abrirSolicitarSeguimiento(String url) {
        VentanaSolicitarSeguimiento vista = new VentanaSolicitarSeguimiento();

        ControladorSolicitarSeguimiento controlador =
                new ControladorSolicitarSeguimientoImpl(
                        vista,
                        new ClienteSeguimientosHttp(url));

        vista.alPulsarSolicitar(controlador::solicitar);

        abrir(VentanaSolicitarSeguimiento.TITULO, vista);
    }
  
    private static void abrirConfirmarCita(String url) {
        VentanaConfirmarCita vista = new VentanaConfirmarCita();

        ControladorConfirmarCita controlador =
                new ControladorConfirmarCitaImpl(
                        vista,
                        new ClienteConfirmacionCitasHttp(url));

        vista.alPulsarConfirmar(controlador::confirmarCita);

        abrir(VentanaConfirmarCita.TITULO, vista);
    }

    private static void abrirSolicitarPrueba(String url) {
        VentanaSolicitarPrueba vista = new VentanaSolicitarPrueba();
        ControladorSolicitarPruebaImpl controlador = new ControladorSolicitarPruebaImpl(
                vista, new ClienteSolicitudesHttp(url));
        vista.alPulsarSolicitar(controlador::solicitar);
        controlador.cargarPruebas();
        abrir(VentanaSolicitarPrueba.TITULO, vista);
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

    private static String medico() {
        String medico = System.getenv("MEDICO");
        return medico == null || medico.isBlank() ? "MED-001" : medico;
    }

    private static String urlServidor() {
        String url = System.getenv("SERVIDOR_URL");

        return url == null || url.isBlank()
                ? URL_POR_DEFECTO
                : url;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
