package es.uclm.esi.iso2.radiodiagnostico.vista;

import es.uclm.esi.iso2.radiodiagnostico.informe.DatosImagen;

/** Lo que el controlador necesita de cualquier vista del CdU17 (escritorio, web...). */
public interface VistaCrearImagen {

    /** @return los datos introducidos, o null si están incompletos o el fichero no se puede leer */
    DatosImagen obtenerDatos();

    void mostrarExito(String mensaje);

    void mostrarError(String mensaje);

    /** Deja el formulario vacío para crear otra imagen. */
    void limpiar();
}
