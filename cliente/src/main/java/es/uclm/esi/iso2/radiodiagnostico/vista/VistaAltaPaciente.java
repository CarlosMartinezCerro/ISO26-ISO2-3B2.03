package es.uclm.esi.iso2.radiodiagnostico.vista;

import es.uclm.esi.iso2.radiodiagnostico.paciente.DatosAltaPaciente;

/** Lo que el controlador necesita de cualquier vista del CdU22 (escritorio, web...). */
public interface VistaAltaPaciente {

    /** @return los datos introducidos, o null si la fecha de nacimiento está mal escrita */
    DatosAltaPaciente obtenerDatos();

    void mostrarExito(String mensaje);

    void mostrarError(String mensaje);

    /** Vacía el formulario para dar de alta a otro paciente. */
    void limpiar();
}
