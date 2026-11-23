
package es.uclm.esi.iso2.radiodiagnostico.vista;

import es.uclm.esi.iso2.radiodiagnostico.modelo.cita.CitaPaciente;
import java.util.List;

public interface VistaConsultarCitas {

    void mostrarCitas(List<CitaPaciente> citas);

    void mostrarMensaje(String mensaje);

    void mostrarError(String mensaje);
}
