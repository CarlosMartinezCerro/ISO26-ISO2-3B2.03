package es.uclm.esi.iso2.radiodiagnostico.controlador;

public interface ControladorAsignarCita {

    /** Pide al servidor las solicitudes pendientes y se las pasa a la vista. */
    void cargarSolicitudes();

    /** Recoge los datos de la vista, los envía al servidor y muestra el resultado. */
    void asignarCita();
}
