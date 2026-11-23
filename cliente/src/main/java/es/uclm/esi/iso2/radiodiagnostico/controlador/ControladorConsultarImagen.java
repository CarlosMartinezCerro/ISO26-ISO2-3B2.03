package es.uclm.esi.iso2.radiodiagnostico.controlador;

public interface ControladorConsultarImagen {

    /** Pide al servidor las imágenes del DNI escrito en la vista y las muestra. */
    void buscarImagenes();

    /** Pide al servidor la imagen seleccionada en la vista y la muestra. */
    void verImagen();
}
