package es.uclm.esi.iso2.radiodiagnostico.vista;

import es.uclm.esi.iso2.radiodiagnostico.informe.ImagenConsultada;
import es.uclm.esi.iso2.radiodiagnostico.informe.ResumenImagen;
import java.util.List;

/** Lo que el controlador necesita de cualquier vista del CdU19 (escritorio, web...). */
public interface VistaConsultarImagen {

    String obtenerDniPaciente();

    /** @return el id de la imagen seleccionada, o null si no hay ninguna */
    String obtenerImagenSeleccionada();

    void mostrarImagenes(List<ResumenImagen> imagenes, String mensaje);

    void mostrarImagen(ImagenConsultada imagen);

    void mostrarError(String mensaje);
}
