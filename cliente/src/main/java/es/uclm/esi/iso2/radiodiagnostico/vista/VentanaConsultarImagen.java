package es.uclm.esi.iso2.radiodiagnostico.vista;

import es.uclm.esi.iso2.radiodiagnostico.informe.ImagenConsultada;
import es.uclm.esi.iso2.radiodiagnostico.informe.ResumenImagen;
import java.io.ByteArrayInputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Function;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/** Vista de escritorio (JavaFX, D-008) del CdU19. No contiene lógica: todo lo decide el controlador. */
public class VentanaConsultarImagen extends BorderPane implements VistaConsultarImagen {

    public static final String TITULO = "Radiodiagnóstico - CdU19: Consultar imagen";
    static final String SIN_VISTA_PREVIA = "Vista previa no disponible (formato DICOM u otro)";
    static final double TAMANO_VISTA_PREVIA = 220;
    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final TextField campoDni = new TextField();
    private final Button botonBuscar = new Button("Buscar imágenes");
    private final Label etiquetaEstado = new Label();
    private final TableView<ResumenImagen> tablaImagenes = new TableView<>();
    private final TextArea areaDatos = new TextArea();
    private final ImageView vistaPrevia = new ImageView();
    private final Label etiquetaVistaPrevia = new Label();
    private final Button botonVer = new Button("Ver imagen");

    public VentanaConsultarImagen() {
        setPadding(new Insets(16));

        campoDni.setId("campoDni");
        botonBuscar.setId("botonBuscar");
        etiquetaEstado.setId("etiquetaEstado");
        tablaImagenes.setId("tablaImagenes");
        areaDatos.setId("areaDatos");
        vistaPrevia.setId("vistaPrevia");
        etiquetaVistaPrevia.setId("etiquetaVistaPrevia");
        botonVer.setId("botonVer");

        campoDni.setPromptText("12345678Z");
        botonBuscar.setDefaultButton(true);
        HBox busqueda = new HBox(10, new Label("DNI del paciente:"), campoDni, botonBuscar, etiquetaEstado);
        busqueda.setPadding(new Insets(0, 0, 10, 0));

        tablaImagenes.getColumns().add(columna("Imagen", ResumenImagen::id));
        tablaImagenes.getColumns().add(columna("Informe", ResumenImagen::idInforme));
        tablaImagenes.getColumns().add(columna("Tipo de prueba", ResumenImagen::tipoPrueba));
        tablaImagenes.getColumns().add(columna("Fecha", imagen -> imagen.fechaCreacion().format(FORMATO)));
        tablaImagenes.getColumns().add(columna("Fichero", ResumenImagen::nombreFichero));
        tablaImagenes.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        areaDatos.setEditable(false);
        areaDatos.setPrefRowCount(7);
        vistaPrevia.setFitWidth(TAMANO_VISTA_PREVIA);
        vistaPrevia.setFitHeight(TAMANO_VISTA_PREVIA);
        vistaPrevia.setPreserveRatio(true);
        VBox detalle = new VBox(10, new Label("Imagen seleccionada"), areaDatos, vistaPrevia, etiquetaVistaPrevia);
        detalle.setPadding(new Insets(0, 0, 0, 10));

        HBox centro = new HBox(tablaImagenes, detalle);
        HBox.setHgrow(tablaImagenes, Priority.ALWAYS);
        tablaImagenes.setPrefSize(560, 380);
        detalle.setPrefWidth(320);

        HBox acciones = new HBox(10, botonVer);
        acciones.setPadding(new Insets(10, 0, 0, 0));

        setTop(busqueda);
        setCenter(centro);
        setBottom(acciones);
    }

    /** El controlador se engancha aquí a los botones (la vista no lo conoce). */
    public void alPulsarBuscar(Runnable accion) {
        botonBuscar.setOnAction(evento -> accion.run());
    }

    public void alPulsarVer(Runnable accion) {
        botonVer.setOnAction(evento -> accion.run());
    }

    @Override
    public String obtenerDniPaciente() {
        return campoDni.getText().trim();
    }

    @Override
    public String obtenerImagenSeleccionada() {
        ResumenImagen seleccionada = tablaImagenes.getSelectionModel().getSelectedItem();
        return seleccionada == null ? null : seleccionada.id();
    }

    @Override
    public void mostrarImagenes(List<ResumenImagen> imagenes, String mensaje) {
        tablaImagenes.getItems().setAll(imagenes);
        etiquetaEstado.setText(mensaje);
        areaDatos.clear();
        vistaPrevia.setImage(null);
        etiquetaVistaPrevia.setText("");
    }

    @Override
    public void mostrarImagen(ImagenConsultada imagen) {
        areaDatos.setText("Imagen: " + imagen.id()
                + "\nInforme: " + imagen.idInforme()
                + "\nPaciente: " + imagen.dniPaciente()
                + "\nTipo de prueba: " + imagen.tipoPrueba()
                + "\nFecha: " + imagen.fechaCreacion().format(FORMATO)
                + "\nFichero: " + imagen.nombreFichero()
                + "\nRIS/PACS: " + imagen.referenciaPacs());
        Image previa = crearVistaPrevia(imagen.contenido());
        vistaPrevia.setImage(previa);
        etiquetaVistaPrevia.setText(previa == null ? SIN_VISTA_PREVIA : "");
    }

    @Override
    public void mostrarError(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.ERROR, mensaje);
        alerta.setTitle("Consultar imagen");
        alerta.setHeaderText(null);
        alerta.showAndWait();
    }

    /** PNG y JPG se pueden pintar; un DICOM necesita un visor específico. */
    static Image crearVistaPrevia(byte[] contenido) {
        if (contenido == null || contenido.length == 0) {
            return null;
        }
        Image imagen = new Image(new ByteArrayInputStream(contenido));
        return imagen.isError() || imagen.getWidth() == 0 ? null : imagen;
    }

    private static TableColumn<ResumenImagen, String> columna(String titulo, Function<ResumenImagen, String> valor) {
        TableColumn<ResumenImagen, String> columna = new TableColumn<>(titulo);
        columna.setCellValueFactory(celda -> new ReadOnlyStringWrapper(valor.apply(celda.getValue())));
        return columna;
    }
}
