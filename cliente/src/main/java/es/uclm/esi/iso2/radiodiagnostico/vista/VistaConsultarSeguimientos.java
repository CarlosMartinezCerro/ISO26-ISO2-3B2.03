package es.uclm.esi.iso2.radiodiagnostico.vista;

import es.uclm.esi.iso2.radiodiagnostico.prueba.SeguimientoPeriodico;
import java.util.List;

/** Vista del CdU11 · Consultar prueba de seguimiento periódica. */
public interface VistaConsultarSeguimientos {

    String obtenerDniPaciente();

    void mostrarSeguimientos(List<SeguimientoPeriodico> seguimientos);

    void mostrarMensaje(String mensaje);

    void mostrarError(String mensaje);

    void limpiar();
}
