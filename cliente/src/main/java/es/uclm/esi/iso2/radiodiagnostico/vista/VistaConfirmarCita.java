package es.uclm.esi.iso2.radiodiagnostico.vista;

import es.uclm.esi.iso2.radiodiagnostico.cita.DatosConfirmacionCita;

/** Lo que el controlador necesita de cualquier vista del CdU2 (escritorio, web...). */
public interface VistaConfirmarCita {

    /** @return los datos introducidos, o null si están incompletos */
    DatosConfirmacionCita obtenerDatos();

    void mostrarExito(String mensaje);

    void mostrarError(String mensaje);
}
