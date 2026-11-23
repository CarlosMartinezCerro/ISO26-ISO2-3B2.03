package es.uclm.esi.iso2.radiodiagnostico.vista;

import es.uclm.esi.iso2.radiodiagnostico.modelo.informe.ImagenConsultada;
import es.uclm.esi.iso2.radiodiagnostico.modelo.informe.ResumenImagen;
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
